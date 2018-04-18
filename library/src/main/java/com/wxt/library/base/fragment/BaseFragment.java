package com.wxt.library.base.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.listener.LoginListener;
import com.wxt.library.priva.listener.FragmentStateChangedListener;
import com.wxt.library.priva.util.ActivityLife;
import com.wxt.library.priva.util.FragmentChangedUtil;
import com.wxt.library.priva.util.MyToast;
import com.wxt.library.util.SharedPreferenceUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseFragment extends Fragment {

    /**
     * 传递的数据
     */
    protected Bundle bundle;

    protected MyToast toast;

    protected SharedPreferenceUtil sharedPreferenceUtil;

    private final boolean isFirstCreated = (null != null ? false : false);

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // 恢复界面数据
            restoreBean(savedInstanceState);
            afterRestoreInstanceState(savedInstanceState);
        }
        if (isFirstCreated && savedInstanceState == null) {
            setIsFirstCreated(false);
            noSaveInstanceStateForCreate();
        }
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentViewStateRestored(this, savedInstanceState);
        }
    }

    protected void noSaveInstanceStateForCreate() {

    }

    private void setIsFirstCreated(boolean b) {
        try {
            Field field = BaseFragment.class.getDeclaredField("isFirstCreated");
            field.setAccessible(true);
            field.set(this, b);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setIsFirstCreated(true);
        bundle = getArguments();
        if (sharedPreferenceUtil == null) {
            sharedPreferenceUtil = SharedPreferenceUtil.getInstance(activity);
        }
        if (toast == null) {
            toast = new MyToast(getActivity());
        }
        ActivityLife.getInstance(activity);

        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentAttach(this, activity);
        }
    }

    protected void afterRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // 存储数据的值
        findBeanForSave(outState);
        super.onSaveInstanceState(outState);
    }

    private void findBeanForSave(Bundle outState) {
        try {
            Class clazz = getClass();
            Field[] fields = clazz.getDeclaredFields();

            String fileName = null;

            for (Field field : fields) {
                try {
                    clazz = field.getType();
                    if (clazz.getName().contains("android.widget") || clazz.getName().contains("android.support")) {
                        continue;
                    }
                    if (field.get(this) instanceof View || field.get(this) instanceof ViewGroup) {
                        // View不需要保存
                        continue;
                    }
                    if (((field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)) {
                        // final 修饰的不需要保存
                        continue;
                    }
                    field.setAccessible(true);

                    Object obj = field.get(this);
                    if (obj == null) {
                        field.setAccessible(false);
                        continue;
                    }

                    Serializable serializable = null;

                    if (obj instanceof Serializable) {
                        if (obj instanceof List || obj instanceof Map) {
                            if (fileName == null) {
                                fileName = getShareFileName();
                            }
                            // 物理存储
                            sharedPreferenceUtil.saveParam(fileName, this.getClass().getName() + field.getName(), obj);
                        } else if (obj.getClass().isPrimitive() || obj instanceof String) {
                            // 基本类型或字符串
                            serializable = (Serializable) obj;
                        } else if (!(obj instanceof ParameterizedType) && obj instanceof Serializable) {
                            // 非泛型的序列化对象
                            serializable = (Serializable) obj;
                        }
                    }
                    field.setAccessible(false);
                    if (serializable != null) {
                        outState.putSerializable(field.getName(), serializable);
                    }
                } catch (Exception e) {

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
                            Field field = clazz.getDeclaredField(keyStr.replace(this.getClass().getName(), ""));
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
                            sharedPreferenceUtil.removeParam(fileName, keyStr);
                        } catch (Exception e) {

                        }
                    }
                } else {
                    try {
                        Long.parseLong(key);
                        continue;
                    } catch (Exception e) {

                    }
                    Field field = clazz.getDeclaredField(key);
                    field.setAccessible(true);
                    field.set(this, savedInstanceState.getSerializable(key));
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //  发现RecyclerView,Adapter 自动刷新
        Field[] fields = clazz.getDeclaredFields();
        //
        for (Field field : fields) {
            try {
                Object object = field.get(this);
                if (object instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) object;
                    recyclerView.getAdapter().notifyDataSetChanged();
                } else if (object instanceof RecyclerView.Adapter) {
                    ((RecyclerView.Adapter) object).notifyDataSetChanged();
                }
            } catch (Exception e) {

            }
        }
    }

    private String getShareFileName() {
        return ConstantMethod.getInstance(getActivity()).getSaveInstanceKeyForListMap(this.getClass().getName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentCreateView(this, view);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentDestroyView(this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentResume(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentPause(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentDestroy(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Set<FragmentStateChangedListener> listeners = FragmentChangedUtil.getInstance().getChangedListener();
        for (FragmentStateChangedListener listener : listeners) {
            listener.onFragmentDetach(this);
        }
    }

}
