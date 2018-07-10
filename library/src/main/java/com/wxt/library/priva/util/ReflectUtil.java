package com.wxt.library.priva.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2018/5/4.
 */

public class ReflectUtil {

    public static List<FieldObject> getAllFieldList(Class clazz) {
        List<Field> selfFields = Arrays.asList(clazz.getDeclaredFields());

        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            try {
                fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
                clazz = clazz.getSuperclass(); //得到父类,然后赋给自己
                if (clazz.getName().contains("BaseActivity") || clazz.getName().contains("BaseFragment")) {
                    clazz = null;
                }
            } catch (Exception e) {
                clazz = null;
            }
        }

        List<FieldObject> allFieldList = new ArrayList<>();

        for (Field field : selfFields) {
            if (!allFieldList.contains(field)) {
                allFieldList.add(new FieldObject(field, true));
            }
        }

        for (Field field : fieldList) {
            if (!allFieldList.contains(field)) {
                allFieldList.add(new FieldObject(field, false));
            }
        }
        return allFieldList;
    }

    public static class FieldObject {
        public Field field;
        public boolean isSelf;

        public FieldObject(Field field, boolean isSelf) {
            this.field = field;
            this.isSelf = isSelf;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Field) {
                Field field1 = (Field) obj;
                if (field1.getName().equals(field.getName())) {
                    return true;
                }
            }
            return false;
        }
    }
}
