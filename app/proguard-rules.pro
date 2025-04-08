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

# Keep rules for Couchbase Lite
-keep class com.couchbase.lite.** { *; }
-keep interface com.couchbase.lite.** { *; }
-keep enum com.couchbase.lite.** { *; }

# Keep names of Fleece classes used in reflection
-keepnames class com.couchbase.lite.internal.fleece.** { *; }

# Keep native method names
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep annotations used by Couchbase Lite (though we added the dependency, keeping them explicitly is safer)
-keep class edu.umd.cs.findbugs.annotations.** { *; }
-keep interface edu.umd.cs.findbugs.annotations.** { *; }
-keep class javax.annotation.** { *; }
-keep interface javax.annotation.** { *; }
-keep class net.jcip.annotations.** { *; }
-keep interface net.jcip.annotations.** { *; }

# If using SQLCipher (Community Edition doesn't typically, but good practice if unsure)
# -keep class net.sqlcipher.** { *; }
# -keep class net.sqlcipher.database.** { *; }

# Keep specific methods potentially called via reflection or JNI
-keepclassmembers class com.couchbase.lite.internal.core.C4Log {
    public static void logCallback(java.lang.String, int, java.lang.String);
}
-keepclassmembers class com.couchbase.lite.internal.core.C4Socket {
    public static void socketOpened(long);
    public static void socketClosed(long, int, java.lang.String);
    public static void socketDataReceived(long, byte[]);
    public static void socketRequestClose(long);
    public static void socketGotHTTPResponse(long, int, byte[]);
}
-keepclassmembers class com.couchbase.lite.internal.core.C4Replicator {
     public static void statusChangedCallback(long, int, int, int, int, int, java.lang.String, java.lang.String);
     public static void documentEndedCallback(long, boolean, java.lang.String, java.lang.String, int, int, int, java.lang.String);
     public static boolean validationFunction(java.lang.String, java.lang.String, boolean, boolean, java.lang.Object);
}
-keepclassmembers class com.couchbase.lite.internal.core.C4QueryEnumerator {
    public static void queryCallback(long, long);
}
-keepclassmembers class com.couchbase.lite.internal.core.C4DatabaseObserver {
    public static void databaseChangedCallback(long);
}
-keepclassmembers class com.couchbase.lite.internal.core.C4DocumentObserver {
    public static void documentChangedCallback(long, java.lang.String);
}

# Gson rules
# Prevent R8 from removing generic type information needed by Gson
-keepattributes Signature
# Keep constructors and fields for classes used with Gson
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep your application's model classes (adjust package if necessary)
# This is important for Gson, Room, Couchbase toMap/fromMap, etc.
-keep class com.lmizuno.smallnotesmanager.models.** { *; }
# Keep classes in the utils package as well, including NavigationStackItem (adjust if needed)
-keep class com.lmizuno.smallnotesmanager.utils.** { *; }

# Keep the anonymous inner class used for TypeToken inside NavigationStackManager
# This specifically targets the class causing the crash ($type$1)
-keep class com.lmizuno.smallnotesmanager.utils.NavigationStackManager$* { *; }

# Keep public class * extends com.google.gson.TypeAdapter
-keep public class * extends com.google.gson.TypeAdapter

# Keep GSON specific annotations
-keep @com.google.gson.annotations.SerializedName class * {*;}

# Keep specific classes that might be instantiated reflectively by Gson
-keep class com.google.gson.internal.bind.ArrayTypeAdapter { *; }
-keep class com.google.gson.internal.bind.CollectionTypeAdapterFactory { *; }
-keep class com.google.gson.internal.bind.DateTypeAdapter { *; }
-keep class com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory { *; }
-keep class com.google.gson.internal.bind.MapTypeAdapterFactory { *; }
-keep class com.google.gson.internal.bind.ObjectTypeAdapter { *; }
-keep class com.google.gson.internal.bind.ReflectiveTypeAdapterFactory { *; }
-keep class com.google.gson.internal.bind.SqlDateTypeAdapter { *; }
-keep class com.google.gson.internal.bind.TimeTypeAdapter { *; }
-keep class com.google.gson.internal.bind.TypeAdapters { *; }

# Keep constructors of primitive types used by Gson
-keep public class java.lang.Boolean { public <init>(boolean); }
-keep public class java.lang.Byte    { public <init>(byte); }
-keep public class java.lang.Character { public <init>(char); }
-keep public class java.lang.Double  { public <init>(double); }
-keep public class java.lang.Float   { public <init>(float); }
-keep public class java.lang.Integer { public <init>(int); }
-keep public class java.lang.Long    { public <init>(long); }
-keep public class java.lang.Short   { public <init>(short); }