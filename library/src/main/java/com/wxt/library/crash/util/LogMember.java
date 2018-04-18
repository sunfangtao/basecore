package com.wxt.library.crash.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Administrator on 2017/6/30.
 */
public final class LogMember {

    private final int DEFAULT_SUM = 30;

    private Queue<String> queue;
    private SimpleDateFormat format;
    private int member_count = DEFAULT_SUM;

    private static LogMember instance;

    public final static LogMember getInstance() {
        if (instance == null) {
            synchronized (LogMember.class) {
                if (instance == null) {
                    instance = new LogMember();
                }
            }
        }
        return instance;
    }

    private LogMember() {
        init();
        format = new SimpleDateFormat("HH:mm:ss");
    }

    public void setMemberCount(int count) {
        member_count = count;
    }

    public final void init() {
        if (queue == null) {
            queue = new LinkedList<String>();
        } else {
            queue.clear();
        }
    }

    public final void add(String log) {
        int size = queue.size();
        if (size >= member_count) {
            queue.poll();
        }
        queue.offer(format.format(new Date()) + " :" + log + "\n");
    }

    public final String get() {
        return queue.poll();
    }
}
