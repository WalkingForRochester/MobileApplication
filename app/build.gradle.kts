plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.maps.secrets.plugin)
}

android {
    namespace = "com.walkingforrochester.walkingforrochester.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.walkingforrochester.walkingforrochester.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 19
        versionName = "2.0.4"

        // For now only supporting English, so stripping out other languages
        // Will strip pseudoLocales en-rXA or ar-rXB as well if testing.
        resourceConfigurations += "en"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    enableStrongSkippingMode = true

    // Uncomment to generate reports
    // reportsDestination = layout.buildDirectory.dir("compose_compiler")
    // Uncomment to provide stability overrides
    // stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.txt")
}


dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)

    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    androidTestImplementation(libs.androidx.navigation.testing)

    implementation(libs.androidx.material)

    // core testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)

    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.webview)

    // Kotlin
    val kotlinBom = platform(libs.kotlin.bom)
    implementation(kotlinBom)
    implementation(libs.kotlin.stdlib)
    androidTestImplementation(kotlinBom)

    // Coroutines
    val coroutinesBom = platform(libs.kotlinx.coroutines.bom)
    implementation(coroutinesBom)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    androidTestImplementation(coroutinesBom)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // okhttp
    val okHttpBom = platform(libs.okhttp.bom)
    implementation(okHttpBom)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    androidTestImplementation(okHttpBom)

    // retrofit
    val retrofitBom = platform(libs.retrofit.bom)
    implementation(retrofitBom)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)

    // okio
    implementation(libs.okio)

    // moshi
    implementation(libs.moshi)
    implementation(libs.moshi.adapters)
    ksp(libs.moshi.kotlin.codegen)

    // Dagger/Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compilier)

    androidTestImplementation(libs.dagger.hilt.android.testing)
    kaptAndroidTest(libs.dagger.hilt.compilier)

    testImplementation(libs.dagger.hilt.android.testing)
    kaptTest(libs.dagger.hilt.compilier)

    // google service
    implementation(libs.play.services.auth)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.google.android.maps.compose)
    implementation(libs.google.android.maps.utils)
    implementation(libs.google.libphonenumber)

    val coilBom = platform(libs.coil.bom)
    implementation(coilBom)
    implementation(libs.coil.compose)

    // logging
    implementation(libs.timber)

    // event bus
    implementation(libs.eventbus)

    // facebook
    implementation(libs.facebook.login)

    debugImplementation(libs.leakcanary.android)
}

secrets {
    // Change the properties file from the default "local.properties" in your root project
    // to another properties file in your root project.
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be checked in version
    // control.
    defaultPropertiesFileName = "secrets.defaults.properties"

    // See https://github.com/google/secrets-gradle-plugin/tree/main for full options
}

kapt {
    correctErrorTypes = true
}