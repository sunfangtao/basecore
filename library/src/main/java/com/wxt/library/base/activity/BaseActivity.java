package com.wxt.library.base.activity;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseExpandableAdapter;
import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.listener.LoginListener;
import com.wxt.library.priva.util.ActivityLife;
import com.wxt.library.priva.util.ForbidFastClick;
import com.wxt.library.priva.util.MyToast;
import com.wxt.library.priva.util.ReflectUtil;
import com.wxt.library.util.SharedPreferenceUtil;
import com.wxt.library.util.Util;
import com.wxt.library.view.DefaultNullRecyclerView;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * 包含异常数据恢复，与activity通信，标题栏定制
 *
 * @author SunFangtao
 */
public abstract class BaseActivity extends AppCompatActivity {

    private Bundle savedInstanceState;

    /**
     * 广播
     */
    private Receiver receiver;

    protected MyToast toast;

    protected Toolbar toolbar;
    protected TextView toolBarTitleTv;

    protected SharedPreferenceUtil sharedPreferenceUtil;

    private final String FragmentSign = "@fragment";

    private final boolean isFirstCreated = (null != null ? false : false);

    private void initPrivateData() {
        if (sharedPreferenceUtil == null) {
            sharedPreferenceUtil = SharedPreferenceUtil.getInstance(this);
        }
        if (toast == null) {
            toast = new MyToast(this);
        }
        // 注册广播
        register(getClass().getName());
    }

    protected boolean isShowNavigationBtn() {
        return true;
    }

    private void setIsFirstCreated(boolean b) {
        try {
            Field field = BaseActivity.class.getDeclaredField("isFirstCreated");
            field.setAccessible(true);
            field.set(this, b);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT < 21) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
        this.savedInstanceState = savedInstanceState;
        initPrivateData();
        setIsFirstCreated(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isFirstCreated && savedInstanceState == null) {
            setIsFirstCreated(false);
            noSaveInstanceStateForCreate();
        }
    }

    @Override
    protected void onResume() {
        if (sharedPreferenceUtil.readBooleanParam(ConstantMethod.getInstance(this.getApplicationContext()).getIsExitKey(), false)) {
            finish();
        }
        super.onResume();
    }

    protected void noSaveInstanceStateForCreate() {

    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        initTitleBar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initTitleBar();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initTitleBar();
    }

    protected boolean isTitleCenter() {
        return false;
    }

    protected boolean isShowDownLine() {
        return false;
    }

    private void initTitleBar() {
        this.toolbar = findViewById(R.id.toolbar);
        try {
            this.toolBarTitleTv = findViewById(R.id.toolbar_title);
            View view = findViewById(R.id.toolbar_devider);
            if (view != null) {
                view.setVisibility(isShowDownLine() ? View.VISIBLE : View.GONE);
            }
        } catch (NoSuchFieldError e) {
            Util.print(getClass().getName() + "页面没有设置居中title");
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.return_btn_bkground);

            if (isTitleCenter()) {
                if (toolBarTitleTv != null) {
                    toolBarTitleTv.setText(toolbar.getTitle());
                    try {
                        Field field = Toolbar.class.getDeclaredField("mTitleTextAppearance");
                        field.setAccessible(true);
                        int mTitleTextAppearance = (int) field.get(toolbar);
                        if (mTitleTextAppearance != 0) {
                            toolBarTitleTv.setTextAppearance(this, mTitleTextAppearance);
                        }
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toolbar.setTitle("");
                setSupportActionBar(toolbar);
            }
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationClick();
                }
            });

            if (!isShowNavigationBtn()) {
                // 移除返回按钮
                try {
                    Field field = Toolbar.class.getDeclaredField("mNavButtonView");
                    field.setAccessible(true);
                    ImageButton btn = (ImageButton) field.get(toolbar);
                    toolbar.removeView(btn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (isTitleCenter()) {
            if (toolBarTitleTv != null) {
                toolBarTitleTv.setText(title);
            }
        } else {
            if (toolbar != null) {
                toolbar.setTitle(title);
                setSupportActionBar(toolbar);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNavigationClick();
                    }
                });
            }
        }
    }

    public void onNavigationClick() {
        finish();
    }

    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    public void setTitleColor(int color) {
        if (isTitleCenter()) {
            if (toolBarTitleTv != null) {
                toolBarTitleTv.setTextColor(color);
            }
        } else {
            if (toolbar != null) {
                toolbar.setTitleTextColor(color);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 关闭当前页面时，解除注册的广播
        unRegister();
        super.onDestroy();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            forReceiverResult(intent);
        }
    }

    /**
     * 注册广播，用于更新activity中的数据，实现forReceiverResult();页面关闭广播自动解除
     */
    private void register(String activityName) {
        if (receiver != null) {
            unRegister();
        }
        //注册广播接收器
        receiver = new Receiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(activityName);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    private void unRegister() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    /**
     * 子类复写实现自己的操作
     *
     * @param intent
     */
    public void forReceiverResult(Intent intent) {

    }

    public boolean onClickSingleView() {
        // 禁止用户连续点击控件
        if (!ForbidFastClick.isFastDoubleClick()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // 存储数据的值
        findBeanForSave(outState);
        // Fragment处理
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // 恢复界面数据
        restoreBean(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
        afterRestoreInstanceState(savedInstanceState);
    }

    protected void afterRestoreInstanceState(Bundle savedInstanceState) {

    }

    private void findBeanForSave(Bundle outState) {
        try {
            Class clazz = getClass();
            List<ReflectUtil.FieldObject> fields = ReflectUtil.getAllFieldList(clazz);

            String fileName = null;
            for (ReflectUtil.FieldObject field : fields) {

                try {
                    ActivityLife.getInstance(this.getApplicationContext());

                    clazz = field.field.getType();
                    if (field.field.getName().equals("_$_findViewCache")) {
                        // view集合，直接过滤
                        continue;
                    }
                    if (clazz.getName().contains("android.widget") || clazz.getName().contains("android.support")) {
                        continue;
                    }

                    field.field.setAccessible(true);
                    Object obj = field.field.get(this);
                    if (obj == null) {
                        field.field.setAccessible(false);
                        continue;
                    }

                    if (obj instanceof View || obj instanceof ViewGroup) {
                        // View不需要保存
                        field.field.setAccessible(false);
                        continue;
                    }
                    if (((field.field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)) {
                        // final 修饰的不需要保存
                        field.field.setAccessible(false);
                        continue;
                    }
                    if (!field.isSelf) {
                        // 父类的属性，只保存public和protected
                        if (field.field.getModifiers() != Modifier.PROTECTED
                                && field.field.getModifiers() != Modifier.PUBLIC
                                && field.field.getModifiers() != Modifier.PRIVATE) {
                            field.field.setAccessible(false);
                            continue;
                        }
                    }

                    Serializable serializable = null;
                    if (obj instanceof Serializable) {
                        if (obj instanceof List || obj instanceof Map) {
                            if (fileName == null) {
                                fileName = getShareFileName();
                            }
                            // 物理存储
                            SharedPreferenceUtil.getInstance(this).saveParam(fileName, this.getClass().getName() + field.field.getName(), obj);
                        } else if (obj.getClass().isPrimitive() || obj instanceof String) {
                            // 基本类型或字符串
                            serializable = (Serializable) obj;
                        } else if (!(obj instanceof ParameterizedType) && obj instanceof Serializable) {
                            // 非泛型的序列化对象
                            serializable = (Serializable) obj;
                        }
                    } else if (obj instanceof Fragment) {
                        outState.putSerializable(field.field.getName() + FragmentSign, "");
                    }
                    field.field.setAccessible(false);
                    if (serializable != null) {
                        outState.putSerializable(field.field.getName(), serializable);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileName != null)
                outState.putSerializable(fileName, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restoreBean(Bundle savedInstanceState) {
        Class clazz = this.getClass();
        List<ReflectUtil.FieldObject> fields = ReflectUtil.getAllFieldList(clazz);
        for (String key : savedInstanceState.keySet()) {
            try {
                String fileName = getShareFileName();
                if (key.equals(fileName)) {
                    // 物理读取
                    Map<String, ?> keyMap = sharedPreferenceUtil.getAllShareKey(fileName);
                    if (key == null) {
                        continue;
                    }

                    for (String keyStr : keyMap.keySet()) {
                        try {
                            Field field = null;
                            for (ReflectUtil.FieldObject f : fields) {
                                if (f.field.getName().equals(keyStr.replace(this.getClass().getName(), ""))) {
                                    field = f.field;
                                    break;
                                }
                            }
                            if (field == null) {
                                continue;
                            }
                            field.setAccessible(true);
                            Object object = field.get(this);
                            if (object instanceof List) {
                                List saveList = sharedPreferenceUtil.readObj(fileName, keyStr);
                                ((List) object).clear();
                                ((List) object).addAll(saveList);
                            } else if (object instanceof Map) {
                                Map saveMap = sharedPreferenceUtil.readObj(fileName, keyStr);
                                ((Map) object).clear();
                                ((Map) object).putAll(saveMap);
                            }
                            field.setAccessible(false);
                            // 删除物理存储
                            SharedPreferenceUtil.getInstance(this).removeParam(fileName, keyStr);
                        } catch (Exception e) {

                        }
                    }
                } else {
                    try {
                        Long.parseLong(key);
                        continue;
                    } catch (Exception e) {

                    }
                    String fieldName = key.replace(FragmentSign, "");
                    if ("android:viewHierarchyState".equals(fieldName) || "android:support:fragments".equals(fieldName) || "android:fragments".equals(fieldName)) {
                        continue;
                    }

                    try {
                        Field field = null;
                        for (ReflectUtil.FieldObject f : fields) {
                            if (f.field.getName().equals(fieldName)) {
                                field = f.field;
                                break;
                            }
                        }
                        if (field == null) {
                            continue;
                        }
                        field.setAccessible(true);
                        if (key.contains(FragmentSign)) {
                            field.set(this, getFragmentManager().findFragmentByTag(key.replace(FragmentSign, "")));
                        } else {
                            field.set(this, savedInstanceState.getSerializable(key));
                        }
                        field.setAccessible(false);
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //  发现RecyclerView,Adapter 自动刷新
        for (ReflectUtil.FieldObject field : fields) {
            try {
                Object object = field.field.get(this);
                if (object instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) object;
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else if (object instanceof RecyclerView.Adapter) {
                    ((RecyclerView.Adapter) object).notifyDataSetChanged();
                } else if (object instanceof DefaultNullRecyclerView) {
                    DefaultNullRecyclerView recyclerView = (DefaultNullRecyclerView) object;
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else if (object instanceof BaseExpandableAdapter) {
                    ((BaseExpandableAdapter) object).notifyDataSetChanged();
                }
            } catch (Exception e) {

            }
        }
    }

    private String getShareFileName() {
        return ConstantMethod.getInstance(this.getApplicationContext()).getSaveInstanceKeyForListMap(this.getClass().getName());
    }

    protected View getRootView() {
        return ((ViewGroup) getWindow().getDecorView().getRootView().findViewById(android.R.id.content)).getChildAt(0);
    }

    @Override
    public void startActivity(Intent intent) {
        if (this instanceof LoginListener) {
            if (intent != null) {
                intent.putExtra(Constant.IntentKey.LOGIN_CALLBACK, (LoginListener) this);
            }
        }
        super.startActivity(intent);
    }
}
