package com.wxt.library.crash.implement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wxt.library.R;
import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.crash.CrashParams;
import com.wxt.library.crash.util.LogMember;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.SharedPreferenceUtil;
import com.wxt.library.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.wxt.library.contanst.Constant.CrashKey.INNER_APP_NET;
import static com.wxt.library.contanst.Constant.CrashKey.INNER_APP_TIME;
import static com.wxt.library.contanst.Constant.CrashKey.INNER_BUG_STYLE;
import static com.wxt.library.contanst.Constant.SharedPreferenceFileName.CRASH_PARAMS_FILE;

/**
 * 全局异常捕获，并将异常写入指定的文件
 *
 * @author SunFangTao
 * @Date 2014-5-30
 */
@SuppressLint({"SimpleDateFormat", "NewApi"})
public final class CrashHandlerImplement implements UncaughtExceptionHandler {

    private static final int HANDLE_TIME = 3000;
    private static final String NullPointerException = "1";
    private static final String IndexOutOfBoundsException = "2";
    private static final String ArithmeticException = "3";
    private static final String ClassCastException = "4";
    private static final String RuntimeException = "5";
    private static final String OutOfMemoryError = "6";
    private static final String IllegalArgumentException = "7";
    private static final String OtherError = "-1";

    private UncaughtExceptionHandler mDefaultHandler;
    private Context context;
    private MyHandler exitHandler;
    /**
     * 用来存储设备信息和异常信息
     */
    private Map<String, String> infos = new HashMap<String, String>();
    /**
     * 错误日志的路径
     */
    private String logPath;
    /**
     * 软件版本
     */
    private String appVersion;

    public CrashHandlerImplement(Context context) {
        SharedPreferenceUtil.getInstance(context).removeParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsConfirmDialog());
        this.context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT > 19) {
            this.logPath = Util.getFilePath(context, "crashLog");
        } else {
            this.logPath = Util.getPicPath(context, "crashLog");
        }
        this.appVersion = Util.getAppVersion(context);

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                //主线程异常拦截
//                while (true) {
//                    try {
//                        Looper.loop();//主线程的异常会从这里抛出
//                    } catch (Throwable e) {
////                        handlerCrash(Thread.currentThread(), e);
//                    }
//                }
//            }
//        });

        //子线程异常拦截
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void cancel() {
        Thread.setDefaultUncaughtExceptionHandler(null);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        handlerCrash(thread, ex);
    }

    private void handlerCrash(final Thread thread, final Throwable ex) {
        if (!handlerException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            // 自己处理
            try {
                // 使用Dialog来显示异常信息
                final int threadId = android.os.Process.myPid();

                new Thread() {
                    @Override
                    public void run() {
                        Looper.prepare();

                        synchronized (CrashHandlerImplement.class) {
                            boolean isHandler = SharedPreferenceUtil.getInstance(context).readBooleanParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsConfirmDialog(), false);
                            if (isHandler) {
                                return;
                            } else {
                                SharedPreferenceUtil.getInstance(context).saveParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsConfirmDialog(), true);

                                // 自定义土司显示位置
                                Toast toast = new Toast(context);
                                View layout = View.inflate(context, R.layout.activity_crash_toast, null);

                                TextView messageTV = layout.findViewById(R.id.crashDialogMessageTV);
                                messageTV.setText("我们已记录该错误并在第一时间进行修复。对您造成的不便敬请谅解！\n\n" + Util.getApplicationName(context) + "即将退出！\n");

                                layout.findViewById(R.id.finishCloseBtn).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });

                                // 设置toast文本，把设置好的布局传进来
                                toast.setView(layout);
                                // 设置土司显示在屏幕的位置
                                toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
                                toast.setDuration(Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }

                        Looper.loop();
                    }
                }.start();

                SystemClock.sleep(HANDLE_TIME);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mDefaultHandler.uncaughtException(thread, ex);
                } else {
                    ((Activity) context).finish();
                    System.exit(10);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    android.os.Process.killProcess(threadId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showCrashDialog(final int threadId, final Thread thread, final Throwable ex) {

        final Dialog dialog = new AlertDialog.Builder(context).setTitle("未知错误").setIcon(R.mipmap.ic_launcher_round)
                .setMessage("我们已记录该错误并在第一时间进行修复。对您造成的不便敬请谅解！\n\n" + Util.getApplicationName(context) + "即将退出！\n")
                .create();
        dialog.setCanceledOnTouchOutside(false);
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                SharedPreferenceUtil.getInstance(context).saveParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsExitByCrash(), true);
//                if (exitHandler != null)
//                    exitHandler.cancle();
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    mDefaultHandler.uncaughtException(thread, ex);
//                } else {
//                    ((Activity) context).finish();
//                    System.exit(10);
//                    android.os.Process.killProcess(android.os.Process.myPid());
//                    android.os.Process.killProcess(threadId);
//                }
//            }
//        });

        dialog.show();

//        exitHandler = new MyHandler(HANDLE_TIME) {
//            @Override
//            public void run() {
//                if (exitHandler != null)
//                    exitHandler.cancle();
//                dialog.dismiss();
//            }
//        };
    }

    /**
     * 异常处理了返回true
     *
     * @param ex
     * @return
     */
    private boolean handlerException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        // 收集设备参数信息
        collectDeviceInfo(context);
        // 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private void collectDeviceInfo(Context ctx) {
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            }
        } catch (Exception e) {

        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     * @throws Exception
     */
    private void saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        sb.append("System Version:=" + Build.VERSION.RELEASE + "\n");
        sb.append("App Version:=" + this.appVersion + "\n");
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        sb.append("------------------Log日志-----------------------").append("\n");
        String log = null;
        while (!TextUtils.isEmpty(log = LogMember.getInstance().get())) {
            sb.append(log);
        }
        sb.append("------------------Log日志-----------------------").append("\n");

        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        int index = result.indexOf("Caused by:");
        String tempExceptionString = result.substring((Math.max(index, 0)));

        String statusCode = "";
        if (tempExceptionString.contains("NullPointerException")) {
            statusCode = NullPointerException;
        } else if (tempExceptionString.contains("IndexOutOfBoundsException")) {
            statusCode = IndexOutOfBoundsException;
        } else if (tempExceptionString.contains("RuntimeException")) {
            statusCode = RuntimeException;
        } else if (tempExceptionString.contains("ArithmeticException")) {
            statusCode = ArithmeticException;
        } else if (tempExceptionString.contains("ClassCastException")) {
            statusCode = ClassCastException;
        } else if (tempExceptionString.contains("OutOfMemoryError")) {
            statusCode = OutOfMemoryError;
        } else if (tempExceptionString.contains("IllegalArgumentException")) {
            statusCode = IllegalArgumentException;
        } else {
            statusCode = OtherError;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        String fileName = format.format(new Date()) + ".txt";
        File file = new File(logPath + File.separator);
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            final File logFile = new File(logPath + File.separator + fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(logFile);
            OutputStreamWriter pw = new OutputStreamWriter(fos, "UTF-8");
            pw.write(sb.toString());
            pw.write("\r\n");
            pw.close();

            // 修改当前的网络类型
            CrashParams.getInstance(context.getApplicationContext()).put(INNER_APP_NET, Util.getNetStyle(context));
            CrashParams.getInstance(context.getApplicationContext()).put(INNER_APP_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            CrashParams.getInstance(context.getApplicationContext()).put(INNER_BUG_STYLE, statusCode);

            Map<String, Object> requestParams = new HashMap<>();
            for (String key : CrashParams.getInstance(context.getApplicationContext()).getCrashMap().keySet()) {
                requestParams.put(key, CrashParams.getInstance(context.getApplicationContext()).getCrashMap().get(key));
            }
            requestParams.put("file", logFile);
            String url = Util.getMetaValue(context, Constant.MetaKey.URL);
            String path = Util.getMetaValue(context, Constant.MetaKey.UPLOAD_URL);

            HttpUtil.getInstance().sendPost(null, Constant.HttpPrivateKey.AUTO_UPLOAD, url + path, requestParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
