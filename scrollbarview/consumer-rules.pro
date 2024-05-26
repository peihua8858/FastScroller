# EnclosingMethod is required to use InnerClasses.
-keepattributes InnerClasses,EnclosingMethod
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
#--- For:保持自定义控件类不被混淆 ---
-keep public class * extends android.view.View{
     *** get*();
     void set*(***);
     public <init>(android.content.Context);
     public <init>(android.content.Context, android.util.AttributeSet);
     public <init>(android.content.Context, android.util.AttributeSet, int);
 }
-keepclasseswithmembers class * {
     public <init>(android.content.Context, android.util.AttributeSet);
     public <init>(android.content.Context, android.util.AttributeSet, int);
 }
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public <init>(android.content.Context, android.util.AttributeSet);
 }
-keep public class * extends  android.graphics.drawable.Drawable{
     *** get*();
     void set*(***);
 }