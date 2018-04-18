package com.wxt.library.priva.util;

/**
 * 防止多点点击和快速重复点击
 *
 * @author SunFangTao
 */
public class ForbidFastClick {

    private static long lastClickTime;
    /**
     * 点击的间隔（ms）
     */
    private static int timeOffset = 300;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < timeOffset) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static void setTimeOffset(int time) {
        timeOffset = time;
    }
}
