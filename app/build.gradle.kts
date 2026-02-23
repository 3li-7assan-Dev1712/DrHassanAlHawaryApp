
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.hassanalhawary"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.hassanalhawary"
        minSdk = 24
        targetSdk = 36
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
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // init dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    // auth credential
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.lifecycle.process)
    ksp(libs.hilt.compiler)

    //room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    // exo player and media session && ui
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.session)

    // navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // used in animations
    implementation(libs.androidx.vectordrawable.animated)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // guava
    implementation(libs.kotlinx.coroutines.guava)



    // splash screen
    implementation(libs.androidx.core.splashscreen)

    // youtube player
    implementation(libs.youtube.player)

    // coil
    implementation(libs.coil.compose)


    // depend on the core-ui library for sharing the ui components
    implementation(project(":feature:feature-splash-screen"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-auth"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-image"))
    implementation(project(":feature:feature-study"))
    implementation(project(":feature:feature-onboarding"))
    implementation(project(":feature:feature-search"))
    implementation(project(":feature:feature-video"))
    implementation(project(":feature:feature-article"))
    implementation(project(":feature:feature-about-dr-hassan"))
    implementation(project(":feature:feature-audio"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-player"))
    implementation(project(":data"))

    // test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
configurations.all {
    resolutionStrategy {
        // This block will run for every configuration (implementation, testImplementation, ksp, etc.)
        eachDependency {
            // When Gradle sees a request for the old 'com.intellij:annotations' library...
            if (requested.group == "com.intellij" && requested.name == "annotations") {
                // ...replace it with the newer 'org.jetbrains:annotations' library.
                useTarget("org.jetbrains:annotations:23.0.0")
                // You can provide a reason for tracking purposes.
                because("IntelliJ annotations are older and conflict with the newer JetBrains annotations used by Kotlin/Compose/Room.")
            }
        }
    }
}