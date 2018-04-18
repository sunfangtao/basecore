package com.wxt.library.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目 adcollection
 * Created by SunFangtao on 2016/8/16.
 */
public class PermissionRequestUtil {

    //group:android.permission-group.LOCATION
    public static final int ACCESS_COARSE_LOCATION = 0x00;
    public static final int ACCESS_FINE_LOCATION = 0x01;

    //group:android.permission-group.CONTACTS
    public static final int WRITE_CONTACTS = 0x02;
    public static final int GET_ACCOUNTS = 0x03;
    public static final int READ_CONTACTS = 0x04;

    // group:android.permission-group.PHONE
    public static final int READ_CALL_LOG = 0x05;
    public static final int READ_PHONE_STATE = 0x06;
    public static final int CALL_PHONE = 0x07;
    public static final int WRITE_CALL_LOG = 0x08;
    public static final int USE_SIP = 0x09;
    public static final int PROCESS_OUTGOING_CALLS = 0x0a;
    public static final int ADD_VOICEMAIL = 0x0b;

    //group:android.permission-group.CALENDAR
    public static final int READ_CALENDAR = 0x0c;
    public static final int WRITE_CALENDAR = 0x0d;

    //group:android.permission-group.CAMERA
    public static final int CAMERA = 0x0e;

    //group:android.permission-group.SENSORS
    public static final int BODY_SENSORS = 0x0f;

    //group:android.permission-group.STORAGE
    public static final int READ_EXTERNAL_STORAGE = 0x10;
    public static final int WRITE_EXTERNAL_STORAGE = 0x11;

    //group:android.permission-group.MICROPHONE
    public static final int RECORD_AUDIO = 0x12;

    //group:android.permission-group.SMS
    public static final int READ_SMS = 0x13;
    public static final int RECEIVE_WAP_PUSH = 0x14;
    public static final int RECEIVE_MMS = 0x15;
    public static final int SEND_SMS = 0x16;
    public static final int READ_CELL_BROADCASTS = 0x17;

    private static Map<Integer, String> permissionMap = new HashMap<>();
    private static PermissionRequestUtil instance;

    public static PermissionRequestUtil getInstance() {
        if (instance == null) {
            synchronized (PermissionRequestUtil.class) {
                if (instance == null) {
                    instance = new PermissionRequestUtil();
                }
            }
        }
        return instance;
    }

    private PermissionRequestUtil() {
        permissionMap.put(ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionMap.put(ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);

        permissionMap.put(WRITE_CONTACTS, Manifest.permission.WRITE_CONTACTS);
        permissionMap.put(GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
        permissionMap.put(READ_CONTACTS, Manifest.permission.READ_CONTACTS);

        permissionMap.put(READ_CALL_LOG, Manifest.permission.READ_CALL_LOG);
        permissionMap.put(READ_PHONE_STATE, Manifest.permission.READ_PHONE_STATE);
        permissionMap.put(CALL_PHONE, Manifest.permission.CALL_PHONE);
        permissionMap.put(WRITE_CALL_LOG, Manifest.permission.WRITE_CALL_LOG);
        permissionMap.put(USE_SIP, Manifest.permission.USE_SIP);
        permissionMap.put(PROCESS_OUTGOING_CALLS, Manifest.permission.PROCESS_OUTGOING_CALLS);
        permissionMap.put(ADD_VOICEMAIL, Manifest.permission.ADD_VOICEMAIL);

        permissionMap.put(READ_CALENDAR, Manifest.permission.READ_CALENDAR);
        permissionMap.put(WRITE_CALENDAR, Manifest.permission.WRITE_CALENDAR);

        permissionMap.put(CAMERA, Manifest.permission.CAMERA);

        permissionMap.put(BODY_SENSORS, Manifest.permission.BODY_SENSORS);

        permissionMap.put(READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionMap.put(WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissionMap.put(RECORD_AUDIO, Manifest.permission.RECORD_AUDIO);

        permissionMap.put(READ_SMS, Manifest.permission.READ_SMS);
        permissionMap.put(RECEIVE_WAP_PUSH, Manifest.permission.RECEIVE_WAP_PUSH);
        permissionMap.put(RECEIVE_MMS, Manifest.permission.RECEIVE_MMS);
        permissionMap.put(SEND_SMS, Manifest.permission.SEND_SMS);
    }

    public boolean requestPermission(Activity activity, int... requestCode) {

        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }

        int length = requestCode.length;
        if (length == 0) {
            return false;
        }

        List<String> permissionList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            String permission = permissionMap.get(requestCode[i]);
            if (TextUtils.isEmpty(permission)) {
                // 不识别的权限
                return false;
            }
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }

        if (permissionList.size() == 0) {
            return true;
        }

        length = permissionList.size();
        String[] permissions = new String[length];
        for (int i = 0; i < length; i++) {
            permissions[i] = permissionList.get(i);
        }

        //  ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions)
        //申请权限
        ActivityCompat.requestPermissions(activity, permissions, requestCode[0]);//自定义的code
        return false;
    }
}
