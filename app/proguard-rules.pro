# Proguard rules for Victus Tune
-keepattributes *Annotation*
-dontwarn androidx.media3.**

# NewPipeExtractor Rhino engine
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**
