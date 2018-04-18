package com.wxt.library.priva.listener;

/**
 * Created by Administrator on 2018/2/28.
 */

public interface HttpReturnListener {

    public void onHttpSuccessForPrivate(String type, byte[] responseBody);

    public void onHttpFailForPrivate(String type, int statusCode, byte[] responseBody);

}
