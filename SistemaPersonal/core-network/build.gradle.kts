import org.jetbrains.kotlin.gradle.dsl.JvmTarget

import org.gradle.api.provider.Provider

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

fun configValue(key: String): String {
    val projectValue = providers.gradleProperty(key).orNull
    if (!projectValue.isNullOrBlank()) return projectValue
    return System.getenv(key).orEmpty()
}

fun configString(key: String): String = "\"${configValue(key).replace("\"", "\\\"")}\""

android {
    namespace = "com.sistemapersonal.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 35
        buildConfigField("String", "FIREBASE_API_KEY", configString("FIREBASE_API_KEY"))
        buildConfigField("String", "FIREBASE_APP_ID", configString("FIREBASE_APP_ID"))
        buildConfigField("String", "FIREBASE_PROJECT_ID", configString("FIREBASE_PROJECT_ID"))
        buildConfigField("String", "FIREBASE_DATABASE_URL", configString("FIREBASE_DATABASE_URL"))
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", configString("FIREBASE_STORAGE_BUCKET"))
        buildConfigField("String", "PROVISIONING_APK_DOWNLOAD_URL", configString("PROVISIONING_APK_DOWNLOAD_URL"))
        buildConfigField("String", "PROVISIONING_SIGNATURE_CHECKSUM", configString("PROVISIONING_SIGNATURE_CHECKSUM"))
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":core-model"))






    api(platform("com.google.firebase:firebase-bom:34.16.0"))
    api("com.google.firebase:firebase-database")
    api("com.google.firebase:firebase-auth")
    api("com.google.firebase:firebase-messaging")
    api("com.google.firebase:firebase-storage")
    api("com.google.firebase:firebase-functions")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
}