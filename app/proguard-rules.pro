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

# --- Retrofit (required for suspend + annotations reflection) ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Keep Retrofit service interfaces (so method generic signatures remain intact)
-keep interface com.rite.pillcounting.** { *; }
-keep class retrofit2.** { *; }
-keep class kotlin.coroutines.** { *; }


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
############################################
# API MODELS (Retrofit JSON)
############################################
-keep class com.rite.pillcounting.feature.**.model.** { *; }

############################################
# KTOR + NETTY (ANDROID SAFE)
############################################

# BlockHound (JVM-only)
-dontwarn reactor.blockhound.**

# Netty OpenSSL / tcnative (native Linux only)
-dontwarn io.netty.internal.tcnative.**
-dontwarn io.netty.handler.ssl.OpenSsl**
-dontwarn io.netty.handler.ssl.ReferenceCountedOpenSsl**

# Jetty NPN / ALPN (JVM-only)
-dontwarn org.eclipse.jetty.npn.**

# JVM management APIs (not on Android)
-dontwarn java.lang.management.**

# Optional logging frameworks (not bundled)
-dontwarn org.apache.log4j.**
-dontwarn org.apache.logging.log4j.**

# Netty internal logging bridges
-dontwarn io.netty.util.internal.logging.**
# Netty uses reflection to instantiate channel classes (required for Ktor Netty server)
-keep class io.netty.channel.ReflectiveChannelFactory { *; }
-keep class io.netty.channel.socket.nio.NioServerSocketChannel { public <init>(); }
-keep class io.netty.channel.socket.nio.NioSocketChannel { public <init>(); }
-keep class io.netty.channel.nio.NioEventLoopGroup { public <init>(...); }
-keep class io.netty.channel.nio.NioEventLoop { *; }

# Safer broad keep for Netty channel/socket (if above isn't enough)
-keep class io.netty.channel.** { *; }
-keep class io.netty.bootstrap.** { *; }
