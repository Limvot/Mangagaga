# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /opt/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
-keep class org.mozilla.javascript.** {*;}
-keep class org.mozilla.classfile.** {*;}
-keep class io.** {*;}
-keep class kotlin.coroutines.**
-keep class kotlin.jvm.**
-keep class kotlinx.coroutines.**
-keep class org.jetbrains.anko.**
-keep class com.google.gson.** {*;}
-dontoptimize
-dontobfuscate
-keepattributes Signature
-dontwarn sun.misc.Unsafe
-dontwarn java.beans.*
-dontwarn java.lang.management.*
-dontwarn org.eclipse.jgit.**
-dontwarn com.jcraft.jsch.**
-dontwarn org.jetbrains.anko.**

-dontwarn org.mozilla.javascript.**
-dontwarn org.mozilla.classfile.**

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
