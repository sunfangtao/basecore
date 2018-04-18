package com.wxt.library.http.listener;

import com.wxt.library.http.parse.HttpParseHelper;

/**
 * Created by Administrator on 2018/4/9.
 */

public interface BaseHttpParseListener {
    /**
     * 设置返回数据的解析方法
     *
     * @return
     */
    HttpParseHelper getParseHelper();
}
