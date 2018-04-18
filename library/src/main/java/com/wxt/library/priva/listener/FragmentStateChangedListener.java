package com.wxt.library.priva.listener;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

/**
 * Created by Administrator on 2017/12/8.
 */

public interface FragmentStateChangedListener {

    public void onFragmentAttach(Fragment fragment, Activity activity);

    public void onFragmentViewStateRestored(Fragment fragment, Bundle savedInstanceState);

    public View onFragmentCreateView(Fragment fragment, View view);

    public void onFragmentDestroyView(Fragment fragment);

    public void onFragmentResume(Fragment fragment);

    public void onFragmentPause(Fragment fragment);

    public void onFragmentDestroy(Fragment fragment);

    public void onFragmentDetach(Fragment fragment);
}
