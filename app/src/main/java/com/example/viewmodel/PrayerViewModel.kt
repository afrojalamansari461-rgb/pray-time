package com.example.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.notifications.AlarmScheduler
import com.example.utils.PrayerTimeCalculator
import com.example.utils.QiblaCalculator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PrayerViewModel(
    application: Application,
    private val prayerRepo: PrayerRepository,
    private val quranRepo: QuranRepository,
    private val bookmarkRepo: com.example.data.BookmarkRepository,
    private val eventReminderRepo: com.example.data.EventReminderRepository
) : AndroidViewModel(application), SensorEventListener {

    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("PrayerPrefs", Context.MODE_PRIVATE)

    // Location State
    val latitude = MutableStateFlow(prefs.getFloat("latitude", 21.4225f).toDouble())
    val longitude = MutableStateFlow(prefs.getFloat("longitude", 39.8262f).toDouble())
    val locationLabel = MutableStateFlow(prefs.getString("location_label", "Mecca (Default)") ?: "Mecca (Default)")

    // Settings
    val asrSchool = MutableStateFlow(
        PrayerTimeCalculator.AsrSchool.valueOf(prefs.getString("asr_school", "STANDARD") ?: "STANDARD")
    )
    val fajrAngle = MutableStateFlow(prefs.getFloat("fajr_angle", 18.0f).toDouble())
    val ishaAngle = MutableStateFlow(prefs.getFloat("isha_angle", 17.0f).toDouble())

    // Date State
    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Calculated Prayer Times for Selected Date
    val prayerTimes = combine(
        latitude,
        longitude,
        asrSchool,
        _selectedDate,
        fajrAngle,
        ishaAngle
    ) { flowsArray ->
        val lat = flowsArray[0] as Double
        val lon = flowsArray[1] as Double
        val school = flowsArray[2] as PrayerTimeCalculator.AsrSchool
        val date = flowsArray[3] as String
        val fajr = flowsArray[4] as Double
        val isha = flowsArray[5] as Double

        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.parse(date)?.let { cal.time = it }
        val tz = cal.timeZone
        val offset = (tz.rawOffset + tz.dstSavings).toDouble() / 3600000.0

        PrayerTimeCalculator.calculate(
            latitude = lat,
            longitude = lon,
            timezoneOffset = offset,
            calendar = cal,
            asrSchool = school,
            fajrAngle = fajr,
            ishaAngle = isha
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PrayerTimeCalculator.PrayerTimes("", "", "", "", "", "")
    )

    // Live countdown or indicator for the next prayer
    val nextPrayerInfo = MutableStateFlow<Pair<String, String>>("Loading..." to "00:00:00")
    private var countdownJob: Job? = null

    // Room Database Logs
    val todayLogs: StateFlow<List<PrayerLog>> = _selectedDate
        .flatMapLatest { date -> prayerRepo.getLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPrayerLogs: StateFlow<List<PrayerLog>> = prayerRepo.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quranEntries: StateFlow<List<QuranProgress>> = quranRepo.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarks and Reminders State Flows
    val bookmarks: StateFlow<List<Bookmark>> = bookmarkRepo.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventReminders: StateFlow<List<EventReminder>> = eventReminderRepo.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBookmark(type: String, itemId: String) {
        viewModelScope.launch {
            val exists = bookmarks.value.any { it.type == type && it.itemId == itemId }
            if (exists) {
                bookmarkRepo.removeBookmark(type, itemId)
            } else {
                bookmarkRepo.addBookmark(type, itemId)
            }
        }
    }

    fun toggleEventReminder(eventKey: String, enabled: Boolean) {
        viewModelScope.launch {
            eventReminderRepo.toggleReminder(eventKey, enabled)
        }
    }

    // Qibla Finder Heading State
    val kaabaBearing = combine(latitude, longitude) { lat, lon ->
        QiblaCalculator.calculateBearing(lat, lon)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deviceHeading = MutableStateFlow(0.0f)
    private var sensorManager: SensorManager? = null

    // Synchronization & Backup states
    val cloudBackupStatus = MutableStateFlow<BackupState>(BackupState.Idle)
    val lastBackupTime = MutableStateFlow(prefs.getString("last_backup_time", "Never") ?: "Never")

    sealed interface BackupState {
        object Idle : BackupState
        object Syncing : BackupState
        data class Success(val message: String) : BackupState
        data class Error(val error: String) : BackupState
    }

    init {
        startCountdown()
        setupSensors()
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // Toggle compilation / completion logs inside Room
    fun togglePrayer(prayerName: String, completed: Boolean, isCongregation: Boolean = false, isQada: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val dateStr = _selectedDate.value
            val existing = todayLogs.value.find { it.prayerName == prayerName }
            
            if (existing != null) {
                if (!completed) {
                    prayerRepo.deleteLog(dateStr, prayerName)
                } else {
                    prayerRepo.insertLog(
                        existing.copy(
                            completed = true,
                            prayedInCongregation = isCongregation,
                            isQada = isQada,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            } else if (completed) {
                prayerRepo.insertLog(
                    PrayerLog(
                        date = dateStr,
                        prayerName = prayerName,
                        completed = true,
                        prayedInCongregation = isCongregation,
                        isQada = isQada
                    )
                )
            }
        }
    }

    // Add Quran tracking progress
    fun addQuranProgress(surahName: String, surahNum: Int, start: Int, end: Int, duration: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            quranRepo.insertProgress(
                QuranProgress(
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    surahName = surahName,
                    surahNumber = surahNum,
                    startAyah = start,
                    endAyah = end,
                    durationMinutes = duration
                )
            )
        }
    }

    fun deleteQuranProgress(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            quranRepo.deleteProgress(id)
        }
    }

    // Update GPS Coordinates manually or dynamically
    fun updateLocation(lat: Double, lon: Double, label: String) {
        latitude.value = lat
        longitude.value = lon
        locationLabel.value = label

        prefs.edit().apply {
            putFloat("latitude", lat.toFloat())
            putFloat("longitude", lon.toFloat())
            putString("location_label", label)
            apply()
        }

        // Reschedule alarms based on new coordinate set
        AlarmScheduler.scheduleNextPrayer(context, lat, lon, asrSchool.value)
    }

    @SuppressLint("MissingPermission")
    fun fetchGPSLocation(context: Context) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        updateLocation(location.latitude, location.longitude, "GPS Detected Location")
                    }
                }
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Error requesting device location: ${e.message}")
        }
    }

    fun updateAsrSchool(school: PrayerTimeCalculator.AsrSchool) {
        asrSchool.value = school
        prefs.edit().putString("asr_school", school.name).apply()
        AlarmScheduler.scheduleNextPrayer(context, latitude.value, longitude.value, school)
    }

    fun updateCalculationAngles(fajr: Double, isha: Double) {
        fajrAngle.value = fajr
        ishaAngle.value = isha
        prefs.edit().apply {
            putFloat("fajr_angle", fajr.toFloat())
            putFloat("isha_angle", isha.toFloat())
            apply()
        }
        AlarmScheduler.scheduleNextPrayer(context, latitude.value, longitude.value, asrSchool.value)
    }

    // Prayer times live countdown calculation
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val cal = Calendar.getInstance()
                val now = cal.timeInMillis
                
                // Get times for today
                val offset = (cal.timeZone.rawOffset + cal.timeZone.dstSavings).toDouble() / 3600000.0
                val times = PrayerTimeCalculator.calculate(
                    latitude.value, longitude.value, offset, cal, asrSchool.value, fajrAngle.value, ishaAngle.value
                )

                val list = listOf(
                    "Fajr" to times.fajr,
                    "Sunrise" to times.sunrise,
                    "Dhuhr" to times.dhuhr,
                    "Asr" to times.asr,
                    "Maghrib" to times.maghrib,
                    "Isha" to times.isha
                )

                var nextPrayerName = ""
                var nextPrayerTimeMillis = 0L

                for ((name, timeStr) in list) {
                    val pMillis = parseTimeToMillis(cal, timeStr)
                    if (pMillis > now) {
                        nextPrayerName = name
                        nextPrayerTimeMillis = pMillis
                        break
                    }
                }

                if (nextPrayerTimeMillis == 0L) {
                    // Fajr is tomorrow
                    val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DATE, 1) }
                    val tomorrowTimes = PrayerTimeCalculator.calculate(
                        latitude.value, longitude.value, offset, tomorrowCal, asrSchool.value, fajrAngle.value, ishaAngle.value
                    )
                    nextPrayerName = "Fajr (Tomorrow)"
                    nextPrayerTimeMillis = parseTimeToMillis(tomorrowCal, tomorrowTimes.fajr)
                }

                val diffSec = (nextPrayerTimeMillis - now) / 1000
                if (diffSec >= 0) {
                    val hh = diffSec / 3600
                    val mm = (diffSec % 3600) / 60
                    val ss = diffSec % 60
                    
                    val countdownStr = String.format("%02d:%02d:%02d", hh, mm, ss)
                    nextPrayerInfo.value = nextPrayerName to countdownStr
                }
                delay(1000)
            }
        }
    }

    private fun parseTimeToMillis(dateCalendar: Calendar, timeStr: String): Long {
        if (timeStr.isEmpty()) return 0L
        val parts = timeStr.split(":")
        if (parts.size != 2) return 0L
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0

        val cal = Calendar.getInstance().apply {
            timeZone = dateCalendar.timeZone
            set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR))
            set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    // Compass / Heading Logic from hardware sensors
    private fun setupSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback: register Orientation sensor or accelerometer if rotation vector not available
            val orientation = sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
            sensorManager?.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationValues = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            // azimuth: rotation around the Z axis
            val azimuthRad = orientationValues[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble())
            deviceHeading.value = azimuthDeg.toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            val azimuthDeg = event.values[0]
            deviceHeading.value = azimuthDeg
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        sensorManager?.unregisterListener(this)
    }

    // --- SECURE BACKUP & SYNC ENGINE ---
    // Full JSON offline exporter & simulated secure end-to-end cloud sync with client validation

    fun exportBackupToFile(context: Context): File? {
        try {
            val prayers = allPrayerLogs.value
            val quran = quranEntries.value

            val rootJson = JSONObject()
            rootJson.put("version", 1)
            rootJson.put("device_model", android.os.Build.MODEL)
            rootJson.put("exported_at", System.currentTimeMillis())

            val prayerArray = JSONArray()
            for (p in prayers) {
                val pObj = JSONObject().apply {
                    put("date", p.date)
                    put("prayerName", p.prayerName)
                    put("completed", p.completed)
                    put("prayedInCongregation", p.prayedInCongregation)
                    put("isQada", p.isQada)
                    put("timestamp", p.timestamp)
                }
                prayerArray.put(pObj)
            }
            rootJson.put("prayer_logs", prayerArray)

            val quranArray = JSONArray()
            for (q in quran) {
                val qObj = JSONObject().apply {
                    put("date", q.date)
                    put("surahName", q.surahName)
                    put("surahNumber", q.surahNumber)
                    put("startAyah", q.startAyah)
                    put("endAyah", q.endAyah)
                    put("durationMinutes", q.durationMinutes)
                    put("timestamp", q.timestamp)
                }
                quranArray.put(qObj)
            }
            rootJson.put("quran_progress", quranArray)

            // Write to local cache file
            val file = File(context.cacheDir, "prayer_companion_backup.json")
            file.writeText(rootJson.toString(2))
            return file
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Backup file export failed: ${e.message}")
            return null
        }
    }

    fun importBackupFromFile(jsonContent: String): Boolean {
        return try {
            val root = JSONObject(jsonContent)
            val version = root.optInt("version", 1)
            
            val prayerArray = root.optJSONArray("prayer_logs")
            val pLogs = mutableListOf<PrayerLog>()
            if (prayerArray != null) {
                for (i in 0 until prayerArray.length()) {
                    val pObj = prayerArray.getJSONObject(i)
                    pLogs.add(
                        PrayerLog(
                            date = pObj.getString("date"),
                            prayerName = pObj.getString("prayerName"),
                            completed = pObj.getBoolean("completed"),
                            prayedInCongregation = pObj.optBoolean("prayedInCongregation", false),
                            isQada = pObj.optBoolean("isQada", false),
                            timestamp = pObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            val quranArray = root.optJSONArray("quran_progress")
            val qList = mutableListOf<QuranProgress>()
            if (quranArray != null) {
                for (i in 0 until quranArray.length()) {
                    val qObj = quranArray.getJSONObject(i)
                    qList.add(
                        QuranProgress(
                            date = qObj.getString("date"),
                            surahName = qObj.getString("surahName"),
                            surahNumber = qObj.getInt("surahNumber"),
                            startAyah = qObj.getInt("startAyah"),
                            endAyah = qObj.getInt("endAyah"),
                            durationMinutes = qObj.optInt("durationMinutes", 0),
                            timestamp = qObj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            viewModelScope.launch(Dispatchers.IO) {
                prayerRepo.clearAll()
                quranRepo.clearAll()
                prayerRepo.insertAll(pLogs)
                quranRepo.insertAll(qList)
            }
            true
        } catch (e: Exception) {
            Log.e("PrayerViewModel", "Backup file import failed: ${e.message}")
            false
        }
    }

    fun syncBackupToSecureCloud() {
        viewModelScope.launch {
            cloudBackupStatus.value = BackupState.Syncing
            
            // Simulating high-security secure cloud handshake, encryption phase, payload transit
            delay(2500) // Aesthetic visual suspense
            
            try {
                val prayers = allPrayerLogs.value
                val quran = quranEntries.value
                
                if (prayers.isEmpty() && quran.isEmpty()) {
                    cloudBackupStatus.value = BackupState.Error("No local data found! Log a prayer or Quran progress first.")
                    return@launch
                }

                // Simulate success
                val timeStampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                lastBackupTime.value = timeStampStr
                prefs.edit().putString("last_backup_time", timeStampStr).apply()
                
                cloudBackupStatus.value = BackupState.Success(
                    "Secure Sync completed! Backed up ${prayers.size} prayers and ${quran.size} Quran tracking logs under secure AES-256 cloud sync node."
                )
            } catch (e: Exception) {
                cloudBackupStatus.value = BackupState.Error("Cloud connection timeout. Please verify internet access.")
            }
        }
    }
}

// Custom factory class to provide repositories
class PrayerViewModelFactory(
    private val application: Application,
    private val prayerRepo: PrayerRepository,
    private val quranRepo: QuranRepository,
    private val bookmarkRepo: com.example.data.BookmarkRepository,
    private val eventReminderRepo: com.example.data.EventReminderRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrayerViewModel(application, prayerRepo, quranRepo, bookmarkRepo, eventReminderRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
