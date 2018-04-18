package com.wxt.library.contanst;

import android.content.Context;

import com.wxt.library.base.application.BaseApplication;
import com.wxt.library.model.LoginUserBean;

/**
 * Created by Administrator on 2018/3/26.
 */

public final class ConstantMethod {

    private Context context;
    private static ConstantMethod instance;

    public static ConstantMethod getInstance(Context context) {
        if (instance == null) {
            synchronized (ConstantMethod.class) {
                if (instance == null) {
                    instance = new ConstantMethod(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private ConstantMethod(Context context) {
        this.context = context;
    }

    /**
     * 正常退出APP（关闭所有页面）需要的Key
     *
     * @return
     */
    public String getIsExitKey() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_EXIT;
    }

    public String getIsHandlerCrash() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_HANDLE_CRASH;
    }

    public String getIsExitByCrash() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_EXIT_BY_CRASH;
    }

    public String getIsConfirmDialog() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_CONFIRM_DIALOG;
    }

    public String getAppSession() {
        return context.getPackageName() + Constant.SharePreferenceKey.APP_SESSION;
    }

    public String getIsExitByAuth() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_EXIT_BY_AUTH;
    }

    public String getIsDialogDismiss() {
        return context.getPackageName() + Constant.SharePreferenceKey.IS_DIALOG_DISMISS;
    }

    public String getLoginUserInfo() {
        return context.getPackageName() + Constant.SharePreferenceKey.LOGIN_USER_INFO;
    }

    public String getSaveInstanceKeyForListMap(String suffix) {
        return Constant.SharedPreferenceFileName.SHARE_FILE_NAME_FOR_SAVEINSTANCE + "_" + suffix;
    }

    public String getLastLoginUserName() {
        LoginUserBean userBean = BaseApplication.getInstance().getParam(LoginUserBean.class, ConstantMethod.getInstance(context).getLoginUserInfo());
        if (userBean == null) {
            return null;
        }
        return userBean.getUsername();
    }

    public String getLastLoginPassword() {
        LoginUserBean userBean = BaseApplication.getInstance().getParam(LoginUserBean.class, ConstantMethod.getInstance(context).getLoginUserInfo());
        if (userBean == null) {
            return null;
        }
        return userBean.getPassword();
    }

    public String getLoginUrl() {
        LoginUserBean userBean = BaseApplication.getInstance().getParam(LoginUserBean.class, ConstantMethod.getInstance(context).getLoginUserInfo());
        if (userBean == null) {
            return null;
        }
        return userBean.getUrl();
    }

}
