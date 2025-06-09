import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

android {

    namespace = "com.miraiprjkt.letmecook"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.miraiprjkt.letmecook"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Tambahkan baris ini untuk Lottie
    implementation("com.airbnb.android:lottie:6.4.0")

    // Retrofit & Gson Converter (untuk networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Glide (untuk memuat gambar dari URL)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation(libs.firebase.crashlytics.buildtools)
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Material Design
    implementation("com.google.android.material:material:1.12.0")

    // Google AI (Gemini)
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")

    // Coroutines untuk handle asynchronous call
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("com.google.guava:guava:33.2.1-android")

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}