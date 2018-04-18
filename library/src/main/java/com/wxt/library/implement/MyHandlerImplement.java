package com.wxt.library.implement;

import android.os.Handler;
import android.os.Message;

import com.wxt.library.util.MyHandler;

/**
 * 用于进行定时的操作，共4中构造方法可选择
 * 
 * @author SunFangTao
 * @Date 2014-6-12
 */
public class MyHandlerImplement {

	private Handler handler;
	private Runnable runnable;
	private int time;
	// 定时任务是否被取消
	private boolean handlerIsCancle;
	// 是否循环执行
	private boolean isLoopRun = false;
	// 循环执行的时间间隔
	private int loopTime = 0;
	
	private MyHandler handl;

	/**
	 * 立即执行重写的run方法
	 */
	public MyHandlerImplement(MyHandler handl) {
		this.time = 0;
		this.handl = handl;
		init(true);
	}

	/**
	 * @param isRun
	 *            true表示立即执行重写的run方法，false手动调用post执行重写的run方法
	 */
	public MyHandlerImplement(boolean isRun, MyHandler handl) {
		this.time = 0;
		this.handl = handl;
		init(isRun);
	}

	/**
	 * 立即循环执行重写的run方法
	 * 
	 * @param isLoopRun
	 *            是否循环执行
	 * @param loopTime
	 *            循环执行的时间间隔，不大于0时不循环
	 * @注 isLoopRun = false;loopTime<=0时需手动调用post执行
	 */
	public MyHandlerImplement(boolean isLoopRun, int loopTime, MyHandler handl) {
		if (loopTime <= 0) {
			this.time = 0;
			this.handl = handl;
			init(isLoopRun);
		} else {
			this.loopTime = loopTime;
			this.time = 0;
			this.handl = handl;
			this.isLoopRun = isLoopRun;
			init(true);
		}
	}

	/**
	 * 延迟一段时间后自动执行重写的run()方法，如果isLoopRun=true,则循环执行时间默认为延迟的时间
	 * 
	 * @param delayedTime
	 *            自动执行的延迟时间,如果不大于零，则只运行一次，不循环执行
	 */
	public MyHandlerImplement(int delayedTime, boolean isLoopRun, MyHandler handl) {
		if (delayedTime <= 0) {
			this.time = 0;
			this.handl = handl;
			init(isLoopRun);
		} else {
			this.time = delayedTime;
			this.handl = handl;
			this.loopTime = delayedTime;
			this.isLoopRun = isLoopRun;
			init(true);
		}
	}

	/**
	 * 延迟一段时间后自动执行重写的run()方法
	 * 
	 * @param delayedTime
	 *            自动执行的延迟时间
	 * @param isLoopRun
	 *            是否循环执行
	 * @param loopTime
	 *            自动执行时间间隔
	 */
	public MyHandlerImplement(int delayedTime, boolean isLoopRun, int loopTime, MyHandler handl) {
		if (loopTime <= 0) {
			new MyHandlerImplement(delayedTime, isLoopRun,handl);
		} else if (delayedTime <= 0) {
			new MyHandlerImplement(isLoopRun, loopTime,handl);
		} else {
			this.time = delayedTime;
			this.handl = handl;
			this.loopTime = loopTime;
			init(true);
			this.isLoopRun = isLoopRun;
		}
	}

	public MyHandlerImplement(int delayedTime, MyHandler handl) {
		this.time = delayedTime;
		this.handl = handl;
		init(true);
	}

	private void init(boolean isRun) {

		handlerIsCancle = false;

		this.handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (!handlerIsCancle) {
					run();
				} else {
					isLoopRun = false;
				}
				if (isLoopRun) {
					handler.postDelayed(runnable, loopTime);
				}
			}
		};

		this.runnable = new Runnable() {

			@Override
			public void run() {
				Message msg = handler.obtainMessage();
				msg.sendToTarget();
			}
		};
		if (isRun) {
			this.post();
		}
	}

	/**
	 * 手动执行重写的run()方法
	 */
	public void post() {
		if (time == 0) {
			this.handler.post(this.runnable);
		} else {
			this.handler.postDelayed(this.runnable, time);
		}
	}

	/**
	 * 重写方法，执行自己的操作
	 * 
	 * @author SunFangTao
	 */
	public void run() {
		handl.run();
	};

	/**
	 * 取消定时操作
	 */
	public void cancle() {
		handlerIsCancle = true;
		this.handler.removeCallbacks(this.runnable);
	}

	public int getLoopTime() {
		return loopTime;
	}

	public void setLoopTime(int loopTime) {
		this.loopTime = loopTime;
	}
}
