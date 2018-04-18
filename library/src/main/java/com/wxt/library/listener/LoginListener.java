package com.wxt.library.listener;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Administrator on 2018/2/10.
 */

public interface LoginListener extends Serializable {

    /**
     * 登录成功后操作
     */
    void afterLogin(JSONObject jsonObject, Object obj);
}
