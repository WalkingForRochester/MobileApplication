buildscript {
    extra["compose_version"] = "1.4.1"
    extra["accompanist_version"] = "0.30.1"
    extra["lifecycle_version"] = "2.6.1"
    extra["hilt_version"] = "2.45"
    extra["retrofit_version"] = "2.9.0"
    extra["moshi_version"] = "1.14.0"
    extra["camerax_version"] = "1.2.2"
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false
}