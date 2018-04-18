package com.wxt.library.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.wxt.library.priva.util.ActivityLife;
import com.wxt.library.retention.NotProguard;
import com.wxt.library.sql.implement.DBOperListenerImpl;
import com.wxt.library.sql.listener.DBOperListener;
import com.wxt.library.sql.model.DBVO;

import java.lang.reflect.Method;
import java.util.List;

@NotProguard
public class DBHelper {

    private static DBHelper instance = null;
    // 数据库名称
    private static String dbName = null;
    // 数据库当前版本
    private static int dbVersion = 1;
    //
    private DatabaseHelper helper;
    //
    private DBOperListener dbOperListener;

    private DBHelper(Context context) {
        dbName = context.getPackageName().replace(".", "");
        helper = new DatabaseHelper(context);
        dbOperListener = new DBOperListenerImpl();
        ActivityLife.getInstance(context.getApplicationContext());
    }

    @NotProguard
    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DBHelper.class) {
                if (instance == null) {
                    instance = new DBHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public <T extends DBVO> long updateInsert(T t) {
        String key = t.findKeyForVO();
        if (TextUtils.isEmpty(key)) {
            return insert(t);
        } else {
            try {
                Method method = t.getClass().getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
                Object value = method.invoke(t);
                int result = update(t, 0, key, String.valueOf(value));
                if (result > 0) {
                    return result;
                } else {
                    return insert(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public <T extends DBVO> long deleteInsert(T t) {
        String key = t.findKeyForVO();
        if (TextUtils.isEmpty(key)) {
            return insert(t);
        } else {
            try {
                Method method = t.getClass().getMethod("get" + key.substring(0, 1).toUpperCase() + key.substring(1));
                Object value = method.invoke(t);
                delete(t.getClass(), key.toLowerCase(), (String) value);
                return insert(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public long insert(DBVO obj) {
        return dbOperListener.insert(getWritableDB(), obj);
    }

    public <T extends DBVO> List<T> query(Class<T> clazz, String... keyValues) {
        return query(clazz, 0, keyValues);
    }

    public <T extends DBVO> List<T> query(Class<T> clazz, int count, String... keyValues) {
        return dbOperListener.query(getWritableDB(), clazz, count, keyValues);
    }

    public <T extends DBVO> int delete(Class<T> clazz, String... keyValues) {
        return dbOperListener.delete(getWritableDB(), clazz, keyValues);
    }

    public <T extends DBVO> int update(DBVO obj, int setCount, String... keys) {
        return dbOperListener.update(getWritableDB(), obj, setCount, keys);
    }

    private SQLiteDatabase getWritableDB() {
        return helper.getWritableDatabase();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, dbName, null, dbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        }
    }
}
