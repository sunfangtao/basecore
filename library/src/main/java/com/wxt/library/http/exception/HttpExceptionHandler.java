package com.wxt.library.http.exception;

import android.content.Context;
import android.text.TextUtils;

import com.wxt.library.contanst.Constant;
import com.wxt.library.crash.CrashParams;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.http.parse.HttpParseHelper;
import com.wxt.library.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.wxt.library.contanst.Constant.CrashKey.INNER_APP_NET;
import static com.wxt.library.contanst.Constant.CrashKey.INNER_APP_TIME;
import static com.wxt.library.contanst.Constant.CrashKey.INNER_BUG_STYLE;

public class HttpExceptionHandler {

    private Context context;

    public HttpExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    private final String saveHttpException2File(String content, int statusCode) {

        StringBuffer sb = new StringBuffer();
        sb.append("System Version:=" + android.os.Build.VERSION.RELEASE + "\n");
        sb.append("App Version:=" + Util.getAppVersion(context) + "\n");

        sb.append("请求参数：").append("\n");
        // 修改当前的网络类型
        CrashParams.getInstance(context.getApplicationContext()).put(INNER_APP_NET, Util.getNetStyle(context));
        CrashParams.getInstance(context.getApplicationContext()).put(INNER_APP_TIME, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        CrashParams.getInstance(context.getApplicationContext()).put(INNER_BUG_STYLE, statusCode + "");

        String appId = Util.getMetaValue(context, Constant.MetaKey.APP_ID);
        if (TextUtils.isEmpty(appId)) {
            // 处理异常，必须设置APP_ID
            return null;
        } else {
            CrashParams.getInstance(context.getApplicationContext()).put(Constant.CrashKey.APP_ID, appId);
        }

        for (String key : CrashParams.getInstance(context.getApplicationContext()).getCrashMap().keySet()) {
            sb.append("key=" + key + " value=" + CrashParams.getInstance(context.getApplicationContext()).getCrashMap().get(key)).append("\n");
        }
        sb.append(content);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        String fileName = format.format(new Date()) + ".txt";
        String logPath = Util.getPicPath(context, "crashLog");
        File file = new File(logPath + File.separator);
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            File logFile = new File(logPath + File.separator + fileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(logFile);
            OutputStreamWriter pw = new OutputStreamWriter(fos, "UTF-8");
            pw.write(sb.toString());
            pw.write("\r\n");
            pw.close();

            Map<String, Object> requestParams = new HashMap<>();
            for (String key : CrashParams.getInstance(context.getApplicationContext()).getCrashMap().keySet()) {
                requestParams.put(key, CrashParams.getInstance(context.getApplicationContext()).getCrashMap().get(key));
            }

            requestParams.put("file", logFile);
            String url = Util.getMetaValue(context, Constant.MetaKey.URL);
            String path = Util.getMetaValue(context, Constant.MetaKey.UPLOAD_URL);

            HttpUtil.getInstance().sendPost(null, Constant.HttpPrivateKey.AUTO_UPLOAD, url + path, requestParams);

            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final void onHttpFail(HttpParseHelper.ReturnObject returnObject) {
        StringBuffer sb = new StringBuffer();
        sb.append("操作类型(http请求的类型)：").append(returnObject.httpType).append("\n");
        sb.append("状态码：").append(returnObject.stateCode).append("\n");
        if (returnObject.stateCode == 500) {
            sb.append("服务器发生异常").append("\n");

        } else if (returnObject.stateCode == 0) {
            if (!Util.getNetStyle(context).equals("no net")) {
                sb.append("与服务器失去连接");
            }
        } else if (returnObject.stateCode == 404) {
            sb.append("请求的连接不存在").append("\n");
        } else {
            sb.append("其他异常状态码").append("\n");
        }

        sb.append("url=" + returnObject.url).append("\n");

        saveHttpException2File(sb.toString(), returnObject.stateCode);
    }
}
