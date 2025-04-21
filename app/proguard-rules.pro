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
#okhttp
-keep public class * extends android.app.Fragment  #所有fragment的子类不要去混淆
-keep public class * extends android.app.Activity  #所有activity的子类不要去混淆
-keep public class * extends android.app.Application #用法同上
-keep public class * extends android.app.Service #用法同上
-keep public class * extends android.content.BroadcastReceiver #用法同上
-keep public class * extends android.content.ContentProvider #用法同上
-keep public class * extends android.app.backup.BackupAgentHelper #用法同上
-keep public class * extends android.preference.Preference #用法同上
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class com.google.** {*;}
-keep class org.apache.http.conn.** {*;}
-keep class androidx.** {*;}

#fragment
-keep public class * extends android.app.Fragment
-keep public class com.taiwanlife.teamwalk.model.** {*;}

#Log
-assumenosideeffects class android.util.Log {

    public static int v(...);

    public static int i(...);

    public static int w(...);

    public static int d(...);

    public static int e(...);

}
