# ProGuard rules for :scratch library release builds only.
# Consumer apps receive consumer-rules.pro via the AAR.

-include consumer-rules.pro

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

-dontwarn com.skretch.scratch.**
