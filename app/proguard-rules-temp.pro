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
# https://blog.csdn.net/banzhuantuqiang/article/details/143624670
# https://stackoverflow.com/questions/79217938/error-java-lang-class-cannot-be-cast-to-java-lang-reflect-parameterizedtype
# Retrofit Gson

# Retrofit 接口
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# 保留 Retrofit 回應 model 的 class
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson 反序列化用 model 必須保留（尤其有 @SerializedName 的）
-keep class com.taiwanlife.teamwalk.remote.** { *; }
-keep class com.taiwanlife.teamwalk.ui.common.model.** { *; }

# 保留 Gson 註解
-keepattributes *Annotation*
-keepattributes Signature

# 不要混淆自訂的 Retrofit/Gson converter（若有）
-dontwarn com.google.gson.**
-keep class com.google.gson.stream.** { *; }

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# 保留 Retrofit 的泛型 interface
-keep interface retrofit2.Call
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# okhttp3
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okio.**
-dontwarn retrofit2.**

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

## Firebase messaging
#-keep class com.google.firebase.messaging.** { *; }
#-dontwarn com.google.firebase.messaging.**
#
## Crashlytics
#-keep class com.google.firebase.crashlytics.** { *; }
#
## 防止 class name 被混淆以利 Crashlytics 報錯
#-keepattributes SourceFile,LineNumberTable
#
## Google Play Services（Fitness, Auth）
#-keep class com.google.android.gms.** { *; }
#-dontwarn com.google.android.gms.**
#
## Health Connect / androidx.connect
#-keep class androidx.health.connect.client.** { *; }
#-dontwarn androidx.health.connect.client.**
#
## Facebook SDK
#-keep class com.facebook.** { *; }
#-dontwarn com.facebook.**
#
## 若用 LoginManager、Graph API
#-keepclassmembers class * {
#    public void onSuccess(...);
#    public void onCancel();
#    public void onError(...);
#}
#
## Koin
#-keep class org.koin.** { *; }
#-dontwarn org.koin.**
#
#-keep class * implements org.koin.core.component.KoinComponent { *; }
#
## Glide 以防萬一
##-keep public class * implements com.bumptech.glide.module.GlideModule
##-keep public class * extends com.bumptech.glide.AppGlideModule
##-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
##    **[] $VALUES;
##    public *;
##}
#
## Timber
#-dontwarn timber.log.Timber
#
## SecurePreferenceStore
#-keep class online.devliving.** { *; }
#-dontwarn online.devliving.**
#
## 保留 Kotlin data class
#-keepclassmembers class ** {
#    <fields>;
#}
#
## 保留 enum values（避免被混淆刪除）
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}