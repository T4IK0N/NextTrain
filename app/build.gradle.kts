plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python")
}

android {
    namespace = "com.example.nexttrain"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nexttrain"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildToolsVersion = "35.0.0"
    ndkVersion = "27.0.12077973"
    buildFeatures {
        viewBinding = true
    }
}

chaquopy {
    defaultConfig {
        version = "3.13"

        buildPython("D:\\Users\\Bogumil\\AppData\\Local\\Programs\\Python\\Python313\\python.exe") // jeśli potrzeba

        pip {
            install("requests")
            install("numpy")
            install("bs4")
        }
    }

    sourceSets {
        getByName("main") {
            srcDir("src/main/python")
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.barcode.scanning.v1710) // wersja z modelem offline
    implementation(libs.pdfbox.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.gson)
    implementation(libs.play.services.location)
//    implementation(libs.barcode.scanning)

    //calendar view
    // The view calendar library for Android
//    implementation("com.kizitonwose.calendar:view:<latest-version>")
//    implementation("com.kizitonwose.calendar:view:2.3.0")
    // The compose calendar library for Android
//    implementation("com.kizitonwose.calendar:compose:<latest-version>")
}