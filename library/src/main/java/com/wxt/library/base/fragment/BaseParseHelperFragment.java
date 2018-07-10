package com.wxt.library.base.fragment;

import com.wxt.library.http.listener.HttpParseListener;
import com.wxt.library.http.parse.HttpParseHelper;
import com.wxt.library.retention.NotProguard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2018/2/10.
 */

public class BaseParseHelperFragment extends BaseFragment implements HttpParseListener {

    @Override
    public void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws JSONException {

    }

    @Override
    public void onHttpSuccess(String type, JSONArray jsonArray, int page, int pageSize, int count, List obj) throws JSONException {

    }

    @Override
    public void onHttpSuccess(String type, String message, Object obj) throws JSONException {

    }

    @Override
    public void onHttpFailure(String type, int code, String err, String resultType) {

    }

    @Override
    public void onHttpSuccess(String type, String json, int page, int pageSize, int count, List<?> obj) throws JSONException {

    }

    @Override
    public HttpParseHelper getParseHelper() {
        return new HttpParseHelper();
    }
}
