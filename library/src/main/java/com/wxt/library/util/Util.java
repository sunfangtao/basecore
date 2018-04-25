package com.wxt.library.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.SparseLongArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.wxt.library.base.activity.BaseActivity;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.crash.util.LogMember;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static int viewId = 10000;
    private static boolean isDebug = false;

    /**
     * 判断对象是否为空
     *
     * @param obj 对象
     * @return {@code true}: 为空<br>{@code false}: 不为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String && obj.toString().length() == 0) {
            return true;
        }
        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return true;
        }
        if (obj instanceof Collection && ((Collection) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof Map && ((Map) obj).isEmpty()) {
            return true;
        }
        if (obj instanceof SparseArray && ((SparseArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseBooleanArray && ((SparseBooleanArray) obj).size() == 0) {
            return true;
        }
        if (obj instanceof SparseIntArray && ((SparseIntArray) obj).size() == 0) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (obj instanceof SparseLongArray && ((SparseLongArray) obj).size() == 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUpdate(Context context, String version) {
        String[] oldVersion = Util.getAppVersion(context).replace(".", "@").split("@");
        String[] newVersion = version.replace(".", "@").split("@");
        int length = oldVersion.length;
        for (int i = 0; i < length; i++) {
            if (Integer.valueOf(newVersion[i]) > Integer.valueOf(oldVersion[i])) {
                return true;
            } else if (Integer.valueOf(newVersion[i]) > Integer.valueOf(oldVersion[i])) {
                return false;
            }
        }
        if (newVersion.length > oldVersion.length) {
            return true;
        }
        return false;
    }

    /**
     * 针对TextView显示中文中出现的排版错乱问题，通过调用此方法得以解决
     *
     * @param str
     * @return 返回全部为全角字符的字符串
     */
    public static String toDBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }

        }
        return new String(c);
    }

    /**
     * 关闭所有Activity,当前Activity需要手动调用finish()关闭
     *
     * @param context
     */
    public static void setAppExit(Context context) {
        SharedPreferenceUtil.getInstance(context).saveParam(ConstantMethod.getInstance(context.getApplicationContext()).getIsExitKey(), true);
    }

    /**
     * 获取蓝牙的状态
     *
     * @return 取值为BluetoothAdapter的四个静态字段：STATE_OFF, STATE_TURNING_OFF,
     * STATE_ON, STATE_TURNING_ON
     * @throws Exception 没有找到蓝牙设备
     */
    public static int getBluetoothState() throws Exception {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new Exception("bluetooth device not found!");
        } else {
            return bluetoothAdapter.getState();
        }
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return true：已经打开或者正在打开；false：已经关闭或者正在关闭 没有找到蓝牙设备
     */
    public static boolean isBluetoothOpen() throws Exception {
        int bluetoothStateCode = getBluetoothState();
        return bluetoothStateCode == BluetoothAdapter.STATE_ON
                || bluetoothStateCode == BluetoothAdapter.STATE_TURNING_ON;
    }

    /**
     * 设置蓝牙状态
     *
     * @param enable 打开 没有找到蓝牙设备
     */
    public static void setBluetooth(boolean enable) throws Exception {
        // 如果当前蓝牙的状态与要设置的状态不一样
        if (isBluetoothOpen() != enable) {
            // 如果是要打开就打开，否则关闭
            if (enable) {
                BluetoothAdapter.getDefaultAdapter().enable();
            } else {
                BluetoothAdapter.getDefaultAdapter().disable();
            }
        }
    }

    /**
     * 开始导出数据 此操作比较耗时,建议在线程中进行
     *
     * @param targetFile   目标文件
     * @param databaseName 要拷贝的数据库文件名
     * @return 是否倒出成功
     */
    public static boolean startExportDatabase(Context context, String targetFile, String databaseName) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return false;
        }
        String sourceFilePath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/"
                + databaseName;
        String destFilePath = Environment.getExternalStorageDirectory()
                + (TextUtils.isEmpty(targetFile) ? (databaseName + ".db") : targetFile);
        boolean isCopySuccess = FileUtils.copyFile(sourceFilePath, destFilePath);
        return isCopySuccess;
    }

    public static String MD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }

            return buf.toString();// 32位的加密
            // return buf.toString().substring(8, 24);// 16位的加密

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDeviceInfo() {
        Map<String, String> infos = new HashMap<String, String>();
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String info = infos.get("BRAND") + " " + infos.get("MODEL");
        return info;
    }

    /**
     * 检测应用是否处于debug模式。
     */
    public static boolean isApkDebugable(Context context, String packageName) {
        try {
            PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
                    packageName, PackageManager.GET_ACTIVITIES);
            if (pkginfo != null) {
                ApplicationInfo info = pkginfo.applicationInfo;
                isDebug = ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            }
        } catch (Exception e) {

        }
        return isDebug;
    }

    public static void print(Object str, boolean isShowPrintLineNumber) {
        if (isDebug) {
            if (isShowPrintLineNumber) {
                Log.e("LOG_", (str == null ? "输入对象为空" : str) + getTraceElement());
            } else {
                Log.e("LOG_", str == null ? "输入对象为空" : str.toString());
            }
        }
        LogMember.getInstance().add(str == null ? "输入对象为空" : str.toString());
    }

    public static void print(Object str) {
        print(str, true);
    }

    /**
     * 可显示调用方法所在的文件行号，在AndroidStudio的logcat处可点击定位。
     * 此方法参考：https://github.com/orhanobut/logger
     */
    private static String getTraceElement() {
        try {
            int methodCount = 2;
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            int stackOffset = _getStackOffset(trace);

            // corresponding method count with the current stack may exceeds the
            // stack trace. Trims the count
            if (methodCount + stackOffset > trace.length) {
                methodCount = trace.length - stackOffset - 1;
            }

            String level = "    ";
            StringBuilder builder = new StringBuilder();
            for (int i = methodCount; i > 0; i--) {
                int stackIndex = i + stackOffset;
                if (stackIndex >= trace.length) {
                    continue;
                }

                if (trace[stackIndex].getFileName() == null || trace[stackIndex].getLineNumber() == -1) {
                    continue;
                } else {
                    builder.append("\n").append(level).append(_getSimpleClassName(trace[stackIndex].getClassName()))
                            .append(".").append(trace[stackIndex].getMethodName()).append(" ").append("(")
                            .append(trace[stackIndex].getFileName()).append(":").append(trace[stackIndex].getLineNumber())
                            .append(")");
                    level += "    ";
                }
            }
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Determines the starting index of the stack trace, after method calls made
     * by this class.
     *
     * @param trace the stack trace
     * @return the stack offset
     */
    private static int _getStackOffset(StackTraceElement[] trace) {
        for (int i = 2; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(Util.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    private static String _getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    /**
     * 关闭输入法,返回输入法状态，true为开，即关闭失败
     *
     * @param context
     * @return
     */
    public static boolean hideInputMethod(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            return imm.isActive();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取根目录
        }
        return sdDir.toString();
    }

    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("((13[0-9])|(15[0-3,5-9])|(18[0-9])){1}\\d{8}");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    /**
     * 获取手机正在运行的服务信息
     *
     * @param size 指定获取的数目，如果小于0，则获取最大数目为30
     * @return 服务的列表
     */
    public static List<RunningServiceInfo> isRunningService(Context context, int size) {
        size = size < 0 ? 30 : size;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningServices(size);
    }

    // 获取AppKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (PackageManager.NameNotFoundException e) {

        }
        return apiKey;
    }

    public static String getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static String getApplicationName(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        return applicationName;
    }

    public static void drawableToFile(Drawable drawable, File path) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        bitmapToFile(bitmap, path);
    }

    public static void bitmapToFile(Bitmap bitmap, File path) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(path);
            fos.write(bitmapdata);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getNetStyle(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        if (info != null) {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                return "wifi";
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                return "mobile";
            } else {
                return "other";
            }
        }
        return "no net";
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     *
     * @param context
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal,
                context.getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    /**
     * px转sp
     *
     * @param context
     * @param pxVal
     * @return
     */
    public static float px2sp(Context context, float pxVal) {
        return (pxVal / context.getResources().getDisplayMetrics().scaledDensity);
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = activity.getResources().getDisplayMetrics().widthPixels;
        int height = activity.getResources().getDisplayMetrics().heightPixels;
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = activity.getResources().getDisplayMetrics().widthPixels;
        int height = activity.getResources().getDisplayMetrics().heightPixels;
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return bp;

    }

    private static int getDpi(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        int height = 0;
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            height = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return height;
    }

    @SuppressWarnings("deprecation")
    private static int[] getScreenWH(Context poCotext) {
        WindowManager wm = (WindowManager) poCotext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return new int[]{width, height};
    }

    public static int getVrtualBtnHeight(Context poCotext) {
        int location[] = getScreenWH(poCotext);
        int realHeiht = getDpi((Activity) poCotext);
        int virvalHeight = realHeiht - location[1];
        return virvalHeight;
    }

    public static Map<String, String> collectDeviceInfo(Context ctx) {
        Map<String, String> infos = new HashMap<String, String>();
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return infos;
    }

    /**
     * 获取相应的图片存储路径
     *
     * @param type
     * @return
     */
    public static String getPicPath(Context context, String type) {
        String basePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File file = new File(basePath + File.separator + type);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 获取相应的文件存储路径
     *
     * @param type
     * @return
     */
    @TargetApi(19)
    public static String getFilePath(Context context, String type) {
        String basePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        File file = new File(basePath + File.separator + type);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    //获取屏幕原始尺寸高度，包括虚拟功能键高度
    public static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {
        int totalHeight = getDpi(context);

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        int contentHeight = outMetrics.heightPixels;

        return totalHeight - contentHeight;
    }

    /**
     * 标题栏高度
     *
     * @return
     */
    public static int getTitleHeight(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    public static int getStatusHeight(Context context) {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    public static synchronized int getViewAutoId() {
        return (viewId++) % 10000 + 10000;
    }
}
