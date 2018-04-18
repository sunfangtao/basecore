package com.wxt.library.util;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.wxt.library.retention.NotProguard;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * JSON 工具类
 */
@NotProguard
public class JSONUtil {

    /**
     * String到JavaBean
     *
     * @param javabean javaBean
     * @throws Exception
     */
    public static <T> T toJavaBean(Class<T> javabean, String jsonString) throws Exception {
        return new Gson().fromJson(jsonString, javabean);
    }

    /**
     * JSONObject到JavaBean
     *
     * @param javabean
     * @param jsonObject
     * @throws Exception
     */
    public static <T> T toJavaBean(Class<T> javabean, JSONObject jsonObject) throws Exception {
        return toJavaBean(javabean, jsonObject.toString());
    }

    /**
     * JavaBean到String
     *
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        return new Gson().toJson(object);
    }

    /**
     * String到List
     *
     * @param javabean
     * @param jsonArray
     * @return
     */
    public static <T> ArrayList<T> toJavaBeanList(Class<T> javabean, String jsonArray) throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement el = parser.parse(jsonArray);
        JsonArray json = null;
        if (el.isJsonArray()) {
            json = el.getAsJsonArray();
        }
        ArrayList<T> list = new ArrayList<T>();
        int length = json.size();
        for (int i = 0; i < length; i++) {
            T t = toJavaBean(javabean, json.get(i).toString());
            list.add(t);
        }
        return list;
    }

    /**
     * JSONArray到List
     *
     * @param javabean
     * @param jsonArray
     * @return
     */
    public static <T> ArrayList<T> toJavaBeanList(Class<T> javabean, JSONArray jsonArray) throws Exception {
        ArrayList<T> list = new ArrayList<T>();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            T t = toJavaBean(javabean, jsonArray.getJSONObject(i));
            list.add(t);
        }
        return list;
    }

}
