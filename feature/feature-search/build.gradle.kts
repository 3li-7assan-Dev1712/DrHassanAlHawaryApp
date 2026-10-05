import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

/**
 * Reads a secret from local.properties first, then from the environment (used by CI).
 *
 * Values in local.properties are usually written with quotes ( KEY="value" ) while CI
 * environment variables are not. Both are normalised here, so the generated BuildConfig
 * field is always a valid Java string literal. Without this, a quoted local.properties
 * value produces  public static final String KEY = ""value"";  which does not compile.
 */
fun secret(name: String): String =
    (localProperties.getProperty(name) ?: System.getenv(name) ?: "")
        .trim()
        .removeSurrounding("\"")
        .removeSurrounding("'")
        .trim()

android {
    namespace = "app.netlify.devalihassan.feature.search"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")


        // local.properties on a dev machine, environment variables on CI
        buildConfigField("String", "GOOGLE_WEB_CLIENT", "\"${secret("GOOGLE_WEB_CLIENT")}\"")
        buildConfigField("String", "ALGOLIA_APP_ID", "\"${secret("ALGOLIA_APP_ID")}\"")
        buildConfigField("String", "ALGOLIA_API_KEY", "\"${secret("ALGOLIA_API_KEY")}\"")
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


    implementation(project(":core:core-domain"))
    implementation(project(":core:core-database"))
    implementation(project(":core:core-network"))
    implementation(project(":core:core-ui"))


    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // coil
    implementation(libs.coil.compose)

    // Algolia for Search Functionality
    implementation(libs.algolia.search)
    // Ktor only needs ONE HTTP engine on the classpath. Three engines means three copies of
    // every META-INF service/licence file for the APK packager to reconcile. okhttp is the
    // right engine on Android; drop the other two once you have confirmed search still works.
    implementation("io.ktor:ktor-client-okhttp:2.0.1")
    implementation("io.ktor:ktor-client-android:2.0.1")
    implementation("io.ktor:ktor-client-cio:2.0.1")

}