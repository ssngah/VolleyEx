# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/lihuan/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}

#-keep public class * extends android.support.v4.**
#-keepclassmembers class * {
#    @android.webkit.JavascriptInterface <methods>;
#}


#-keep public class com.huxi.huxilibraries.R$*{
#    public static final int *;
#}

######################gson#######################

#-keep class com.google.gson.JsonObject { *; }
#-keepattributes Signature
#-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.examples.android.model.** { *; }
#-keep class com.google.gson.stream.** { *; }



###################volley############################
#-keep class com.android.volley.** {*;}
#-keep class com.android.volley.toolbox.** {*;}
#-keep class com.android.volley.Response$* { *; }
#-keep class com.android.volley.Request$* { *; }
#-keep class com.android.volley.RequestQueue$* { *; }
#-keep class com.android.volley.toolbox.HurlStack$* { *; }
#-keep class com.android.volley.toolbox.ImageLoader$* { *; }
