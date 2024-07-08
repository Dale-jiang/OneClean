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

# keep ViewBinding
-keep class **.databinding.*Binding { *; }
-keep class **.databinding.*Binding$* { *; }

# keep the class extends ViewBinding
-keepclassmembers class * extends androidx.viewbinding.ViewBinding {
    public static ** inflate(android.view.LayoutInflater);
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
    public static ** bind(android.view.View);
}

# keep ComponentActivity child
-keep class * extends androidx.activity.ComponentActivity { *; }

# keep Fragment and child
-keep class * extends androidx.fragment.app.Fragment { *; }

# keep the class extends Fragment
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public static ** inflate(android.view.LayoutInflater, android.view.ViewGroup, boolean);
}

# kee parameter
-keepattributes Signature
