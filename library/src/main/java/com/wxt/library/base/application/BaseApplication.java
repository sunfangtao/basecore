package com.wxt.library.base.application;

import android.app.Application;
import android.content.Context;
import android.support.annotation.CallSuper;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.crash.CrashHandler;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.model.LoginUserBean;
import com.wxt.library.util.SharedPreferenceUtil;

public class BaseApplication extends Application {

    protected static BaseApplication app;

    public static <T extends BaseApplication> T getInstance() {
        return (T) app;
    }

    private RefWatcher refWatcher;

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        BaseApplication leakApplication = (BaseApplication) context.getApplicationContext();
        return leakApplication.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        refWatcher= setupLeakCanary();

        if (app == null) {
            synchronized (BaseApplication.class) {
                if (app == null) {
                    app = this;
                    CrashHandler.init(this, true);
                }
            }
        }
    }

    public String getLoginUsername() {
        return ConstantMethod.getInstance(this).getLastLoginUserName();
    }

    /**
     * 支持单个对象（DBVO子类）的存取
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getParam(Class<T> clazz, String subFix) {
        String key = clazz.getName() + "_" + subFix;
        return SharedPreferenceUtil.getInstance(this).readObj(key);
    }

    /**
     * 数据存储
     *
     * @param obj 需要实现序列化
     */
    public void setParam(Object obj, String subFix) {
        String key = obj.getClass().getName() + "_" + subFix;
        SharedPreferenceUtil.getInstance(this).saveParam(key, obj);
    }

    /**
     * 清除cookie和登录密码
     */
    @CallSuper
    public void clearLoginInfo() {
        HttpUtil.getInstance().clearCookies();

        LoginUserBean userBean = new LoginUserBean();
        userBean.setUsername(ConstantMethod.getInstance(this).getLastLoginUserName());
        userBean.setPassword(null);
        BaseApplication.getInstance().setParam(userBean, ConstantMethod.getInstance(this).getLoginUserInfo());
    }
}
