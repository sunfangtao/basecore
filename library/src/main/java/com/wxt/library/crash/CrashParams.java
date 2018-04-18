package com.wxt.library.crash;

import android.content.Context;

import com.wxt.library.R;
import com.wxt.library.contanst.Constant;
import com.wxt.library.retention.NotProguard;
import com.wxt.library.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/13.
 */
public final class CrashParams {

    private Map<String, String> crashMap;

    private static CrashParams crashParams;

    private CrashParams(Context context) {
        crashMap = new HashMap<>();
        crashMap.put(Constant.CrashKey.INNER_APP_STYLE, context.getString(R.string.android));
        crashMap.put(Constant.CrashKey.INNER_APP_NAME, Util.getApplicationName(context));
        crashMap.put(Constant.CrashKey.INNER_APP_ID, context.getPackageName());
        crashMap.put(Constant.CrashKey.INNER_APP_VERSION, Util.getAppVersion(context));
        crashMap.put(Constant.CrashKey.INNER_APP_NET, Util.getNetStyle(context));
        crashMap.put(Constant.CrashKey.INNER_APP_HADRWARE, Util.getDeviceInfo());
        crashMap.put(Constant.CrashKey.INNER_APP_HARDWARE_VERSION, android.os.Build.VERSION.RELEASE);
    }

    public final static CrashParams getInstance(Context context) {
        if (crashParams == null) {
            synchronized (CrashParams.class) {
                if (crashParams == null) {
                    crashParams = new CrashParams(context.getApplicationContext());
                }
            }
        }
        return crashParams;
    }

    public final void put(String key, String value) {
        crashMap.put(key, value);
    }

    public final Map<String, String> getCrashMap() {
        return crashMap;
    }
}
