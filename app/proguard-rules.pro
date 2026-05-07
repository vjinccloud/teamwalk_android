########################################
# 基本屬性（Kotlin / Retrofit / Gson 必要）
########################################
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

########################################
# Gson（API Response）
########################################
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

########################################
# Retrofit / OkHttp
########################################
-keep interface * {
    @retrofit2.http.* <methods>;
}

# 整個保留我們的 Retrofit Service 介面，避免 Koin reified get<T> 在 R8 後
# 出現 ClassCastException（AppModuleKt.appModule lambda$0$10 那條）。
-keep interface com.taiwanlife.teamwalk.remote.service.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**

########################################
# Koin（AppModule + ViewModel）
########################################
-keep class org.koin.** { *; }
-dontwarn org.koin.**

-keep class * extends androidx.lifecycle.ViewModel

# 精準保護你的 AppModule
-keep class com.taiwanlife.teamwalk.di.AppModuleKt { *; }

########################################
# AndroidX / Material / UI
########################################
-dontwarn androidx.**
-dontwarn com.google.android.material.**

########################################
# Glide
########################################
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

########################################
# Firebase / Google / Credentials
########################################
-dontwarn com.google.firebase.**
-dontwarn com.google.android.play.core.**

# Google Identity / Credentials（必要 keep）
-keep class com.google.android.gms.auth.api.** { *; }
-keep class com.google.android.libraries.identity.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.libraries.identity.**

########################################
# DataStore / Tink
########################################
-dontwarn androidx.datastore.**

# Tink 官方建議
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

########################################
# 其他第三方套件
########################################
-dontwarn timber.log.**
-dontwarn org.jetbrains.annotations.**

# PatternLockView / RecyclerCoverFlow
-keep class com.andrognito.patternlockview.** { *; }

########################################
# Kotlin
########################################
-dontwarn kotlin.**
-keep class kotlin.coroutines.Continuation
