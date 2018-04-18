package com.wxt.library.priva.util;

import com.wxt.library.crash.CrashParams;
import com.wxt.library.priva.listener.ActivityStateChangedListener;
import com.wxt.library.priva.listener.FragmentStateChangedListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/12/8.
 */

public class FragmentChangedUtil {

    private static FragmentChangedUtil util;

    public Set<FragmentStateChangedListener> changedListenerList;

    public static FragmentChangedUtil getInstance() {
        if (util == null) {
            synchronized (CrashParams.class) {
                if (util == null) {
                    util = new FragmentChangedUtil();
                }
            }
        }
        return util;
    }

    public void addFragmentChangedListener(FragmentStateChangedListener listener) {
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        changedListenerList.add(listener);
    }

    public void removeFragmentChangedListener(FragmentStateChangedListener listener) {
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        changedListenerList.remove(listener);
    }

    public Set<FragmentStateChangedListener> getChangedListener(){
        if (changedListenerList == null) {
            changedListenerList = new HashSet<>();
        }
        return changedListenerList;
    }
}
