# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.coffevendor.data.model.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Moshi
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
