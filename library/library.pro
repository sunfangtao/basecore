#-----------------------混淆参数配置----------------------------------
#不移除无用代码
-dontshrink
# 优化算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
# 指定优化次数，默认执行一次优化。多次优化可能会得到更好的结果。如果某次优化后没有变化，优化会自动结束。只有开启优化时可用。
-optimizationpasses 5
#
-useuniqueclassmembernames
# 混淆时不会产生大小写混合的类名。默认混淆后的类名可以包含大写及小写
-dontusemixedcaseclassnames
# 不混淆指定的包名。过滤器是由逗号分隔的包名列表。包名可以包含 ？、*、** 通配符，并且可以在包名前加上 ! 否定符。只有开启混淆时可用
#-keeppackagenames cn.sft.update,cn.sft.taghandler,cn.sft.base.*,cn.sft.listener,cn.sft.crash,cn.sft.http,cn.sft.http.parse,cn.sft.sqlhelper,cn.sft.util,cn.sft.view,cn.sft.tree,cn.sft.retention
# 指定不对处理后的类文件进行预校验。默认情况下如果类文件的目标平台是 Java Micro Edition 或 Java 6 或更高时会进行预校验。目标平台是 Android 时没必要开启，关闭可减少处理时间。
-dontpreverify
# 指定处理期间打印更多相关信息。
# -verbose
#-----------------------混淆参数配置-------------------------------------

#-----------------------第三方混淆配置ok----------------------------------
#说明：Gson
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
#说明：Glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
#说明：BGABanner
-dontwarn cn.bingoogolapple.**
-keep class cn.bingoogolapple.**{*;}
#说明：okhttp,okio
-dontwarn okio.**,okhttp3.**
-keep class okhttp3.**{*;}
-keep class okio.**{*;}
#说明：高德地图
    #3D 地图 V5.0.0之后：
-keep   class com.amap.api.maps.**{*;}
-keep   class com.autonavi.**{*;}
-keep   class com.amap.api.trace.**{*;}
    #定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
    #搜索
-keep   class com.amap.api.services.**{*;}
    #2D地图
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}
    #导航
-keep class com.amap.api.navi.**{*;}
-keep class com.autonavi.**{*;}
# 讯飞语音
-dontwarn com.iflytek.**
-keep class com.iflytek.** {*;}
# 银联
-dontwarn com.unionpay.**
-keep class com.unionpay.** {*;}
# 极光推送
-dontwarn cn.jpush.**
-keep class cn.jpush.** {*;}
#-----------------------第三方混淆配置----------------------------------

#-----------------------通用类混淆配置----------------------------------
#说明：混淆时的注解辅助类配置ok
#不混淆注解的类名
-keep @com.wxt.library.retention.NotProguard class *
#不混淆注解的类名
#不混淆注解的域和方法
-keepclassmembers class * {
    @com.wxt.library.retention.NotProguard <fields>;
    @com.wxt.library.retention.NotProguard <methods>;
}
#说明：对外开放Listener不混淆
-keep class com.wxt.library.listener.** {*;}
#说明：对外开放TagHandler不混淆,保留public方法
-keep class com.wxt.library.taghandler.** {
    public <methods>;
}
#说明：对外开放Util不混淆，保留所有类的public方法和publi域
-keep class com.wxt.library.util.** {
    public <methods>;
    public <fields>;
}
#说明：自定义View构造和public方法不混淆
-keep class com.wxt.library.view.** {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public <methods>;
}
#-----------------------通用类混淆配置----------------------------------

#-----------------------框架基类混淆配置----------------------------------
#说明：BaseAdapter
-keep class com.wxt.library.base.adapter.BaseAdapter {
    !private <fields>;
    !private <methods>;
}
-keep class * extends com.wxt.library.base.adapter.BaseAdapter {
    !private <fields>;
    !private <methods>;
}
#说明：BaseActivity
-keep class com.wxt.library.base.activity.BaseActivity {
     !private <fields>;
     !private <methods>;
}
-keep class * extends com.wxt.library.base.activity.BaseActivity {
    !private <fields>;
    !private <methods>;
}
#说明：BaseFragment
-keep class com.wxt.library.base.fragment.BaseFragment {
     !private <fields>;
     !private <methods>;
}
-keep class * extends com.wxt.library.base.fragment.BaseFragment {
     !private <fields>;
     !private <methods>;
}
#说明：BaseApplication
-keep class com.wxt.library.base.application.BaseApplication {
     !private <fields>;
     !private <methods>;
}
#-----------------------框架基类混淆配置----------------------------------

#-----------------------http混淆配置----------------------------------
#说明：HttpParseHelper和SimpleHttpParseListener需要对外使用，通过注解防止混淆
#     HttpUtil保留类名和public方法
#     其他全部混淆即可
-keep class com.wxt.library.http.HttpUtil {
    public <methods>;
}
#-----------------------http混淆配置----------------------------------

#-----------------------数据库简单操作混淆配置----------------------------------
#说明：DBVO,DBUtil需要对外使用(DBHelper操作的对象需要继承DBVO)，通过注解防止混淆
#     DBHelper保留类名和public方法
#     其他全部混淆即可
-keep class com.wxt.library.sql.DBHelper {
    public <methods>;
}
-keep class * extends com.wxt.library.sql.model.DBVO {*;}
#-----------------------数据库简单操作混淆配置----------------------------------

#-----------------------Crash捕获混淆配置----------------------------------
#说明：CrashParams可以使用户添加自定义上传的数据(默认包含了基本的上传数据)
#     CrashHandler用户开启和关闭Crash的捕获
#     LogMember用户记录用户的操作日志（开启并且Crash时进行上传，可配置记录日志的数量）
#     其他全部混淆即可
-keep class com.wxt.library.crash.CrashParams {
    public <methods>;
}
-keep class com.wxt.library.crash.util.LogMember {
    public <methods>;
}
#-----------------------Crash捕获混淆配置----------------------------------

#-----------------------分页树混淆配置----------------------------------
#说明：PageTreeUpdateListener需要实现
#     PageTreeHelper用于和tree进行交互
#     其他全部混淆即可
-keep class com.wxt.library.tree.listener.PageTreeUpdateListener {*;}
-keep class com.wxt.library.tree.pagetree.PageTreeHelper {
    !private <fields>;
    !private <methods>;
}
#-----------------------分页树混淆配置----------------------------------

#-----------------------代码运行与否无影响混淆配置----------------------------------
-assumenosideeffects public class cn.sft.util.Util {
    public static boolean isEmpty(java.lang.Object);
    public static boolean isUpdate(android.content.Context, java.lang.String);
    public static java.lang.String toDBC(java.lang.String);
    public static int getBluetoothState();
    public static boolean isBluetoothOpen();
    public static java.lang.String MD5(java.lang.String);
    public static java.lang.String getDeviceInfo();
    public static void print(java.lang.Object);
    public static java.lang.String getSDPath();
    public static boolean isMobileNO(java.lang.String);
    public static List<RunningServiceInfo> isRunningService(int);
    public static java.lang.String getMetaValue(java.lang.String);
    public static java.lang.String getAppVersion();
    public static Bitmap readBitMap(android.content.Context, int);
    public static java.lang.String getApplicationName();
    public static java.lang.String getNetStyle();
    public static int dp2px(android.content.Context, float);
    public static int sp2px(android.content.Context, float);
    public static float px2dp(android.content.Context, float);
    public static float px2sp(android.content.Context, float);
    public static android.graphics.Bitmap snapShotWithStatusBar(android.app.Activity);
    public static android.graphics.Bitmap snapShotWithoutStatusBar(android.app.Activity);
    public static int getVrtualBtnHeight(android.content.Context);
    private static int getDpi(android.app.Activity);
    private static int[] getScreenWH(android.content.Context);
    public static java.lang.String getPicPath(android.content.Context, java.lang.String);
    public static java.lang.String getFilePath(android.content.Context, java.lang.String);
    public static int getDpi(android.content.Context);
    public static int getBottomStatusHeight(android.content.Context) ;
    public static int getTitleHeight(android.app.Activity);
    public static int getStatusHeight(android.content.Context);

}
#-----------------------代码运行与否无影响混淆配置----------------------------------

#-----------------------通用混淆配置----------------------------------
# 保留R下面的资源
-keep class **.R$* {*;}
# 保留Parcelable序列化类不被混淆
-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
# TODO 说明：注解
# 避免混淆泛型
-keepattributes Signature

# Keep names - Native method names. Keep all native class/method names.
#-keepclasseswithmembers,allowshrinking class *,*,* {
#    native <methods>;
#}

#-keepclassmembers,allowshrinking class * {
#    java.lang.Class class$(java.lang.String);
#    java.lang.Class class$(java.lang.String,boolean);
#}
# 说明：枚举
-keepclassmembers enum  *,* {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Remove - System method calls. Remove all invocations of System
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.System {
    public static long currentTimeMillis();
    static java.lang.Class getCallerClass();
    public static int identityHashCode(java.lang.Object);
    public static java.lang.SecurityManager getSecurityManager();
    public static java.util.Properties getProperties();
    public static java.lang.String getProperty(java.lang.String);
    public static java.lang.String getenv(java.lang.String);
    public static java.lang.String mapLibraryName(java.lang.String);
    public static java.lang.String getProperty(java.lang.String,java.lang.String);
}

-assumenosideeffects public class java.lang.Math {
    public static double sin(double);
    public static double cos(double);
    public static double tan(double);
    public static double asin(double);
    public static double acos(double);
    public static double atan(double);
    public static double toRadians(double);
    public static double toDegrees(double);
    public static double exp(double);
    public static double log(double);
    public static double log10(double);
    public static double sqrt(double);
    public static double cbrt(double);
    public static double IEEEremainder(double,double);
    public static double ceil(double);
    public static double floor(double);
    public static double rint(double);
    public static double atan2(double,double);
    public static double pow(double,double);
    public static int round(float);
    public static long round(double);
    public static double random();
    public static int abs(int);
    public static long abs(long);
    public static float abs(float);
    public static double abs(double);
    public static int max(int,int);
    public static long max(long,long);
    public static float max(float,float);
    public static double max(double,double);
    public static int min(int,int);
    public static long min(long,long);
    public static float min(float,float);
    public static double min(double,double);
    public static double ulp(double);
    public static float ulp(float);
    public static double signum(double);
    public static float signum(float);
    public static double sinh(double);
    public static double cosh(double);
    public static double tanh(double);
    public static double hypot(double,double);
    public static double expm1(double);
    public static double log1p(double);
}

-assumenosideeffects public class java.lang.* extends java.lang.Number {
    public static java.lang.String toString(byte);
    public static java.lang.Byte valueOf(byte);
    public static byte parseByte(java.lang.String);
    public static byte parseByte(java.lang.String,int);
    public static java.lang.Byte valueOf(java.lang.String,int);
    public static java.lang.Byte valueOf(java.lang.String);
    public static java.lang.Byte decode(java.lang.String);
    public int compareTo(java.lang.Byte);
    public static java.lang.String toString(short);
    public static short parseShort(java.lang.String);
    public static short parseShort(java.lang.String,int);
    public static java.lang.Short valueOf(java.lang.String,int);
    public static java.lang.Short valueOf(java.lang.String);
    public static java.lang.Short valueOf(short);
    public static java.lang.Short decode(java.lang.String);
    public static short reverseBytes(short);
    public int compareTo(java.lang.Short);
    public static java.lang.String toString(int,int);
    public static java.lang.String toHexString(int);
    public static java.lang.String toOctalString(int);
    public static java.lang.String toBinaryString(int);
    public static java.lang.String toString(int);
    public static int parseInt(java.lang.String,int);
    public static int parseInt(java.lang.String);
    public static java.lang.Integer valueOf(java.lang.String,int);
    public static java.lang.Integer valueOf(java.lang.String);
    public static java.lang.Integer valueOf(int);
    public static java.lang.Integer getInteger(java.lang.String);
    public static java.lang.Integer getInteger(java.lang.String,int);
    public static java.lang.Integer getInteger(java.lang.String,java.lang.Integer);
    public static java.lang.Integer decode(java.lang.String);
    public static int highestOneBit(int);
    public static int lowestOneBit(int);
    public static int numberOfLeadingZeros(int);
    public static int numberOfTrailingZeros(int);
    public static int bitCount(int);
    public static int rotateLeft(int,int);
    public static int rotateRight(int,int);
    public static int reverse(int);
    public static int signum(int);
    public static int reverseBytes(int);
    public int compareTo(java.lang.Integer);
    public static java.lang.String toString(long,int);
    public static java.lang.String toHexString(long);
    public static java.lang.String toOctalString(long);
    public static java.lang.String toBinaryString(long);
    public static java.lang.String toString(long);
    public static long parseLong(java.lang.String,int);
    public static long parseLong(java.lang.String);
    public static java.lang.Long valueOf(java.lang.String,int);
    public static java.lang.Long valueOf(java.lang.String);
    public static java.lang.Long valueOf(long);
    public static java.lang.Long decode(java.lang.String);
    public static java.lang.Long getLong(java.lang.String);
    public static java.lang.Long getLong(java.lang.String,long);
    public static java.lang.Long getLong(java.lang.String,java.lang.Long);
    public static long highestOneBit(long);
    public static long lowestOneBit(long);
    public static int numberOfLeadingZeros(long);
    public static int numberOfTrailingZeros(long);
    public static int bitCount(long);
    public static long rotateLeft(long,int);
    public static long rotateRight(long,int);
    public static long reverse(long);
    public static int signum(long);
    public static long reverseBytes(long);
    public int compareTo(java.lang.Long);
    public static java.lang.String toString(float);
    public static java.lang.String toHexString(float);
    public static java.lang.Float valueOf(java.lang.String);
    public static java.lang.Float valueOf(float);
    public static float parseFloat(java.lang.String);
    public static boolean isNaN(float);
    public static boolean isInfinite(float);
    public static int floatToIntBits(float);
    public static int floatToRawIntBits(float);
    public static float intBitsToFloat(int);
    public static int compare(float,float);
    public boolean isNaN();
    public boolean isInfinite();
    public int compareTo(java.lang.Float);
    public static java.lang.String toString(double);
    public static java.lang.String toHexString(double);
    public static java.lang.Double valueOf(java.lang.String);
    public static java.lang.Double valueOf(double);
    public static double parseDouble(java.lang.String);
    public static boolean isNaN(double);
    public static boolean isInfinite(double);
    public static long doubleToLongBits(double);
    public static long doubleToRawLongBits(double);
    public static double longBitsToDouble(long);
    public static int compare(double,double);
    public boolean isNaN();
    public boolean isInfinite();
    public int compareTo(java.lang.Double);
    public <init>(byte);
    public <init>(short);
    public <init>(int);
    public <init>(long);
    public <init>(float);
    public <init>(double);
    public <init>(java.lang.String);
    public byte byteValue();
    public short shortValue();
    public int intValue();
    public long longValue();
    public float floatValue();
    public double doubleValue();
    public int compareTo(java.lang.Object);
    public boolean equals(java.lang.Object);
    public int hashCode();
    public java.lang.String toString();
}

-assumenosideeffects public class java.lang.String {
    public <init>();
    public <init>(byte[]);
    public <init>(byte[],int);
    public <init>(byte[],int,int);
    public <init>(byte[],int,int,int);
    public <init>(byte[],int,int,java.lang.String);
    public <init>(byte[],java.lang.String);
    public <init>(char[]);
    public <init>(char[],int,int);
    public <init>(java.lang.String);
    public <init>(java.lang.StringBuffer);
    public static java.lang.String copyValueOf(char[]);
    public static java.lang.String copyValueOf(char[],int,int);
    public static java.lang.String valueOf(boolean);
    public static java.lang.String valueOf(char);
    public static java.lang.String valueOf(char[]);
    public static java.lang.String valueOf(char[],int,int);
    public static java.lang.String valueOf(double);
    public static java.lang.String valueOf(float);
    public static java.lang.String valueOf(int);
    public static java.lang.String valueOf(java.lang.Object);
    public static java.lang.String valueOf(long);
    public boolean contentEquals(java.lang.StringBuffer);
    public boolean endsWith(java.lang.String);
    public boolean equalsIgnoreCase(java.lang.String);
    public boolean equals(java.lang.Object);
    public boolean matches(java.lang.String);
    public boolean regionMatches(boolean,int,java.lang.String,int,int);
    public boolean regionMatches(int,java.lang.String,int,int);
    public boolean startsWith(java.lang.String);
    public boolean startsWith(java.lang.String,int);
    public byte[] getBytes();
    public byte[] getBytes(java.lang.String);
    public char charAt(int);
    public char[] toCharArray();
    public int compareToIgnoreCase(java.lang.String);
    public int compareTo(java.lang.Object);
    public int compareTo(java.lang.String);
    public int hashCode();
    public int indexOf(int);
    public int indexOf(int,int);
    public int indexOf(java.lang.String);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(int);
    public int lastIndexOf(int,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.CharSequence subSequence(int,int);
    public java.lang.String concat(java.lang.String);
    public java.lang.String replaceAll(java.lang.String,java.lang.String);
    public java.lang.String replace(char,char);
    public java.lang.String replaceFirst(java.lang.String,java.lang.String);
    public java.lang.String[] split(java.lang.String);
    public java.lang.String[] split(java.lang.String,int);
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
    public java.lang.String toLowerCase();
    public java.lang.String toLowerCase(java.util.Locale);
    public java.lang.String toString();
    public java.lang.String toUpperCase();
    public java.lang.String toUpperCase(java.util.Locale);
    public java.lang.String trim();
}

-assumenosideeffects public class java.lang.StringBuffer {
    public <init>();
    public <init>(int);
    public <init>(java.lang.String);
    public <init>(java.lang.CharSequence);
    public java.lang.String toString();
    public char charAt(int);
    public int capacity();
    public int codePointAt(int);
    public int codePointBefore(int);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
}

-assumenosideeffects public class java.lang.StringBuilder {
    public <init>();
    public <init>(int);
    public <init>(java.lang.String);
    public <init>(java.lang.CharSequence);
    public java.lang.String toString();
    public char charAt(int);
    public int capacity();
    public int codePointAt(int);
    public int codePointBefore(int);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
}
#-----------------------通用捕获混淆配置----------------------------------