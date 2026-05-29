package com.example.data

data class IslamicEvent(
    val id: String, // format: type_year
    val eventCode: String, // e.g. "ramadan"
    val title: String,
    val dateStr: String, // Format: yyyy-MM-dd
    val hijriDate: String,
    val description: String,
    val isMajor: Boolean = false
)

object IslamicEventData {

    val EVENTS = listOf(
        // === 2025 ===
        IslamicEvent(
            id = "isra_miraj_2025",
            eventCode = "isra_miraj",
            title = "Isra and Mi'raj (Night Journey)",
            dateStr = "2025-01-27",
            hijriDate = "27 Rajab 1446",
            description = "Commemorates the Prophet Muhammad's night journey from Mecca to Jerusalem and ascension to heaven.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ramadan_2025",
            eventCode = "ramadan",
            title = "Ramadan (Start)",
            dateStr = "2025-03-01",
            hijriDate = "1 Ramadan 1446",
            description = "Start of the holy month of fasting, prayer, self-reflection, and charity.",
            isMajor = true
        ),
        IslamicEvent(
            id = "eid_al_fitr_2025",
            eventCode = "eid_al_fitr",
            title = "Eid al-Fitr",
            dateStr = "2025-03-30",
            hijriDate = "1 Shawwal 1446",
            description = "The festival of breaking fast, celebrating the completion of Ramadan.",
            isMajor = true
        ),
        IslamicEvent(
            id = "day_of_arafah_2025",
            eventCode = "arafah",
            title = "Day of Arafah",
            dateStr = "2025-06-05",
            hijriDate = "9 Dhul-Hijjah 1446",
            description = "The most critical day of Hajj, where pilgrims seek divine forgiveness.",
            isMajor = false
        ),
        IslamicEvent(
            id = "eid_al_adha_2025",
            eventCode = "eid_al_adha",
            title = "Eid al-Adha (Feast of Sacrifice)",
            dateStr = "2025-06-06",
            hijriDate = "10 Dhul-Hijjah 1446",
            description = "Commemorates Prophet Ibrahim's absolute devotion and willingness to sacrifice.",
            isMajor = true
        ),
        IslamicEvent(
            id = "islamic_new_year_2025",
            eventCode = "hijri_new_year",
            title = "Islamic New Year (1 Muharram)",
            dateStr = "2025-06-26",
            hijriDate = "1 Muharram 1447",
            description = "Marks the migration (Hijrah) of the Prophet Muhammad from Mecca to Medina.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ashura_2025",
            eventCode = "ashura",
            title = "Day of Ashura (10 Muharram)",
            dateStr = "2025-07-05",
            hijriDate = "10 Muharram 1447",
            description = "A day of fasting, commemorating the liberation of Musa (Moses) and his followers.",
            isMajor = false
        ),
        IslamicEvent(
            id = "mawlid_2025",
            eventCode = "mawlid",
            title = "Mawlid al-Nabi",
            dateStr = "2025-09-05",
            hijriDate = "12 Rabi' al-Awwal 1447",
            description = "The observance of the birthday of Islamic Prophet Muhammad.",
            isMajor = false
        ),

        // === 2026 ===
        IslamicEvent(
            id = "isra_miraj_2026",
            eventCode = "isra_miraj",
            title = "Isra and Mi'raj (Night Journey)",
            dateStr = "2026-01-16",
            hijriDate = "27 Rajab 1447",
            description = "Commemorates the Prophet Muhammad's night journey from Mecca to Jerusalem and ascension to heaven.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ramadan_2026",
            eventCode = "ramadan",
            title = "Ramadan (Start)",
            dateStr = "2026-02-18",
            hijriDate = "1 Ramadan 1447",
            description = "Start of the holy month of fasting, prayer, self-reflection, and charity.",
            isMajor = true
        ),
        IslamicEvent(
            id = "eid_al_fitr_2026",
            eventCode = "eid_al_fitr",
            title = "Eid al-Fitr",
            dateStr = "2026-03-20",
            hijriDate = "1 Shawwal 1447",
            description = "The festival of breaking fast, celebrating the completion of Ramadan.",
            isMajor = true
        ),
        IslamicEvent(
            id = "day_of_arafah_2026",
            eventCode = "arafah",
            title = "Day of Arafah",
            dateStr = "2026-05-26",
            hijriDate = "9 Dhul-Hijjah 1447",
            description = "The most critical day of Hajj, where pilgrims seek divine forgiveness.",
            isMajor = false
        ),
        IslamicEvent(
            id = "eid_al_adha_2026",
            eventCode = "eid_al_adha",
            title = "Eid al-Adha (Feast of Sacrifice)",
            dateStr = "2026-05-27",
            hijriDate = "10 Dhul-Hijjah 1447",
            description = "Commemorates Prophet Ibrahim's absolute devotion and willingness to sacrifice.",
            isMajor = true
        ),
        IslamicEvent(
            id = "islamic_new_year_2026",
            eventCode = "hijri_new_year",
            title = "Islamic New Year (1 Muharram)",
            dateStr = "2026-06-16",
            hijriDate = "1 Muharram 1448",
            description = "Marks the migration (Hijrah) of the Prophet Muhammad from Mecca to Medina.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ashura_2026",
            eventCode = "ashura",
            title = "Day of Ashura (10 Muharram)",
            dateStr = "2026-06-25",
            hijriDate = "10 Muharram 1448",
            description = "A day of fasting, commemorating the liberation of Musa (Moses) and his followers.",
            isMajor = false
        ),
        IslamicEvent(
            id = "mawlid_2026",
            eventCode = "mawlid",
            title = "Mawlid al-Nabi",
            dateStr = "2026-08-25",
            hijriDate = "12 Rabi' al-Awwal 1448",
            description = "The observance of the birthday of Islamic Prophet Muhammad.",
            isMajor = false
        ),

        // === 2027 ===
        IslamicEvent(
            id = "isra_miraj_2027",
            eventCode = "isra_miraj",
            title = "Isra and Mi'raj (Night Journey)",
            dateStr = "2027-01-05",
            hijriDate = "27 Rajab 1448",
            description = "Commemorates the Prophet Muhammad's night journey from Mecca to Jerusalem and ascension to heaven.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ramadan_2027",
            eventCode = "ramadan",
            title = "Ramadan (Start)",
            dateStr = "2027-02-07",
            hijriDate = "1 Ramadan 1448",
            description = "Start of the holy month of fasting, prayer, self-reflection, and charity.",
            isMajor = true
        ),
        IslamicEvent(
            id = "eid_al_fitr_2027",
            eventCode = "eid_al_fitr",
            title = "Eid al-Fitr",
            dateStr = "2027-03-09",
            hijriDate = "1 Shawwal 1448",
            description = "The festival of breaking fast, celebrating the completion of Ramadan.",
            isMajor = true
        ),
        IslamicEvent(
            id = "day_of_arafah_2027",
            eventCode = "arafah",
            title = "Day of Arafah",
            dateStr = "2027-05-15",
            hijriDate = "9 Dhul-Hijjah 1448",
            description = "The most critical day of Hajj, where pilgrims seek divine forgiveness.",
            isMajor = false
        ),
        IslamicEvent(
            id = "eid_al_adha_2027",
            eventCode = "eid_al_adha",
            title = "Eid al-Adha (Feast of Sacrifice)",
            dateStr = "2027-05-16",
            hijriDate = "10 Dhul-Hijjah 1448",
            description = "Commemorates Prophet Ibrahim's absolute devotion and willingness to sacrifice.",
            isMajor = true
        ),
        IslamicEvent(
            id = "islamic_new_year_2027",
            eventCode = "hijri_new_year",
            title = "Islamic New Year (1 Muharram)",
            dateStr = "2027-06-06",
            hijriDate = "1 Muharram 1449",
            description = "Marks the migration (Hijrah) of the Prophet Muhammad from Mecca to Medina.",
            isMajor = false
        ),
        IslamicEvent(
            id = "ashura_2027",
            eventCode = "ashura",
            title = "Day of Ashura (10 Muharram)",
            dateStr = "2027-06-15",
            hijriDate = "10 Muharram 1449",
            description = "A day of fasting, commemorating the liberation of Musa (Moses) and his followers.",
            isMajor = false
        ),
        IslamicEvent(
            id = "mawlid_2027",
            eventCode = "mawlid",
            title = "Mawlid al-Nabi",
            dateStr = "2027-08-15",
            hijriDate = "12 Rabi' al-Awwal 1449",
            description = "The observance of the birthday of Islamic Prophet Muhammad.",
            isMajor = false
        )
    )

    fun getAdjustedEvents(isSouthAsian: Boolean): List<IslamicEvent> {
        if (!isSouthAsian) return EVENTS
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return EVENTS.map { event ->
            try {
                val date = sdf.parse(event.dateStr) ?: return@map event
                val cal = java.util.Calendar.getInstance().apply {
                    time = date
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
                val newDateStr = sdf.format(cal.time)
                event.copy(dateStr = newDateStr)
            } catch (e: Exception) {
                event
            }
        }
    }
}
