package com.wxt.library.http.util;

import com.wxt.library.contanst.Constant;

/**
 * Created by Administrator on 2018/3/31.
 */

public class HttpPrintUtil {

    public static boolean isShowHttpLog = true;

    public static final boolean isShowHttpLog(String resultType) {
        if (resultType.equals(Constant.HttpPrivateKey.APP_CHECK)
                || resultType.equals(Constant.HttpPrivateKey.AUTO_LOGIN)
                || resultType.equals(Constant.HttpPrivateKey.AUTO_UPDATE)
                || resultType.equals(Constant.HttpPrivateKey.AUTO_UPLOAD)) {
            return isShowHttpLog;
        }
        return true;
    }
}
