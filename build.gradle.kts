// Root build.gradle.kts

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0") // Required for Firebase
    }
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
