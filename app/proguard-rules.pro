# ---------- OpenCV ----------
# OpenCV 通过 JNI 按类名/方法名反射查找，必须整包保留
-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** { *; }

# ---------- WebView JS 接口 ----------
# @JavascriptInterface 暴露给网页的方法不能被混淆/删除
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepattributes Annotation

# ---------- WebView 相关 ----------
-keep class android.webkit.** { *; }
-dontwarn android.webkit.**

# ---------- Kotlin / 协程 ----------
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---------- ML Kit ----------
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ---------- 保留行号，便于回读堆栈 ----------
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# OpenCV 的 CameraBridgeViewBase 引用了不存在的 R$styleable（AAR 裁剪掉了资源），忽略即可
-dontwarn org.opencv.**
-dontwarn org.opencv.android.**
