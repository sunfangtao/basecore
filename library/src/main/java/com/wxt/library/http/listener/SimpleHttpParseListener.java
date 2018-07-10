package com.wxt.library.http.listener;

import com.wxt.library.http.parse.HttpParseHelper;
import com.wxt.library.retention.NotProguard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2018/3/28.
 */

@NotProguard
public class SimpleHttpParseListener<T> implements BaseHttpParseListener {

    public void onHttpSuccess(String type, JSONObject jsonObject, T obj) throws JSONException {

    }

    public void onHttpSuccess(String type, String json, T obj) throws JSONException {

    }

    public void onHttpSuccess(String type, JSONArray jsonArray, int page, int pageSize, int count, List<T> obj) throws JSONException {

    }

    public void onHttpSuccess(String type, String json, int page, int pageSize, int count, List<T> obj) throws JSONException {

    }

    public void onHttpFailure(String type, int code, String err, String resultType) {

    }

    public HttpParseHelper getParseHelper() {
        return new HttpParseHelper();
    }
}
