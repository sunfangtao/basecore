package com.wxt.library.util;

import com.wxt.library.implement.MyHandlerImplement;

/**
 * 用于进行定时的操作
 *
 * @author SunFangTao
 */
public class MyHandler {

    private MyHandlerImplement handlerImplement;

    /**
     * 立即执行重写的run方法
     */
    public MyHandler() {
        handlerImplement = new MyHandlerImplement(this);
    }

    /**
     * @param isRun true表示立即执行重写的run方法，false手动调用post执行重写的run方法
     */
    public MyHandler(boolean isRun) {
        handlerImplement = new MyHandlerImplement(isRun, this);
    }

    /**
     * 延时loopTime后循环执行重写的run方法
     *
     * @param isLoopRun 是否循环执行
     * @param loopTime  循环执行的时间间隔，不大于0时不循环
     * @注 isLoopRun = false;loopTime<=0时需手动调用post执行
     */
    public MyHandler(boolean isLoopRun, int loopTime) {
        handlerImplement = new MyHandlerImplement(isLoopRun, loopTime, this);
    }

    /**
     * 延迟一段时间后自动执行重写的run()方法，如果isLoopRun=true,则循环执行时间默认为延迟的时间
     *
     * @param delayedTime 自动执行的延迟时间,如果不大于零，则只运行一次，不循环执行
     */
    public MyHandler(int delayedTime, boolean isLoopRun) {
        handlerImplement = new MyHandlerImplement(delayedTime, isLoopRun, this);
    }

    /**
     * 延迟一段时间后自动执行重写的run()方法
     *
     * @param delayedTime 自动执行的延迟时间
     * @param isLoopRun   是否循环执行
     * @param loopTime    自动执行时间间隔
     */
    public MyHandler(int delayedTime, boolean isLoopRun, int loopTime) {
        handlerImplement = new MyHandlerImplement(delayedTime, isLoopRun,
                loopTime, this);
    }

    public MyHandler(int delayedTime) {
        handlerImplement = new MyHandlerImplement(delayedTime, this);
    }

    public void run() {

    }

    /**
     * 手动执行重写的run()方法
     */
    public void post() {
        handlerImplement.post();
    }

    /**
     * 取消定时操作
     */
    public void cancle() {
        handlerImplement.cancle();
    }

    public int getLoopTime() {
        return handlerImplement.getLoopTime();
    }

    public void setLoopTime(int loopTime) {
        handlerImplement.setLoopTime(loopTime);
    }
}
