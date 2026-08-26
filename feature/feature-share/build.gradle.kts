plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.feature.share"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(project(":core:core-ui"))

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // hiltViewModel() in Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // guava (ImmutableList for the overlay API, arrives with media3-common too)
    implementation(libs.kotlinx.coroutines.guava)

    // remote-clip fallback download (already a project dependency elsewhere, not a new addition)
    implementation(libs.squareup.okhttp3)

    // Coil for ShareBackgroundSource.FromImageUri (feature-video / feature-image reuse later);
    // audio content always uses FromDrawableRes today.
    implementation(libs.coil.compose)

    // Private preview player for the clip - not the app's shared PlaybackService/MediaSession.
    implementation(libs.media3.exoplayer)

    // On-device video export (Phase 4) - no FFmpeg.
    implementation(libs.media3.transformer)
    implementation(libs.media3.effect)
    implementation(libs.media3.common)
    implementation(libs.media3.muxer)
}
