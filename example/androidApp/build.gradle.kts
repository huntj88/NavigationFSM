plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
}
group = "me.jameshunt.example"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
}
dependencies {
    implementation(project(":shared"))
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.material:material:1.2.1")
    implementation(project(":NavigationFSM"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.1")
}

tasks.register<me.jameshunt.navfsm.compile.GenerateAndroidProxiesTask>("generateProxies")
tasks.getByName("preBuild").dependsOn(tasks.getByName("generateProxies"))

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "me.jameshunt.example.androidApp"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}