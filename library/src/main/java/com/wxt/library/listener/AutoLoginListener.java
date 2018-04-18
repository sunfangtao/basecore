package com.wxt.library.listener;

import org.json.JSONObject;

/**
 * Created by Administrator on 2018/2/10.
 */

public interface AutoLoginListener<T> {
    void parseLogin(boolean isSuccess, JSONObject jsonObject, T obj, String message);
}
