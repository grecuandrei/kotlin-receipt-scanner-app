import java.io.FileNotFoundException
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    val propertiesFile = file("./src/main/config.properties") // Adjust the path if needed
    if (propertiesFile.exists()) {
        val properties = Properties()
        properties.load(propertiesFile.inputStream())

        // Define build config fields for sensitive data
        buildTypes.forEach {
            it.buildConfigField("String", "NOTION_BEARER_TOKEN", "\"${properties["NOTION_BEARER_TOKEN"]}\"")
            it.buildConfigField("String", "NOTION_DATABASE_ID", "\"${properties["NOTION_DATABASE_ID"]}\"")
        }
    } else {
        throw FileNotFoundException("config.properties file not found. Please create it with required configurations.")
    }

    namespace = "com.example.receiptscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.receiptscanner"
        minSdk = 26
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ML Kit for text recognition (OCR)
    implementation(libs.text.recognition)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Retrofit for Notion API calls (you can add later)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}