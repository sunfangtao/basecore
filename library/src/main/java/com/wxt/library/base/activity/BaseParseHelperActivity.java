package com.wxt.library.base.activity;

import android.os.Bundle;

import com.wxt.library.http.listener.HttpParseListener;
import com.wxt.library.http.parse.HttpParseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 基于BaseActivity 实现Http 返回数据的解析功能，解析类可以自定义
 * Created by Administrator on 2018/2/10.
 */

public abstract class BaseParseHelperActivity extends BaseActivity implements HttpParseListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public HttpParseHelper getParseHelper() {
        return new HttpParseHelper();
    }

    @Override
    public void onHttpSuccess(final String type, final JSONObject jsonObject, Object obj) throws JSONException {

    }

    @Override
    public void onHttpSuccess(final String type, final JSONArray jsonArray, final int page, final int pageSize, final int count, List obj) throws JSONException {

    }

    @Override
    public void onHttpSuccess(final String type, final String message, Object obj) throws JSONException {

    }

    @Override
    public void onHttpFailure(final String type, final int code, final String err, final String resultType) {

    }

    @Override
    public void onHttpSuccess(String type, String json, int page, int pageSize, int count, List<?> obj) throws JSONException {

    }

}
