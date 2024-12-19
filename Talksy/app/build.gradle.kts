plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "praksa.unravel.talksy"
    compileSdk = 35

    defaultConfig {
        applicationId = "praksa.unravel.talksy"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //fragment navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    //noinspection UseTomlInstead
    implementation ("com.google.firebase:firebase-auth:23.1.0")
    implementation (libs.play.services.auth) // For Google sign-in
    implementation (libs.facebook.login.v1520)

    implementation (libs.androidx.lifecycle.viewmodel.ktx) // For ViewModel
    implementation (libs.androidx.lifecycle.livedata.ktx)  // For LiveData
    implementation (libs.androidx.lifecycle.runtime.ktx)   // For lifecycle-aware components

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    //facebook
    implementation (libs.facebook.android.sdk)

    //Room dependency
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    //Firestore
    implementation (libs.firebase.firestore)

    //Firestorage
    implementation(libs.firebase.storage)

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    ksp ("com.github.bumptech.glide:compiler:4.15.1")






}