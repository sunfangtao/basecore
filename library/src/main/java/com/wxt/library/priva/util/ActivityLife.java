package com.wxt.library.priva.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.wxt.library.R;
import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.crash.CrashParams;
import com.wxt.library.crash.implement.CrashHandlerImplement;
import com.wxt.library.crash.util.LogMember;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.http.listener.SimpleHttpParseListener;
import com.wxt.library.http.parse.HttpParseHelper;
import com.wxt.library.priva.listener.ActivityStateChangedListener;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.SharedPreferenceUtil;
import com.wxt.library.util.Util;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Response;

import static com.wxt.library.contanst.Constant.SharedPreferenceFileName.CRASH_PARAMS_FILE;
import static com.wxt.library.contanst.Constant.SharedPreferenceFileName.SHARE_FILE_NAME_FOR_SAVEINSTANCE;

/**
 * Created by Administrator on 2017/4/13.
 * BaseActivity中的findBeanForVO,onCreat中添加ActivityLife
 * CrashHandler中的init中添加ActivityLife
 */

public final class ActivityLife implements Application.ActivityLifecycleCallbacks {

    private Context context;
    private int activityCount = 0;
    private CrashHandlerImplement crashHandlerImplement;
    private Map<Activity, Dialog> dialogMap;
    private Dialog dialog;
    private MyHandler exitHandler;

    private static ActivityLife activityLife;

    public static ActivityLife getInstance(Context context) {
        if (activityLife == null) {
            synchronized (ActivityLife.class) {
                if (activityLife == null) {
                    activityLife = new ActivityLife(context.getApplicationContext());
                }
            }
        }
        return activityLife;
    }

    public ActivityLife(Context context) {
        this.context = context;
        dialogMap = new HashMap<Activity, Dialog>();
        Util.isApkDebugable(context, context.getPackageName());
        ((Application) context.getApplicationContext()).unregisterActivityLifecycleCallbacks(this);
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        boolean isHandleCrash = SharedPreferenceUtil.getInstance(activity).readBooleanParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(activity).getIsHandlerCrash(), false);
        if (crashHandlerImplement == null) {
            if (isHandleCrash) {
                // 需要拦截异常
                String appId = Util.getMetaValue(activity, Constant.MetaKey.APP_ID);
                if (TextUtils.isEmpty(appId)) {
                    // 处理异常，必须设置APP_ID
                    Toast.makeText(activity, activity.getString(R.string.no_appId), Toast.LENGTH_SHORT).show();
                    activity.finish();
                    return;
                } else {
                    CrashParams.getInstance(activity).put(Constant.CrashKey.APP_ID, appId);
                    crashHandlerImplement = new CrashHandlerImplement(activity);
                }
            }
        } else {
            if (!isHandleCrash) {
                crashHandlerImplement.cancel();
                crashHandlerImplement = null;
            }
        }

        if (activityCount++ == 0) {
            SharedPreferenceUtil.getInstance(activity).removeParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsExitByAuth());
            SharedPreferenceUtil.getInstance(activity).removeParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsDialogDismiss());
            SharedPreferenceUtil.getInstance(activity).removeParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getAppSession());
            SharedPreferenceUtil.getInstance(activity).removeParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context).getIsExitByCrash());
            LogMember.getInstance().init();
        }
        checkApp(activity);
        Set<ActivityStateChangedListener> listeners = ActivityChangedUtil.getInstance().getChangedListener();
        for (ActivityStateChangedListener listener : listeners) {
            listener.onActivityCreated(activity, savedInstanceState);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (crashHandlerImplement != null)
            crashHandlerImplement.setContext(activity);
        if (SharedPreferenceUtil.getInstance(activity).readBooleanParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context).getIsExitByCrash(), false)) {
            activity.finish();
        }
        if (SharedPreferenceUtil.getInstance(activity).readBooleanParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsExitByAuth(), false)) {
            if (SharedPreferenceUtil.getInstance(activity).readBooleanParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsDialogDismiss(), false)) {
                Dialog dialog = dialogMap.get(activity);
                if (dialog != null) {
                    dialog.dismiss();
                }
                activity.finish();
            } else {
                showDialog(activity);
            }
        }
        Set<ActivityStateChangedListener> listeners = ActivityChangedUtil.getInstance().getChangedListener();
        for (ActivityStateChangedListener listener : listeners) {
            listener.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Set<ActivityStateChangedListener> listeners = ActivityChangedUtil.getInstance().getChangedListener();
        for (ActivityStateChangedListener listener : listeners) {
            listener.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Set<ActivityStateChangedListener> listeners = ActivityChangedUtil.getInstance().getChangedListener();
        for (ActivityStateChangedListener listener : listeners) {
            listener.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

        Map<String, ?> keyMap = SharedPreferenceUtil.getInstance(activity).getAllShareKey(SHARE_FILE_NAME_FOR_SAVEINSTANCE);
        if (keyMap != null) {
            for (String keyStr : keyMap.keySet()) {
                if (keyStr.contains(activity.getClass().getName())) {
                    // 删除物理存储
                    SharedPreferenceUtil.getInstance(activity).removeParam(SHARE_FILE_NAME_FOR_SAVEINSTANCE, keyStr);
                }
            }
        }
        activityCount--;
        dialogMap.remove(activity);
        Set<ActivityStateChangedListener> listeners = ActivityChangedUtil.getInstance().getChangedListener();
        for (ActivityStateChangedListener listener : listeners) {
            listener.onActivityDestroyed(activity);
        }
    }

    private void checkApp(final Activity activity) {
        String session = SharedPreferenceUtil.getInstance(context).readStrParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getAppSession(), "");
        if (TextUtils.isEmpty(session)) {

            Map<String, String> r = new HashMap<>();
            r.put("name", context.getPackageName());
            String url = Util.getMetaValue(context, Constant.MetaKey.URL);
            String path = Util.getMetaValue(context, Constant.MetaKey.CHECK_URL);
            HttpUtil.getInstance().sendGet(new SimpleHttpParseListener() {
                @Override
                public void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws Exception {
                    SharedPreferenceUtil.getInstance(context).saveParam(Util.getApplicationName(context),
                            jsonObject.getString("result").equals("ok") ? ConstantMethod.getInstance(context).getAppSession() : ConstantMethod.getInstance(context).getIsExitByAuth(),
                            jsonObject.getString("result").equals("ok") ? System.currentTimeMillis() : true);

                    if (SharedPreferenceUtil.getInstance(activity).readBooleanParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsExitByAuth(), false)) {
                        showDialog(activity);
                    }
                }

                @Override
                public HttpParseHelper getParseHelper() {
                    return new ParseLife();
                }
            }, Constant.HttpPrivateKey.APP_CHECK, url + path, r);
        } else {
            if (System.currentTimeMillis() - Long.parseLong(session) > 5 * 1000) {
                SharedPreferenceUtil.getInstance(context).removeParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getAppSession());
                checkApp(activity);
            }
        }
    }

    private class ParseLife extends HttpParseHelper {
        @Override
        public ReturnObject parse(String type, Call call, Response response) {
            ReturnObject returnObject = new ReturnObject();
            returnObject.httpType = type;
            returnObject.isSuccess = true;
            returnObject.failReason = "";
            try {
                returnObject.jsonObject = new JSONObject(response.body().string());
            } catch (Exception e) {

            }
            return returnObject;
        }
    }

    private void showDialog(final Activity activity) {
        dialog = dialogMap.get(activity);
        if (dialog == null) {
            dialog = new AlertDialog.Builder(activity).setTitle("App即将退出")
                    .setMessage("出现无法预知问题，请联系管理员")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (exitHandler != null) {
                                exitHandler.cancle();
                            }
                            activity.finish();
                            SharedPreferenceUtil.getInstance(context).saveParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsDialogDismiss(), true);
                        }
                    }).create();
            dialog.setCanceledOnTouchOutside(false);
            try {
                if (!activity.isFinishing()) {
                    dialogMap.put(activity, dialog);
                    dialog.show();
                }
            } catch (Exception e) {

            }
        }

        if (exitHandler != null) {
            exitHandler.cancle();
        }
        exitHandler = new MyHandler(3000) {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                activity.finish();
                SharedPreferenceUtil.getInstance(context).saveParam(Util.getApplicationName(context), ConstantMethod.getInstance(context).getIsDialogDismiss(), true);
            }
        };

    }

}
