package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class UserProfileJson(
    val username: String,
    val email: String,
    val passwordSecured: String,
    val isLoggingEnabled: Boolean,
    val address: String,
    val selectedTheme: String
)

interface CloudSyncApi {
    @GET("{key}")
    suspend fun getProfile(@Path("key") key: String): Response<UserProfileJson>

    @PUT("{key}")
    suspend fun saveProfile(@Path("key") key: String, @Body profile: UserProfileJson): Response<String>
}

object CloudSyncManager {
    private const val TAG = "CloudSyncManager"
    private const val BUCKET_NAME = "afah_cloud_sync_bucket_2026_v23"
    private const val BASE_URL = "https://kvdb.io/$BUCKET_NAME/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val api: CloudSyncApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(CloudSyncApi::class.java)
    }

    private fun getEmailHash(email: String): String {
        val normalized = email.lowercase().trim()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(normalized.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Synchronizes a local user account profile to the Secure Al-Afah Cloud Database.
     */
    suspend fun uploadProfile(
        username: String,
        email: String,
        passwordSecured: String,
        isLoggingEnabled: Boolean,
        address: String,
        selectedTheme: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = getEmailHash(email)
            val profile = UserProfileJson(
                username = username,
                email = email.lowercase().trim(),
                passwordSecured = passwordSecured,
                isLoggingEnabled = isLoggingEnabled,
                address = address,
                selectedTheme = selectedTheme
            )
            val response = api.saveProfile(key, profile)
            if (response.isSuccessful) {
                Log.d(TAG, "Profile synchronized successfully for $email.")
                true
            } else {
                Log.e(TAG, "Failed to upload profile: ${response.code()} ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during profile cloud upload", e)
            false
        }
    }

    /**
     * Downloads and returns a user profile from the Secure Al-Afah Cloud Database by email.
     */
    suspend fun downloadProfileByEmail(email: String): UserProfileJson? = withContext(Dispatchers.IO) {
        try {
            val key = getEmailHash(email)
            val response = api.getProfile(key)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.w(TAG, "No profile found on cloud for $email (Code: ${response.code()})")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during profile cloud download", e)
            null
        }
    }
}
