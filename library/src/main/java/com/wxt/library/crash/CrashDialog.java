package com.wxt.library.crash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.contanst.Constant;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CrashDialog extends Activity {

    private MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_toast);
        CrashDialog.this.setFinishOnTouchOutside(false);

        Map<String, Object> requestParams = new HashMap<>();
        for (String key : CrashParams.getInstance(getApplicationContext()).getCrashMap().keySet()) {
            requestParams.put(key, CrashParams.getInstance(getApplicationContext()).getCrashMap().get(key));
        }
        requestParams.put("file", new File(getIntent().getStringExtra("logFilePath")));
        String url = Util.getMetaValue(this, Constant.MetaKey.URL);
        String path = Util.getMetaValue(this, Constant.MetaKey.UPLOAD_URL);

        Util.print("url + path=" + url + path);
        for (String key : requestParams.keySet()) {
            Util.print("key=" + key + " value=" + requestParams.get(key));
        }
        HttpUtil.getInstance().sendPost(null, Constant.HttpPrivateKey.AUTO_UPLOAD, url + path, requestParams);

        ((TextView) findViewById(R.id.crashDialogMessageTV)).setText("我们已记录该错误并在第一时间进行修复。对您造成的不便敬请谅解！\n\n" + Util.getApplicationName(this) + "即将退出！\n");
        findViewById(R.id.finishCloseBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        findViewById(R.id.finishRestartBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });

        handler = new MyHandler(3000) {
            @Override
            public void run() {
                handler = null;
                exit();
            }
        };
    }

    private void exit() {
        finish();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void restart() {
        Intent intent = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        exit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.cancle();
        }
    }
}