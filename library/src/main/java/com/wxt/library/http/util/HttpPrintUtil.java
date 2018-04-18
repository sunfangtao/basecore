package com.wxt.library.http.util;

import com.wxt.library.contanst.Constant;

/**
 * Created by Administrator on 2018/3/31.
 */

public class HttpPrintUtil {

    public static final boolean isShowHttpLog(String resultType) {
        return (!resultType.equals(Constant.HttpPrivateKey.APP_CHECK)
                && !resultType.equals(Constant.HttpPrivateKey.AUTO_LOGIN)
                && !resultType.equals(Constant.HttpPrivateKey.AUTO_UPDATE)
                && !resultType.equals(Constant.HttpPrivateKey.AUTO_UPLOAD));
    }
}
