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

import com.wxt.library.base.adapter.BaseExpandableAdapter;
import com.wxt.library.contanst.Constant;
import com.wxt.library.contanst.ConstantMethod;
import com.wxt.library.listener.LoginListener;
import com.wxt.library.priva.listener.FragmentStateChangedListener;
import com.wxt.library.priva.util.ActivityLife;
import com.wxt.library.priva.util.FragmentChangedUtil;
import com.wxt.library.priva.util.MyToast;
import com.wxt.library.priva.util.ReflectUtil;
import com.wxt.library.util.SharedPreferenceUtil;
import com.wxt.library.view.DefaultNullRecyclerView;

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
        ActivityLife.getInstance(activity.getApplicationContext());

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
            List<ReflectUtil.FieldObject> fields = ReflectUtil.getAllFieldList(clazz);

            String fileName = null;

            for (ReflectUtil.FieldObject field : fields) {
                try {
                    clazz = field.field.getType();
                    if (field.field.getName().equals("_$_findViewCache")) {
                        // view的集合，直接过滤
                        continue;
                    }

                    if (clazz.getName().contains("android.widget") || clazz.getName().contains("android.support")) {
                        continue;
                    }

                    if (clazz.getName().equals("android:view_state")) {
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
                        // TODO 只对java有效（Kotlin无效）
//                        if (!((field.field.getModifiers() & Modifier.PROTECTED) == Modifier.PROTECTED) && !((field.field.getModifiers() & Modifier.PUBLIC) == Modifier.PUBLIC)) {
//                            // final 修饰的不需要保存
//                            field.field.setAccessible(false);
//                            continue;
//                        }
                    }

                    Serializable serializable = null;

                    if (obj instanceof Serializable) {
                        if (obj instanceof List || obj instanceof Map) {
                            if (fileName == null) {
                                fileName = getShareFileName();
                            }
                            // 物理存储
                            sharedPreferenceUtil.saveParam(fileName, this.getClass().getName() + field.field.getName(), obj);
                        } else if (obj.getClass().isPrimitive() || obj instanceof String) {
                            // 基本类型或字符串
                            serializable = (Serializable) obj;
                        } else if (!(obj instanceof ParameterizedType)) {
                            // 非泛型的序列化对象
                            serializable = (Serializable) obj;
                        }
                    }

                    if (serializable != null) {
                        outState.putSerializable(field.field.getName(), serializable);
                    }
                } catch (Exception e) {

                } finally {
                    field.field.setAccessible(false);
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
                            sharedPreferenceUtil.removeParam(fileName, keyStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Long.parseLong(key);
                        continue;
                    } catch (Exception e) {

                    }
                    if (key.equals("android:view_state")) {
                        continue;
                    }
                    try {
                        Field field = null;
                        for (ReflectUtil.FieldObject f : fields) {
                            if (f.field.getName().equals(key)) {
                                field = f.field;
                                break;
                            }
                        }
                        if (field == null) {
                            continue;
                        }
                        field.setAccessible(true);
                        field.set(this, savedInstanceState.getSerializable(key));
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
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
        return ConstantMethod.getInstance(getActivity().getApplicationContext()).getSaveInstanceKeyForListMap(this.getClass().getName());
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
