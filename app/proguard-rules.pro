# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interfaceDetail
# class:
#-keepclassmembers class fqcn.of.javascript.interfaceDetail.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn org.slf4j.impl.StaticLoggerBinder

## KOTLIN & COROUTINES

-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin metadata for reflection + Room + Moshi
-keepclassmembers class kotlin.Metadata { *; }
-keepattributes KotlinMetadata


## Jetpack Compose (official rules)

-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Needed for composables with lambdas
-keep class kotlin.Unit


## HILT / DAGGER

-dontwarn dagger.**
-dontwarn javax.inject.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }

# Keep generated Hilt components
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }


## ROOM (official)

-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}
-keepclassmembers class androidx.room.* { *; }
-dontwarn androidx.room.**


## RETROFIT / MOSHI / OKHTTP

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Moshi (very important)
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class **JsonAdapter { *; }
-dontwarn com.squareup.moshi.**


## KOTLINX SERIALIZATION

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontwarn kotlinx.serialization.**


## GSON (you also use Gson + SerializedName)

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep your model classes
-keep class com.rite.pillcounting.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}


## FIREBASE

-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }


## MLKIT (Barcode Scanning)

-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**


## TENSORFLOW LITE

-keep class org.tensorflow.** { *; }
-dontwarn org.tensorflow.**

-keep class org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**


## CAMERAX

-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**


## COIL (image loading)

-dontwarn coil.**
-keep class coil.** { *; }


## ITEXT 7 (PDF)

-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**


## Android Security Crypto

-dontwarn androidx.security.**
-keep class androidx.security.** { *; }


## CORE ANDROIDX

-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keep class androidx.annotation.** { *; }
-dontwarn androidx.annotation.**


## GENERAL SAFE DEFAULTS

-keepattributes InnerClasses
-keepattributes EnclosingMethod
-dontwarn javax.**


