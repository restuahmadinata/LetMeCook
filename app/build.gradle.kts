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

    // Lottie
    implementation(libs.lottie)

    // Retrofit & Gson Converter (untuk networking)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Glide (untuk memuat gambar dari URL)
    implementation(libs.glide)
    implementation(libs.firebase.crashlytics.buildtools)
    annotationProcessor(libs.compiler)

    // Material Design
    implementation(libs.material)

    // Google AI (Gemini)
    implementation(libs.generativeai)

    // Coroutines untuk handle asynchronous call
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.guava)

    implementation(libs.gson)

    // Markwon untuk merender Markdown di TextView
    implementation(libs.core)

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