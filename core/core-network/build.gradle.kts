import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}


val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

/**
 * Reads a secret from local.properties first, then from the environment (used by CI).
 * Surrounding quotes coming from local.properties are stripped so the generated
 * BuildConfig field is always a valid Java string literal.
 */
fun secret(name: String): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: "")
        .trim()
        .removeSurrounding("\"")
        .removeSurrounding("'")
        .trim()

android {
    namespace = "com.example.data_firebase"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        minSdk = 24

        buildConfigField("String", "GOOGLE_WEB_CLIENT", "\"${secret("GOOGLE_WEB_CLIENT")}\"")


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

    implementation(project(":core:core-domain"))
    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.functions)

//    implementation(libs.firebase.database.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}