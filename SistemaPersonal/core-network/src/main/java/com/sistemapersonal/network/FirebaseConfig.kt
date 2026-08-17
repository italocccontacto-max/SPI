package com.sistemapersonal.network

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"

    val API_KEY: String get() = BuildConfig.FIREBASE_API_KEY
    val APPLICATION_ID: String get() = BuildConfig.FIREBASE_APP_ID
    val PROJECT_ID: String get() = BuildConfig.FIREBASE_PROJECT_ID
    val DATABASE_URL: String get() = BuildConfig.FIREBASE_DATABASE_URL
    val STORAGE_BUCKET: String get() = BuildConfig.FIREBASE_STORAGE_BUCKET

    val estaConfigurado: Boolean
        get() = API_KEY.isNotBlank() && APPLICATION_ID.isNotBlank() &&
            PROJECT_ID.isNotBlank() && DATABASE_URL.isNotBlank() && STORAGE_BUCKET.isNotBlank()

    val provisioningUrl: String get() = BuildConfig.PROVISIONING_APK_DOWNLOAD_URL
    val provisioningSignatureChecksum: String get() = BuildConfig.PROVISIONING_SIGNATURE_CHECKSUM

    fun inicializar(context: Context) {
        if (!estaConfigurado) return
        if (FirebaseApp.getApps(context).isNotEmpty()) return
        runCatching {
            val options = FirebaseOptions.Builder()
                .setApiKey(API_KEY)
                .setApplicationId(APPLICATION_ID)
                .setProjectId(PROJECT_ID)
                .setDatabaseUrl(DATABASE_URL)
                .setStorageBucket(STORAGE_BUCKET)
                .build()
            FirebaseApp.initializeApp(context, options)
        }.onFailure { Log.e(TAG, "No se pudo inicializar Firebase", it) }
    }
}
