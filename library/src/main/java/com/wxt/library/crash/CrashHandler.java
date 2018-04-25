package com.wxt.library.crash;

import android.content.Context;

import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.retention.NotProguard;
import com.wxt.library.util.SharedPreferenceUtil;

import static com.wxt.library.contanst.Constant.SharedPreferenceFileName.CRASH_PARAMS_FILE;

/**
 * AppCrash 处理类 ，不要去尝试new一个对象，由应用自动去维护 ；需要此功能时，调用init方法即可
 *
 * @author Administrator
 */
@NotProguard
public final class CrashHandler {

    private CrashHandler() {
    }

    /**
     * 初始化Crash日志管理类
     *
     * @param context        上下文
     * @param isHandlerCrash 是否处理Crash
     */
    @NotProguard
    public final static void init(Context context, boolean isHandlerCrash) {
        // 获取SharedPreferences对象
        SharedPreferenceUtil.getInstance(context).saveParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsHandlerCrash(), isHandlerCrash);
        SharedPreferenceUtil.getInstance(context).saveParam(CRASH_PARAMS_FILE, ConstantMethod.getInstance(context.getApplicationContext()).getIsExitByCrash(), false);
    }
}
