package com.wxt.library.priva.listener;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Administrator on 2017/12/8.
 */

public interface ActivityStateChangedListener {

    public void onActivityCreated(Activity activity, Bundle savedInstanceState);

    public void onActivityResumed(Activity activity);

    public void onActivityPaused(Activity activity);

    public void onActivityStopped(Activity activity);

    public void onActivityDestroyed(Activity activity);
}
