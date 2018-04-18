package com.wxt.library.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.wxt.library.listener.APPUpdateListener;
import com.wxt.library.retention.NotProguard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * 更新管理器
 *
 * @author Administrator
 */
@SuppressLint("HandlerLeak")
public class UpdateManager {

    private Context context;
    // 安装包下载url
    private String apkUrl;

    private Dialog downloadDialog;
    // 下载文件存储路径
    private String saveFilePath;
    // 下载进度
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int DOWN_ERROR = 3;

    private int progress;

    private Thread downLoadThread;

    private boolean interceptFlag = false;

    private APPUpdateListener listener;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                case DOWN_ERROR:
                    failDownload();
                    break;
                default:
                    break;
            }
        }
    };

    @NotProguard
    public UpdateManager(Context context, String apkUrl, APPUpdateListener listener) {
        this.context = context;
        this.listener = listener;
        if (Build.VERSION.SDK_INT > 19) {
            this.saveFilePath = Util.getFilePath(context, "apk");
        } else {
            this.saveFilePath = Util.getPicPath(context, "apk");
        }
        this.apkUrl = apkUrl;
    }

    @NotProguard
    public void showDefaultDownloadDialog() {
        Builder builder = new Builder(context);
        builder.setTitle("软件版本更新");
        mProgress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mProgress.setLayoutParams(params);
        builder.setView(mProgress);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
                // 取消下载更新安装包 do more
                // 删除已下载的安装包
                deleteApk();
                if (listener != null) {
                    listener.downloadCancel();
                }
            }
        });
        downloadDialog = builder.create();
        downloadDialog.setCancelable(false);
        downloadDialog.show();
        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(saveFilePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String apkFileName = Util.getApplicationName(context) + ".apk";
                File ApkFile = new File(saveFilePath + File.separator + apkFileName);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载

                fos.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                // 下载完成通知安装
                mHandler.sendEmptyMessage(DOWN_ERROR);
            }
        }
    };

    /**
     * 下载apk
     */
    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    private void failDownload() {
        if (downloadDialog != null) {
            downloadDialog.dismiss();
        }
        if (listener != null) {
            listener.downloadFail();
        }
    }

    /**
     * 安装apk
     */
    private void installApk() {
        if (listener != null) {
            listener.successDownload();
        }
        if (downloadDialog != null) {
            downloadDialog.dismiss();
        }
        File apkfile = new File(saveFilePath + File.separator + Util.getApplicationName(context) + ".apk");

        if (!apkfile.exists()) {
            return;
        }

        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT > 23) {
            /**Android 7.0以上的方式**/
            Uri contentUri = getUriForFile(context, context.getPackageName() + ".fileprovider", apkfile);
//            context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        }
        context.startActivity(i);
    }

    /**
     * 删除下载的apk
     */
    private void deleteApk() {
        FileUtils.deleteFile(new File(saveFilePath));
    }
}
