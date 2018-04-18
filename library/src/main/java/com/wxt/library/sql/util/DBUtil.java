package com.wxt.library.sql.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.wxt.library.retention.NotProguard;

@NotProguard
public class DBUtil {

	/**
	 * 表是否存在
	 * 
	 * @param tableName
	 *            
	 * @return
	 */
	@NotProguard
	public String isExistTable(SQLiteDatabase db, String tableName) {
		if (TextUtils.isEmpty(tableName)) {
			return "table name error!";
		}
		String result = null;
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
					+ tableName + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
				} else {
					result = "table not exist";
				}
			} else {
				result = "table not exist";
			}
			cursor.close();

		} catch (Exception e) {
			cursor.close();
			result = "exception";
		}
		return result;
	}
}
