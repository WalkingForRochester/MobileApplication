plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
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
        targetSdk = 33
        versionCode = 18
        versionName = "2.0.3"

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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
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

    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.webview)

    implementation("com.google.maps.android:maps-compose:2.10.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    // logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    // for google maps services
    //implementation("org.slf4j:slf4j-simple:1.7.25")

    // coroutines
    val coroutines_version = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")




    // google
    implementation("com.google.android.gms:play-services-auth:20.5.0")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // DO NOT UPDATE TO VERSION 2.2.0 - https://github.com/googlemaps/google-maps-services-java/issues/906
    // Used for places api vs official sdk
    implementation("com.google.maps:google-maps-services:2.1.2")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.11.1")
    implementation("com.google.maps.android:android-maps-utils:3.4.0")

    // facebook
    implementation("com.facebook.android:facebook-login:16.0.0")

    // event bus
    implementation("org.greenrobot:eventbus:3.3.1")

    // Dagger/Hilt
    val hilt_version = "2.51.1"
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    // Retrofit
    val retrofit_version = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    //implementation("com.squareup.retrofit2:converter-scalars:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Kotlin
    val kotlinBom = platform("org.jetbrains.kotlin:kotlin-bom:1.9.24")
    implementation(kotlinBom)
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    androidTestImplementation(kotlinBom)

    // moshi
    val moshi_version = "1.14.0"
    implementation("com.squareup.moshi:moshi:$moshi_version")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshi_version")

    // tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0-alpha03")

    androidTestImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptAndroidTest("com.google.dagger:hilt-compiler:$hilt_version")

    testImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptTest("com.google.dagger:hilt-compiler:$hilt_version")
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