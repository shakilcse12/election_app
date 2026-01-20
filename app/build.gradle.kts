plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    kotlin("kapt") // ✅ THIS FIXES kapt()
}

android {
    namespace = "com.example.electionapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.electionapp"
        minSdk = 24
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
        compose = true
    }
    kapt {
        correctErrorTypes = true
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

}

dependencies {
    /* ---------------- Core Android ---------------- */
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    /* ---------------- Jetpack Compose ---------------- */
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    /* ---------------- Navigation ---------------- */
    implementation(libs.androidx.navigation.compose)

    /* ---------------- Lifecycle & StateFlow ---------------- */
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    /* ---------------- Room (Local DB) ---------------- */
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    kapt(libs.androidx.room.compiler)  // This should work now

    /* ---------------- Dependency Injection (Hilt) ---------------- */
    implementation(libs.google.dagger.hilt.android)
    kapt(libs.google.dagger.hilt.compiler)  // This should work now
    implementation(libs.androidx.hilt.navigation.compose)

    /* ---------------- Coroutines ---------------- */
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.datastore.preferences)

    /* ---------------- Google Maps ---------------- */
    implementation(libs.google.maps.compose)
    implementation(libs.google.play.services.maps)
    /* ---------------- OpenStreetMap (OSM) ---------------- */
    // Added osm-droid for free map alternatives
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    /* ---------------- Testing ---------------- */
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}