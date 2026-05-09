# Kuro ProGuard Rules

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# Keep Gson serialized classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.kuro.music.data.remote.dto.** { *; }

# Keep Room entities
-keep class com.kuro.music.data.local.entity.** { *; }

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
