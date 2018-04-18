package com.wxt.library.sql.listener;

import android.database.sqlite.SQLiteDatabase;

import com.wxt.library.sql.model.DBVO;

import java.util.List;

public interface DBOperListener {

    public <T extends DBVO> long insert(SQLiteDatabase db, T obj);

    public <T extends DBVO> List<T> query(SQLiteDatabase db, Class<T> clazz, int count, String... keyValues);

    public <T extends DBVO> int delete(SQLiteDatabase db, Class<T> clazz, String... keyValues);

    public <T extends DBVO> int update(SQLiteDatabase db, T obj, int setCount, String... keyValues);
}
