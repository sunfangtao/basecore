package com.wxt.library.http.listener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2018/2/10.
 */

public interface HttpParseListener extends BaseHttpParseListener {

    /**
     * 当数据有效时，交由相应的子activity去处理，方法体的具体内容由子activity实现
     *
     * @param type       http请求的唯一标示，用于区分不同的http请求;与asyncHttpResponseHandler.setType()方法中的参数一致
     * @param jsonObject http返回的数据
     */
    void onHttpSuccess(String type, JSONObject jsonObject, Object obj) throws JSONException;

    /**
     * 当数据有效时，交由相应的子activity去处理，方法体的具体内容由子activity实现
     *
     * @param type    http请求的唯一标示，用于区分不同的http请求;与asyncHttpResponseHandler.setType()方法中的参数一致
     * @param message http返回的数据
     */
    void onHttpSuccess(String type, String message, Object obj) throws JSONException;

    /**
     * 当数据有效时，交由相应的子activity去处理，方法体的具体内容由子activity实现
     *
     * @param type      http请求的唯一标示，用于区分不同的http请求;与asyncHttpResponseHandler.setType()方法中的参数一致
     * @param jsonArray http返回的数据
     */
    void onHttpSuccess(String type, JSONArray jsonArray, int page, int pageSize, int count, List<?> obj) throws JSONException;

    /**
     * 当数据有效时，交由相应的子activity去处理，方法体的具体内容由子activity实现
     *
     * @param type      http请求的唯一标示，用于区分不同的http请求;与asyncHttpResponseHandler.setType()方法中的参数一致
     * @param json http返回的数据
     */
    void onHttpSuccess(String type, String json, int page, int pageSize, int count, List<?> obj) throws JSONException;

    /**
     * 当数据无效时，交由相应的子activity去处理，方法体的具体内容由子activity实现
     *
     * @param code 状态码
     * @param type http请求的唯一标示，用于区分不同的http请求;与asyncHttpResponseHandler.setType()方法中的参数一致
     * @param err  错误信息
     */
    void onHttpFailure(String type, int code, final String err, final String resultType);

}
