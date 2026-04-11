plugins {
    id("org.jetbrains.kotlin.jvm")
}
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.paging.common)
//    implementation(libs.androidx.paging.common.jvm)
}
