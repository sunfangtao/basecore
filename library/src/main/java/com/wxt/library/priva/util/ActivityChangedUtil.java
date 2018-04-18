package com.wxt.library.priva.util;

import com.wxt.library.crash.CrashParams;
import com.wxt.library.priva.listener.ActivityStateChangedListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/8.
 */

public class ActivityChangedUtil {

    private static ActivityChangedUtil util;

    public Set<ActivityStateChangedListener> changedListenerList;

    public static ActivityChangedUtil getInstance() {
        if (util == null) {
            synchronized (CrashParams.class) {
                if (util == null) {
                    util = new ActivityChangedUtil();
                }
            }
        }
        return util;
    }

    public void addActivityChangedListener(ActivityStateChangedListener listener) {
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        changedListenerList.add(listener);
    }

    public void removeActivityChangedListener(ActivityStateChangedListener listener) {
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        changedListenerList.remove(listener);
    }

    public Set<ActivityStateChangedListener> getChangedListener(){
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        return changedListenerList;
    }
}
