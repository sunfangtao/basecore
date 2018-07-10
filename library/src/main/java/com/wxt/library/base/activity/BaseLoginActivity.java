package com.wxt.library.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.wxt.library.base.application.BaseApplication;
import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.http.listener.SimpleHttpParseListener;
import com.wxt.library.listener.LoginListener;
import com.wxt.library.model.LoginUserBean;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO 实现用户名密码登录，第三方登录没有实现
 * Created by Administrator on 2018/4/2.
 */

public class BaseLoginActivity<T> extends BaseParseHelperActivity {

    private Class indexClazz;
    private boolean isNeedLogin;

    /**
     * 登录的用户名key，为空使用默认值‘username’
     *
     * @return
     */
    protected String changeUserNameParams() {
        return "";
    }

    /**
     * 登录的密码key，为空使用默认值‘password’
     *
     * @return
     */
    protected String changePasswordParams() {
        return "";
    }

    protected boolean autoJumpIndex() {
        return false;
    }

    private void jump() {
        if (this.indexClazz != null) {
            Intent intent = new Intent(this, this.indexClazz);
            startActivity(intent);
            finish();
        } else {
            finish();
            Util.print("没有跳转的对象");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.indexClazz = (Class) getIntent().getSerializableExtra(Constant.IntentKey.INDEX_ACTIVITY);
        this.isNeedLogin = getIntent().getBooleanExtra(Constant.IntentKey.LOGIN_IS_NEED, false);
    }

    protected final void login(final String username, final String password) {
        login(username, password, null);
    }

    protected final void login(final String username, final String password, final String url) {
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            throw new IllegalArgumentException("username或password为空！");
        }

        Class clazz = null;
        try {
            Type type = getClass().getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
                clazz = (Class) actualTypeArgument;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashMap<>();
        map.put(TextUtils.isEmpty(changeUserNameParams()) ? "username" : changeUserNameParams(), username);
        map.put(TextUtils.isEmpty(changePasswordParams()) ? "password" : changePasswordParams(), password);

        HttpUtil.getInstance().sendPost(new SimpleHttpParseListener() {
            @Override
            public void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws JSONException {
                saveLoginInfo(username, password, url);
                LoginListener loginListener = (LoginListener) getIntent().getSerializableExtra(Constant.IntentKey.LOGIN_CALLBACK);
                if (loginListener != null) {
                    finish();
                    loginListener.afterLogin(jsonObject, obj);
                } else {
                    BaseLoginActivity.this.onHttpSuccess(type, jsonObject, obj);
                    if (autoJumpIndex()) {
                        new MyHandler(500) {
                            @Override
                            public void run() {
                                jump();
                            }
                        };
                    }
                }
            }

            @Override
            public void onHttpFailure(String type, int code, String err, String resultType) {
                BaseLoginActivity.this.onHttpFailure(type, code, err, resultType);
            }
        }, url == null ? Constant.UrlKey.LOGIN_URL : url, map, clazz);
    }

    private void saveLoginInfo(String username, String password, String url) {
        LoginUserBean userBean = new LoginUserBean();
        userBean.setUsername(username);
        userBean.setPassword(password);
        userBean.setUrl(url == null ? Constant.UrlKey.LOGIN_URL : url);
        BaseApplication.getInstance().setParam(userBean, ConstantMethod.getInstance(this.getApplicationContext()).getLoginUserInfo());
    }

    protected final String getLastUserName() {
        return ConstantMethod.getInstance(this.getApplicationContext()).getLastLoginUserName();
    }

    protected final String getLastPassword() {
        return ConstantMethod.getInstance(this.getApplicationContext()).getLastLoginPassword();
    }

}
