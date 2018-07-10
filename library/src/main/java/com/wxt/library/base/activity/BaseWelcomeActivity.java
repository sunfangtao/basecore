package com.wxt.library.base.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.http.HttpUtil;
import com.wxt.library.http.listener.SimpleHttpParseListener;
import com.wxt.library.listener.APPUpdateListener;
import com.wxt.library.listener.AutoLoginListener;
import com.wxt.library.listener.GuideListener;
import com.wxt.library.listener.PermissionListener;
import com.wxt.library.model.VersionBean;
import com.wxt.library.priva.dialog.ReasonDiaWin;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.PermissionRequestUtil;
import com.wxt.library.util.UpdateDialog;
import com.wxt.library.util.UpdateManager;
import com.wxt.library.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/9.
 * 欢迎页面
 */

public abstract class BaseWelcomeActivity extends BaseParseHelperActivity {

    private long startTime = 0;
    // 用于跳转页面
    private MyHandler jumpHandler;
    //
    private ReasonDiaWin permissionReasonDialog;
    //
    private Map<String, String> params;

    private final int NO_UPDATE = -1;
    private final int WAIT_UPDATE = 0;
    private final int DOWN_UPDATE = 1;

    private int obtainCount = 1;

    private int loginResult = -1;

    private void readyJump() {
        if (jumpHandler != null) {
            jumpHandler.cancle();
        }
        jumpHandler = new MyHandler(500, true, 100) {
            @Override
            public void run() {
                handlerRun();
            }
        };
    }

    // -1 不更新； 0 :等待中 ； 1：更新中（下载中）
    private void setIsUpdate(int isUpdate) {

        if (isUpdate == NO_UPDATE) {
            // 没有更新，或不想更新了
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
                if (this instanceof AutoLoginListener && !isJumpGuide()) {
                    params = new HashMap<>();
                    autoLogin();
                    return;
                }
            }
            readyJump();
        } else if (isUpdate == WAIT_UPDATE) {
            // 等待用户确定是否更新，或验证是否有更新
        } else {
            // 下载更新安装包
        }
    }

    public boolean isNeedLogin() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 申请权限
        if (getPermission()) {
            // 拦截网络请求
            getUpdateInfo();
            afterPermission();
        }
    }

    protected void afterPermission(){

    }

    private boolean getPermission() {
        if (this instanceof PermissionListener) {
            PermissionListener permissionListener = (PermissionListener) this;
            obtainCount = permissionListener.getObtainCount();
            if (--obtainCount <= 1) {
                obtainCount = 1;
            }
            List<Integer> permissionList = permissionListener.getPermissionList();
            if (permissionList != null && permissionList.size() > 0) {
                int length = permissionList.size();

                List<String> reasonList = permissionListener.getPermissionReason();
                if (reasonList == null || reasonList.size() != length) {
                    throw new IllegalArgumentException("大小不一致！");
                }

                int[] permissionArray = new int[length];
                for (int i = 0; i < length; i++) {
                    permissionArray[i] = permissionList.get(i);
                }

                return PermissionRequestUtil.getInstance().requestPermission(this, permissionArray);
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(final int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this instanceof PermissionListener) {
            PermissionListener permissionListener = (PermissionListener) this;
            final List<String> permissionList = new ArrayList<>();
            List<String> tempReasonList = new ArrayList<>();
            List<String> reasonList = permissionListener.getPermissionReason();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                permissionList.add(permissions[i]);
                tempReasonList.add(reasonList.get(i));
            }
            Util.print("permissionList.size=" + permissionList.size());
            if (permissionList.size() > 0 && obtainCount-- > 0) {
                permissionReasonDialog = new ReasonDiaWin(this).setTitle("申请原因").setContent(tempReasonList).setDismissListener(new ReasonDiaWin.PermissionReasonDialogDismissListener() {
                    @Override
                    public void onDialogDismiss(boolean isConfirm) {
                        if (isConfirm) {
                            // 用户拒绝了部分权限
                            ActivityCompat.requestPermissions(BaseWelcomeActivity.this, permissionList.toArray(new String[]{}), requestCode);
                        } else {
                            Util.print("未成功授权！");
                            new MyHandler(1000) {
                                @Override
                                public void run() {
                                    finish();
                                }
                            };
                        }
                    }
                });
                permissionReasonDialog.show();
            } else if (permissionList.size() > 0 && obtainCount <= 0) {
                // 用户多次拒绝权限，直接退APP
                Util.print("未成功授权！");
                new MyHandler(1000) {
                    @Override
                    public void run() {
                        finish();
                    }
                };
            } else {
                Util.print("用户同意权限");
                // 用户同意权限
                getUpdateInfo();
                afterPermission();
            }
        }

    }

    private void getUpdateInfo() {
        setIsUpdate(WAIT_UPDATE);
        params = new HashMap<>();
        HttpUtil.getInstance(3000, 3000, 3000).sendGet(new SimpleHttpParseListener() {
            @Override
            public void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws JSONException {
                // 更新结果
                if (obj instanceof VersionBean) {
                    VersionBean versionBean = (VersionBean) obj;
                    if (Util.isUpdate(BaseWelcomeActivity.this, versionBean.getVersionCode())) {
                        showUpdateVersionDialog(versionBean);
                        return;
                    }
                }
                setIsUpdate(NO_UPDATE);
            }

            @Override
            public void onHttpFailure(String type, int code, String err, String resultType) {
                setIsUpdate(NO_UPDATE);
            }
        }, Constant.HttpPrivateKey.AUTO_UPDATE, Constant.UrlKey.UPDATE_URL, params, VersionBean.class);
    }

    private void autoLogin() {

        Class clazz = null;
        try {
            Type[] types = getClass().getGenericInterfaces();
            ParameterizedType parameterizedType = null;
            for (int i = 0; i < types.length; i++) {
                if (types[i].toString().contains(AutoLoginListener.class.getName())) {
                    if (types[i] instanceof ParameterizedType) {
                        parameterizedType = (ParameterizedType) types[i];
                    }
                    break;
                }
            }
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            clazz = (Class) actualTypeArgument;
        } catch (Exception e) {
            e.printStackTrace();
        }

        final AutoLoginListener loginListener = (AutoLoginListener) this;

        // 已经完成引导页（如果存在），并且需要自动登录
        String userName = ConstantMethod.getInstance(this.getApplicationContext()).getLastLoginUserName();
        String password = ConstantMethod.getInstance(this.getApplicationContext()).getLastLoginPassword();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            loginResult = 0;
            // 用户名和密码都存在，自动登录
            params = new HashMap<>();
            params.put("username", userName);
            params.put("password", password);

            HttpUtil.getInstance(3000, 3000, 3000).sendPost(new SimpleHttpParseListener() {
                @Override
                public void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws JSONException {
                    loginListener.parseLogin(true, jsonObject, obj, null);
                    loginResult = 1;
                    readyJump();
                }

                @Override
                public void onHttpFailure(String type, int code, String err, String resultType) {
                    loginListener.parseLogin(false, null, null, err);
                    loginResult = -1;
                    readyJump();
                }
            }, Constant.HttpPrivateKey.AUTO_LOGIN, ConstantMethod.getInstance(this.getApplicationContext()).getLoginUrl(), params, clazz);
        } else {
            // TODO 没有执行自动登录
            if (isNeedLogin()) {
                // 延迟时间后，跳转登录页面
                loginListener.parseLogin(false, null, null, null);
                loginResult = -1;
            } else {
                // 延迟时间后，跳转主界面，需要登录的时候在处理登录
                loginListener.parseLogin(false, null, null, null);
                loginResult = -1;
            }
            readyJump();
        }
    }

    private final boolean isShowGuide() {
        String isShowGuide = sharedPreferenceUtil.readStrParam(Util.getApplicationName(this), Constant.SharePreferenceKey.FIRST_RUN_APP, "");
        if (isShowGuide.equals(Constant.IS_SHOW_GUIDE)) {
            // 不再显示Guide
            return false;
        }
        return true;
    }

    protected abstract Class setIndexActivity();

    protected abstract Class setLoginActivity();

    protected boolean autoJump() {
        return true;
    }

    /**
     * 页面停留指定时间后跳转页面
     */
    private void jumpActivity() {
        Intent intent = null;
        if (isJumpGuide()) {
            GuideListener listener = (GuideListener) this;
            intent = new Intent(this, listener.setGuideActivity());
            intent.putExtra(Constant.IntentKey.INDEX_ACTIVITY, setIndexActivity());
            intent.putExtra(Constant.IntentKey.LOGIN_IS_NEED, isNeedLogin());
            intent.putExtra(Constant.IntentKey.LOGIN_ACTIVITY, setLoginActivity());
        } else {
            if (this instanceof AutoLoginListener) {
                if (loginResult <= 0 && isNeedLogin()) {
                    intent = new Intent(this, setLoginActivity());
                    intent.putExtra(Constant.IntentKey.INDEX_ACTIVITY, setIndexActivity());
                } else {
                    intent = new Intent(this, setIndexActivity());
                }
            } else {
                if (isNeedLogin()) {
                    intent = new Intent(this, setLoginActivity());
                    intent.putExtra(Constant.IntentKey.INDEX_ACTIVITY, setIndexActivity());
                } else {
                    intent = new Intent(this, setIndexActivity());
                }
            }
        }
        startActivity(intent);
        finish();
    }

    private boolean isJumpGuide() {
        if (this instanceof GuideListener) {
            Class clazz = ((GuideListener) this).setGuideActivity().getSuperclass();
            if (clazz.getName().equals(BaseGuideActivity.class.getName())) {
                return isShowGuide();
            }
            Util.print("引导Activity需要继承BaseGuideActivity!,否则不能跳转");
        }
        return false;
    }

    private boolean durationTime() {
        if (isJumpGuide()) {
            // 1秒后跳转引导页面
            return System.currentTimeMillis() - startTime >= 1000;
        } else if (this instanceof AutoLoginListener) {
            if (loginResult < 0) {
                return System.currentTimeMillis() - startTime >= 2000;
            } else if (loginResult == 0) {
                return System.currentTimeMillis() - startTime >= 3000;
            } else {
                return System.currentTimeMillis() - startTime >= 2000;
            }
        } else {
            return System.currentTimeMillis() - startTime >= 2000;
        }

    }

    private void showUpdateVersionDialog(final VersionBean versionBean) {
        new UpdateDialog(this).setTitle(versionBean.getTitle()).setContent(versionBean.getContent(), null)
                .setDismissListener(new UpdateDialog.UpdateDialogDismissListener() {
                    @Override
                    public void onDialogDismiss(boolean isConfirm) {
                        if (!isFinishing())
                            handlerUpdate(versionBean, isConfirm);
                    }
                }).show();
    }

    private void handlerUpdate(final VersionBean versionBean, boolean isConfirm) {
        if (isConfirm) {
            new UpdateManager(this, versionBean.getVersionUrl(), new APPUpdateListener() {
                @Override
                public void downloadCancel() {
                    if ("1".equals(versionBean.getIsForce())) {
                        // 强制更新，用户取消更新，直接退出
                        finish();
                    } else {
                        setIsUpdate(NO_UPDATE);
                    }
                }

                @Override
                public void successDownload() {
                    finish();
                }

                @Override
                public void downloadFail() {
                    Util.print("更新失败！");
                    if ("1".equals(versionBean.getIsForce())) {
                        // 强制更新，用户取消更新，直接退出
                        finish();
                    } else {
                        setIsUpdate(NO_UPDATE);
                    }
                }
            }).showDefaultDownloadDialog();
            setIsUpdate(DOWN_UPDATE);
        } else {
            if ("1".equals(versionBean.getIsForce())) {
                // 强制更新，用户不更新，直接退出
                finish();
            } else {
                setIsUpdate(NO_UPDATE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (jumpHandler != null) {
            jumpHandler.cancle();
        }
        if (permissionReasonDialog != null) {
            permissionReasonDialog.dismiss();
        }
    }

    private void handlerRun() {
        if (!autoJump()) {
            return;
        }
        if (durationTime()) {
            jumpActivity();
            if (jumpHandler != null) {
                jumpHandler.cancle();
                jumpHandler = null;
            }
        }
    }

}
