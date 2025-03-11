plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maps.secrets.plugin)
}

android {
    namespace = "com.walkingforrochester.walkingforrochester.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.walkingforrochester.walkingforrochester.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 24
        versionName = "2.0.7"

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
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
    // Keep source info for android (this is default)
    includeSourceInformation = true

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

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.webkit)

    implementation(libs.google.googleid)

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

    implementation(libs.androidx.browser)
    implementation(libs.androidx.material)

    // core testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)

    implementation(libs.accompanist.permissions)

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
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.okhttp.tls)

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
    ksp(libs.dagger.hilt.compilier)

    androidTestImplementation(libs.dagger.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.compilier)

    testImplementation(libs.dagger.hilt.android.testing)
    kspTest(libs.dagger.hilt.compilier)

    // google service
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
