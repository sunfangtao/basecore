package com.wxt.library.priva.util;

import android.content.Context;
import android.widget.Toast;

public class MyToast {

    private Toast mToast;
    private Context context;

    public MyToast(Context context) {
        this.context = context.getApplicationContext();
        ActivityLife.getInstance(context.getApplicationContext());
    }

    public void setText(String s) {
        if (mToast == null) {
            mToast = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(s);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public void cancel() {
        if (mToast != null) {
            try {
                mToast.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
