############################################
# GENERAL
############################################

-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn javax.**

-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes KotlinMetadata


############################################
# KOTLIN
############################################
# Kotlin stdlib does NOT need keep rules
# Metadata is enough for reflection / Room / Moshi


############################################
# JETPACK COMPOSE
############################################
# Compose already provides consumer ProGuard rules
# DO NOT add -keep rules here


############################################
# HILT / DAGGER
############################################

# Keep generated Hilt components only
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }

-dontwarn dagger.**
-dontwarn javax.inject.**


############################################
# ROOM
############################################

-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

-dontwarn androidx.room.**


############################################
# RETROFIT
############################################

-keep class retrofit2.Call
-keep class retrofit2.Response

-dontwarn retrofit2.**


############################################
# MOSHI
############################################

# Keep only classes annotated with @JsonClass
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**


############################################
# GSON
############################################

# Keep only fields using @SerializedName
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-dontwarn com.google.gson.**


############################################
# KOTLINX SERIALIZATION
############################################

-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

-dontwarn kotlinx.serialization.**


############################################
# FIREBASE
############################################

-dontwarn com.google.firebase.**


############################################
# ML KIT
############################################

-dontwarn com.google.mlkit.**


############################################
# TENSORFLOW LITE
############################################

-dontwarn org.tensorflow.**
-dontwarn org.tensorflow.lite.**


############################################
# CAMERAX
############################################

-dontwarn androidx.camera.**


############################################
# COIL
############################################

-dontwarn coil.**

############################################
# ANDROID SECURITY CRYPTO
############################################

-dontwarn androidx.security.**


############################################
# ANDROIDX ANNOTATIONS
############################################

-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

-dontwarn androidx.annotation.**


############################################
# YOUR APP (KEEP ONLY WHAT USES REFLECTION)
############################################

# Example: JSON / DB / Serialization models
# Adjust package as needed
-keep class com.rite.pillcounting.model.** { *; }