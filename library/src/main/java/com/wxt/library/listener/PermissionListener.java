package com.wxt.library.listener;

import java.util.List;

/**
 * Created by Administrator on 2018/3/2.
 */

public interface PermissionListener {

    List<Integer> getPermissionList();

    List<String> getPermissionReason();

    int getObtainCount();
}
