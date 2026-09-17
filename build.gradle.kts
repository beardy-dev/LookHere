// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // AGP's built-in Kotlin support pins its own Kotlin Gradle Plugin version;
        // override it so it matches the kotlin.plugin.compose / kotlin.plugin.serialization
        // plugin versions applied in app/build.gradle.kts (both must be the same Kotlin version).
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}