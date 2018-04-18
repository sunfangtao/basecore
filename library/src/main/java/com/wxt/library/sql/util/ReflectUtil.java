package com.wxt.library.sql.util;

import com.wxt.library.retention.NotProguard;
import com.wxt.library.sql.model.DBVO;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectUtil<T extends DBVO> {

    Object obj = null;

    public Class<?> getClass(Class<T> clazz) {
        if (clazz.getName().equals(DBVO.class.getName()))
            throw new RuntimeException("请使用DBVO的子类");
        while (!clazz.getSuperclass().getName().equals(DBVO.class.getName())) {
            clazz = (Class<T>) clazz.getSuperclass();
        }
        return clazz;
    }

    public ReflectUtil(Class<T> clazz) {
        try {
            obj = getClass(clazz).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ReflectUtil() {

    }

    public ReflectUtil(DBVO baseVO) {
        obj = baseVO;
    }

    /**
     * 获取类中所有的成员变量，不包括父类
     *
     * @param clazz
     * @return
     */
    public static List<String> getFieldsName(Class<?> clazz) {
        List<String> fieldList = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldList.add(field.getName());
        }
        return fieldList;
    }

    public static List<Field> getFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        return Arrays.asList(fields);
    }

    /**
     * 根据类获取其中的Setter方法
     *
     * @param clazz
     * @return 方法的list列表
     */
    public List<Method> obtainSetMethods(Class<T> clazz) {
        List<Method> list = null;
        try {
            Method[] methods = getClass(clazz).getDeclaredMethods();

            int length = methods.length;
            list = new ArrayList<Method>();
            for (int i = 0; i < length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith("set")) {
                    list.add(methods[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据类获取其中的Getter方法
     *
     * @param clazz
     * @return 方法的list列表
     */
    public List<Method> obtainGetMethods(Class<T> clazz) {
        List<Method> list = null;
        try {
            Method[] methods = getClass(clazz).getDeclaredMethods();

            int length = methods.length;
            list = new ArrayList<Method>();
            for (int i = 0; i < length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith("get") || methodName.startsWith("is"))
                    list.add(methods[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void setter(String methodName, Object value, Class<?>... paramsType) {
        try {
            Method method = obj.getClass().getMethod(methodName, paramsType);
            method.invoke(obj, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getter(String methodName, Class<?>... paramsType) {
        Object result = null;
        try {
            Method method = obj.getClass().getMethod(methodName, paramsType);
            result = method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Object getObj() {
        return obj;
    }
}
