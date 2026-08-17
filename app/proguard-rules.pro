# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Compose
-keep class androidx.compose.** { *; }

# Keep data models for Room serialization
-keep class com.coffevendor.data.local.** { *; }
-keep class com.coffevendor.data.model.** { *; }
-keep class com.coffevendor.data.remote.WebSocketEvent { *; }
