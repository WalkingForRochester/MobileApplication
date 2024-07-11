plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.walkingforrochester.walkingforrochester.android"
    compileSdk = 33

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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // core/ui
    val compose_version = "1.4.1"
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
    implementation("androidx.compose.material3:material3:1.1.0-beta02")
    implementation("androidx.navigation:navigation-compose:2.5.3")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    val accompanist_version = "0.30.1"
    implementation("com.google.accompanist:accompanist-webview:$accompanist_version")
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")
    implementation("com.google.maps.android:maps-compose:2.10.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("io.coil-kt:coil-compose:2.2.2")

    // logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    // for google maps services
    implementation("org.slf4j:slf4j-simple:1.7.25")

    // coroutines
    val coroutines_version = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines_version")

    // lifecycle
    val lifecycle_version = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")

    // camerax
    val camerax_version = "1.2.2"
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

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
    val hilt_version = "2.45"
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    // Retrofit
    val retrofit_version = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-scalars:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // moshi
    val moshi_version = "1.14.0"
    implementation("com.squareup.moshi:moshi:$moshi_version")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshi_version")

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

hilt {
    enableAggregatingTask = true
}