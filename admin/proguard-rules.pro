# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Suppress SLF4J missing classes warning
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep Firestore Models and Domain Models
-keep class com.example.domain.module.** { *; }
-keep class com.example.data_firebase.model.** { *; }

-keepclassmembers class com.example.domain.module.** {
    public <init>();
    *;
}

-keepclassmembers class com.example.data_firebase.model.** {
    public <init>();
    *;
}

# Keep annotations and signatures
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod