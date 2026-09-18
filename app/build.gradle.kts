import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val keystoreProperties = Properties().apply {
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}

// Local dev uses keystore.properties (gitignored); CI has no such file and
// supplies the same values as environment variables instead (see
// .github/workflows/release.yml). rootProject.file(...) resolves both a
// relative path (local) and an absolute one (CI) correctly.
val releaseStoreFile: String? = keystoreProperties.getProperty("storeFile") ?: System.getenv("KEYSTORE_PATH")
val releaseStorePassword: String? = keystoreProperties.getProperty("storePassword") ?: System.getenv("KEYSTORE_PASSWORD")
val releaseKeyAlias: String? = keystoreProperties.getProperty("keyAlias") ?: System.getenv("KEY_ALIAS")
val releaseKeyPassword: String? = keystoreProperties.getProperty("keyPassword") ?: System.getenv("KEY_PASSWORD")

// Same pattern as the signing config above: local dev sets this in
// local.properties, CI supplies it as an environment variable instead.
val klipyApiKey: String = localProperties.getProperty("KLIPY_API_KEY") ?: System.getenv("KLIPY_API_KEY") ?: ""

android {
    namespace = "com.beardydev.lookhere"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.beardydev.lookhere"
        minSdk = 26
        targetSdk = 37
        // versionCode derived from semver (major * 10_000 + minor * 100 + patch) so
        // it stays in step with versionName and keeps increasing across releases,
        // which Android requires for every update.
        versionCode = 201
        versionName = "0.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "KLIPY_API_KEY", "\"$klipyApiKey\"")
    }

    signingConfigs {
        create("release") {
            if (releaseStoreFile != null) {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Output APK base name: without this it's "app-<buildType>.apk", taken from
// the Gradle module directory name (app/), not the app's actual name.
base {
    archivesName.set("lookhere")
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    // Not debug-only: AppContainer references it directly from the shared main
    // source set (guarded by BuildConfig.DEBUG at runtime, not by build variant).
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.glide)

    implementation(libs.androidx.window)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
