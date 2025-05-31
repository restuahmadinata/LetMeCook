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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // Retrofit & Gson Converter (untuk networking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // atau versi terbaru
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // atau versi terbaru yang kompatibel

    // Glide (untuk memuat gambar dari URL)
    implementation("com.github.bumptech.glide:glide:4.12.0") // atau versi terbaru
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Material Design (untuk ChipGroup, SearchView, dll. - seharusnya sudah ada dari setup awal)
    implementation("com.google.android.material:material:1.12.0") // Sesuaikan dengan versi yang Anda gunakan

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