package com.wxt.library.sql.implement;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.wxt.library.sql.listener.DBOperListener;
import com.wxt.library.sql.model.DBVO;
import com.wxt.library.sql.util.DBUtil;
import com.wxt.library.sql.util.ReflectUtil;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("DefaultLocale")
public final class DBOperListenerImpl implements DBOperListener {
    /**
     * 获取表中列名
     *
     * @param db
     * @param tableName
     * @return
     * @throws Exception
     */
    private List<String> getTableColumns(SQLiteDatabase db, String tableName) {
        try {
            String sql = "select * from " + tableName + " limit 1";
            Cursor cursor = db.rawQuery(sql, null);
            List<String> oldCol = new ArrayList<String>();
            if (cursor.moveToNext()) {
                int count = cursor.getColumnCount();
                for (int i = 0; i < count; i++) {
                    oldCol.add(cursor.getColumnName(i));
                }
                oldCol.remove("_id");
            }
            cursor.close();
            return oldCol;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据clazz创建新表，如果不存在的话
     *
     * @param db
     * @param clazz
     * @param <T>
     * @return 返回表中字段列表
     */
    private <T extends DBVO> List<String> creatTable(SQLiteDatabase db, Class<T> clazz) {
        ReflectUtil reflectUtil = new ReflectUtil();
        String tableName = getTableNameByClass(reflectUtil.getClass(clazz));
        List<Method> getMethodList = reflectUtil.obtainGetMethods(clazz);
        int length = getMethodList.size();
        if (length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(tableName);
        sb.append(" (_id INTEGER PRIMARY KEY,");

        List<String> list = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
            Method method = getMethodList.get(i);
            String methodName = method.getName();
            String returnName = method.getReturnType().getName();
            if (methodName.startsWith("get")) {
                // 其他类型的是get开头
                sb.append(methodName.substring(3).toLowerCase());
                list.add(methodName.substring(3).toLowerCase());
                if (returnName.contains("String")) {
                    sb.append(" TEXT,");
                } else {
                    sb.append(" INT,");
                }
            } else {
                // boolean类型的是is开头
                sb.append(methodName.substring(2).toLowerCase());
                list.add(methodName.substring(2).toLowerCase());
                sb.append(" INT,");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");

        db.execSQL(sb.toString());
        return list;
    }

    /**
     * 更新表中字段（添加新字段，不删除旧字段）
     *
     * @param db
     * @param clazz
     * @param <T>
     * @throws Exception
     */
    private <T extends DBVO> void updateTable(SQLiteDatabase db, Class<T> clazz) {
        ReflectUtil reflectUtil = new ReflectUtil();
        String tableName = getTableNameByClass(reflectUtil.getClass(clazz));

        List<String> oldCol = getTableColumns(db, tableName);

        if (oldCol == null) return;
        if (oldCol.size() == 0) {
            // 删除表
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            return;
        }

        List<String> curCol = creatTable(db, clazz);
        if (curCol == null) {
            return;
        }

        StringBuffer columns = new StringBuffer();
        for (int i = 0; i < curCol.size(); i++) {
            if (!oldCol.contains(curCol.get(i))) {
                columns.append(curCol.get(i)).append(",");
            }
        }
        if (columns.length() > 0) {
            columns.deleteCharAt(columns.length() - 1);
            StringBuffer sql = new StringBuffer();
            sql.append("alter table ").append(tableName).append(" add column ");
            sql.append(columns);

            db.execSQL(sql.toString());
        }

    }

    @Override
    public <T extends DBVO> long insert(SQLiteDatabase db, List<T> obj) {
        int size = 0;
        if (obj == null || (size = obj.size()) == 0) {
            return 0;
        }
        ReflectUtil reflectUtil = new ReflectUtil(obj.get(0));

        Class<T> clazz = reflectUtil.getClass(obj.get(0).getClass());

        // 表不存在时，不执行任何操作
        updateTable(db, clazz);

        String tableName = getTableNameByClass(clazz);
        List<Method> getMethodList = reflectUtil.obtainGetMethods(clazz);
        int length = getMethodList.size();

        if (new DBUtil().isExistTable(db, tableName) != null) {
            // 如果没有表先创建表
            if (creatTable(db, clazz) == null) {
                return 0;
            }
        }

        try {
            db.beginTransaction();
            for (int j = 0; j < size; j++) {
                reflectUtil = new ReflectUtil(obj.get(j));
                // 插入数据
                ContentValues values = new ContentValues();
                for (int i = 0; i < length; i++) {
                    Method method = getMethodList.get(i);
                    Class<?> returnClazz = method.getReturnType();
                    String methodName = method.getName();
                    String colName = methodName.substring(3).toLowerCase();
                    Class<?>[] paramsTypes = method.getParameterTypes();
                    Object value = reflectUtil.getter(methodName, paramsTypes);
                    try {
                        if (returnClazz.getName().contains("boolean")) {
                            values.put(methodName.substring(2).toLowerCase(), (Boolean) value);
                        } else if (returnClazz.getName().contains("int")) {
                            values.put(colName, (Integer) value);
                        } else if (returnClazz.getName().contains("float")) {
                            values.put(colName, (Float) value);
                        } else if (returnClazz.getName().contains("double")) {
                            values.put(colName, (Double) value);
                        } else {
                            values.put(colName, (String) value);
                        }
                    } catch (Exception e) {
                        try {
                            // 将复杂的对象转成字符串存储,比如list或者map
                            values.put(colName, new Gson().toJson(value));
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    }
                }
                db.insert(tableName, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return 1;
    }

    @Override
    public <T extends DBVO> long insert(SQLiteDatabase db, T obj) {
        if (obj == null) {
            return 0;
        }

        ReflectUtil reflectUtil = new ReflectUtil(obj);

        Class<T> clazz = reflectUtil.getClass(obj.getClass());

        // 表不存在时，不执行任何操作
        updateTable(db, clazz);

        String tableName = getTableNameByClass(clazz);
        List<Method> getMethodList = reflectUtil.obtainGetMethods(clazz);
        int length = getMethodList.size();

        if (new DBUtil().isExistTable(db, tableName) != null) {
            // 如果没有表先创建表
            if (creatTable(db, clazz) == null) {
                return 0;
            }
        }

        // 插入数据
        ContentValues values = new ContentValues();
        for (int i = 0; i < length; i++) {
            Method method = getMethodList.get(i);
            Class<?> returnClazz = method.getReturnType();
            String methodName = method.getName();
            String colName = methodName.substring(3).toLowerCase();
            Class<?>[] paramsTypes = method.getParameterTypes();
            Object value = reflectUtil.getter(methodName, paramsTypes);
            try {
                if (returnClazz.getName().contains("boolean")) {
                    values.put(methodName.substring(2).toLowerCase(), (Boolean) value);
                } else if (returnClazz.getName().contains("int")) {
                    values.put(colName, (Integer) value);
                } else if (returnClazz.getName().contains("float")) {
                    values.put(colName, (Float) value);
                } else if (returnClazz.getName().contains("double")) {
                    values.put(colName, (Double) value);
                } else {
                    values.put(colName, (String) value);
                }
            } catch (Exception e) {
                try {
                    // 将复杂的对象转成字符串存储,比如list或者map
                    values.put(colName, new Gson().toJson(value));
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }
        return db.insert(tableName, null, values);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DBVO> List<T> query(SQLiteDatabase db, Class<T> clazz, int count, String... keyValues) {
        if (keyValues != null && keyValues.length % 2 != 0) {
            return null;
        }
        ReflectUtil reflectUtil = new ReflectUtil();

        clazz = reflectUtil.getClass(clazz);

        String tableName = getTableNameByClass(clazz);

        List<T> list = new ArrayList<T>();
        if (new DBUtil().isExistTable(db, tableName) == null) {

            List<Method> setMethodList = reflectUtil.obtainSetMethods(clazz);
            Gson gson = new Gson();

            StringBuffer sb = new StringBuffer();
            sb.append("select * from ").append(tableName).append(addSql(0, keyValues));
            if (count > 0) {
                sb.append(" order by _id desc").append(" limit ").append(count);
            }

            Cursor cr = db.rawQuery(sb.toString(), null);

            int length = setMethodList.size();
            while (cr.moveToNext()) {
                reflectUtil = new ReflectUtil(clazz);
                for (int i = 0; i < length; i++) {
                    Method method = setMethodList.get(i);
                    String methodName = method.getName();
                    Class<?>[] paramsTypes = method.getParameterTypes();
                    Class<?> methodClazz = paramsTypes[0];
                    Field field = null;
                    String name = methodName.substring(3);
                    if (methodClazz.getName().contains("boolean")) {
                        name = methodName.substring(2);
                    }
                    try {
                        field = clazz.getDeclaredField(name.substring(0, 1).toLowerCase() + name.substring(1));
                    } catch (Exception e) {
                        try {
                            if (field == null && methodClazz.getName().contains("boolean")) {
                                field = clazz.getDeclaredField("is" + name);
                            }
                        } catch (Exception ee) {

                        }
                    }

                    try {
                        if (field != null) {
                            if (methodClazz.getName().contains("int")) {
                                int intValue = cr.getInt(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                reflectUtil.setter(methodName, intValue, paramsTypes);
                            } else if (methodClazz.getName().contains("boolean")) {
                                String tempStr = cr.getString(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                boolean booleanValue = tempStr.equals("0") ? false : true;
                                reflectUtil.setter(methodName, booleanValue, paramsTypes);
                            } else if (methodClazz.getName().contains("float")) {
                                float floatValue = cr
                                        .getFloat(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                reflectUtil.setter(methodName, floatValue, paramsTypes);
                            } else if (methodClazz.getName().contains("double")) {
                                double doubleValue = cr
                                        .getDouble(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                reflectUtil.setter(methodName, doubleValue, paramsTypes);
                            } else if (methodClazz.getName().contains("String")) {
                                String strValue = cr
                                        .getString(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                reflectUtil.setter(methodName, strValue, paramsTypes);
                            } else {
                                String strValue = cr
                                        .getString(cr.getColumnIndex(methodName.toLowerCase().substring(3)));
                                if (strValue != null) {
                                    strValue = strValue.trim();
                                    JsonReader reader = new JsonReader(new StringReader(strValue));
                                    reader.setLenient(true);
                                    Object obj = gson.fromJson(reader, field.getGenericType());
                                    reflectUtil.setter(methodName, obj, paramsTypes);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                list.add((T) reflectUtil.getObj());
            }
        }
        return list;
    }

    @Override
    public <T extends DBVO> int delete(SQLiteDatabase db, Class<T> clazz, String... keyValues) {
        ReflectUtil reflectUtil = new ReflectUtil();

        clazz = reflectUtil.getClass(clazz);
        String tableName = getTableNameByClass(clazz);

        if (keyValues != null && keyValues.length % 2 != 0) {
            return 0;
        }

        if (new DBUtil().isExistTable(db, tableName) == null) {
            StringBuffer sql = new StringBuffer();
            sql.append("delete from ");
            sql.append(tableName);

            if (keyValues != null && keyValues.length > 0) {
                sql.append(addSql(0, keyValues));
            }

            db.execSQL(sql.toString());
            return 1;
        }
        return 0;
    }

    /**
     * 更新数据库记录
     *
     * @param db
     * @param obj
     * @param setCount
     * @param keys
     * @return
     */
    @Override
    public int update(SQLiteDatabase db, DBVO obj, int setCount, String... keys) {
        if (keys == null || keys.length == 0) {
            return 0;
        }
        if (keys.length <= setCount) {
            return 0;
        }
        if (obj == null) {
            return 0;
        }

        ReflectUtil reflectUtil = new ReflectUtil(obj);

        String tableName = getTableNameByClass(reflectUtil.getClass(obj.getClass()));

        if (new DBUtil().isExistTable(db, tableName) == null) {
            StringBuffer sql = new StringBuffer();
            sql.append("update ").append(tableName).append(" set ");

            // 根据对象的值更新所有的列
            if (setCount == 0) {
                List<Method> getMethodList = reflectUtil.obtainGetMethods(obj.getClass());
                int length = getMethodList.size();
                if (length == 0) {
                    return 0;
                }
                for (int i = 0; i < length; i++) {
                    Method method = getMethodList.get(i);
                    Class<?> clazz = method.getReturnType();
                    Object object = null;
                    try {
                        object = method.invoke(obj);
                        if (object == null) continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return 0;
                    }
                    String methodName = method.getName();
                    String name = methodName.substring(3);
                    if (clazz.getName().contains("boolean")) {
                        name = methodName.substring(2);
                    }

                    Field field = null;
                    if (clazz.getName().contains("boolean")) {
                        try {
                            field = reflectUtil.getClass(obj.getClass()).getDeclaredField("is" + name);
                        } catch (Exception e) {
                            try {
                                field = reflectUtil.getClass(obj.getClass())
                                        .getDeclaredField(name.substring(0, 1).toLowerCase() + name.substring(1));
                            } catch (Exception ee) {
                                ee.printStackTrace();
                                return 0;
                            }
                        }
                        boolean booleanValue = (Boolean) object;
                        sql.append(field.getName().toLowerCase()).append(" = ").append(booleanValue ? 1 : 0);
                    } else {
                        try {
                            field = reflectUtil.getClass(obj.getClass())
                                    .getDeclaredField(name.substring(0, 1).toLowerCase() + name.substring(1));
                            if (clazz.getName().contains("String")) {
                                String strValue = (String) object;
                                sql.append(field.getName().toLowerCase()).append(" = '").append(strValue).append("'");
                            } else {
                                sql.append(field.getName().toLowerCase()).append(" = ").append(object);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                    sql.append(",");
                }
                sql.deleteCharAt(sql.length() - 1);
            } else {
                sql.append(getSql(reflectUtil, "set", obj, 0, setCount - 1, keys));
            }

            // where
            sql.append(" where ");
            sql.append(getSql(reflectUtil, "where", obj, setCount, keys.length - 1, keys));

            db.execSQL(sql.toString());
            return 1;
        }
        return 0;
    }

    private String getSql(ReflectUtil reflectUtil, String type, DBVO obj, int start, int end, String... keys) {
        if (keys == null || keys.length == 0) {
            return "";
        }
        if (keys.length <= end) {
            return "";
        }
        StringBuffer sql = new StringBuffer();

        Field field = null;
        for (int i = start; i <= end; i += 2) {
            try {
                sql.append(keys[i].toLowerCase());
                field = reflectUtil.getClass(obj.getClass()).getDeclaredField(keys[i]);
                Class<?> clazz = field.getType();
                Method method = null;
                if (clazz.getName().contains("boolean")) {
                    method = reflectUtil.getClass(obj.getClass())
                            .getDeclaredMethod("is" + keys[i].substring(0, 1).toUpperCase() + keys[i].substring(1));
                } else {
                    method = reflectUtil.getClass(obj.getClass())
                            .getDeclaredMethod("get" + keys[i].substring(0, 1).toUpperCase() + keys[i].substring(1));
                }
                Object object = method.invoke(obj);
                if (clazz.getName().contains("boolean")) {
                    boolean booleanVavlue = (Boolean) object;
                    sql.append(" = ").append(booleanVavlue ? 1 : 0);
                } else if (clazz.getName().contains("String")) {
                    String strVavlue = (String) object;
                    sql.append(" = '").append(strVavlue).append("'");
                } else {
                    sql.append(" = ").append(object);
                }
                if (type.equals("set")) {
                    sql.append(",");
                } else {
                    sql.append(" and ");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
        if (type.equals("set")) {
            return sql.substring(0, sql.length() - 1);
        } else {
            return sql.substring(0, sql.length() - 5);
        }
    }

    private String getTableNameByClass(Class<?> clazz) {
        String tableName = clazz.getName();
        if (tableName.contains(".")) {
            tableName = tableName.substring(tableName.lastIndexOf(".") + 1);
        }
        return tableName;
    }

    private StringBuffer addSql(int i, String... sql) {
        StringBuffer sb = new StringBuffer();
        if (sql != null && sql.length > 0) {
            sb.append(" where ");
            for (; i < sql.length; i += 2) {
                sb.append(sql[i]);

                if (sql[i + 1].contains(",")) {
                    // in
                    String[] valueArray = sql[i + 1].split(",");
                    sb.append(" in (");
                    for (int j = 0; j < valueArray.length; j++) {
                        sb.append("'");
                        sb.append(valueArray[j]);
                        sb.append("',");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append(")");
                } else {
                    sb.append(" = '");
                    sb.append(sql[i + 1]);
                    sb.append("'");
                }
                if (i < sql.length - 2) {
                    sb.append(" and ");
                }
            }
        }
        return sb;
    }

}
