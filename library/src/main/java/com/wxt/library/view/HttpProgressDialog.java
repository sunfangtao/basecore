package com.wxt.library.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.retention.NotProguard;

/**
 * Created by SunFangtao on 2016/8/3.
 */
public class HttpProgressDialog extends Dialog {

    private View view;
    private TextView tvMessage;
    private ImageView ivSuccess, ivFailure;
    private ImageView ivProgressSpinner;
    private AnimationDrawable adProgressSpinner;
    private AsyncTask<String, Integer, Long> task;
    private int dismissTime = 1000;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void dismiss() {
        reset();
        super.dismiss();
    }

    public HttpProgressDialog(Context context) {
        super(context, R.style.DialogTheme);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setCanceledOnTouchOutside(false);
        this.view = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        tvMessage = (TextView) view.findViewById(R.id.textview_message);
        ivSuccess = (ImageView) view.findViewById(R.id.imageview_success);
        ivFailure = (ImageView) view.findViewById(R.id.imageview_failure);
        ivProgressSpinner = (ImageView) view.findViewById(R.id.imageview_progress_spinner);

        ivProgressSpinner.setBackgroundResource(R.drawable.round_spinner);
        adProgressSpinner = (AnimationDrawable) ivProgressSpinner.getBackground();
        this.setContentView(view);
        adProgressSpinner.stop();
        adProgressSpinner.start();
    }

    public HttpProgressDialog setMessage(String message) {
        tvMessage.setText(message);
        return this;
    }

    public void dismissWithSuccess(String message, int... time) {
        showSuccessImage();
        if (message != null) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("");
        }
        if (!isShowing()) {
            show();
        }
        if (task != null) {
            task.cancel(false);
        }
        if (time.length > 0) {
            dismissHUD(time[0]);
        } else {
            dismissHUD(1000);
        }
    }

    public void dismissWithFailure(String message, int... time) {
        showFailureImage();
        if (message != null) {
            tvMessage.setText(message);
        } else {
            tvMessage.setText("");
        }
        if (task != null) {
            task.cancel(false);
        }
        if (!isShowing()) {
            show();
        }
        if (time.length > 0) {
            dismissHUD(time[0]);
        } else {
            dismissHUD(1000);
        }
    }

    protected void showSuccessImage() {
        ivProgressSpinner.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.VISIBLE);
    }

    protected void showFailureImage() {
        ivProgressSpinner.setVisibility(View.GONE);
        ivFailure.setVisibility(View.VISIBLE);
    }

    protected void dismissHUD(int time) {
        dismissTime = time < 1000 ? 1000 : time;
        task = new AsyncTask<String, Integer, Long>() {

            @Override
            protected Long doInBackground(String... params) {
                SystemClock.sleep(dismissTime);
                return null;
            }

            @Override
            protected void onPostExecute(Long result) {
                super.onPostExecute(result);
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        task.execute();
    }

    public void reset() {
        ivProgressSpinner.setVisibility(View.VISIBLE);
        ivFailure.setVisibility(View.GONE);
        ivSuccess.setVisibility(View.GONE);
        tvMessage.setText("");
        if (task != null) {
            task.cancel(false);
        }
    }
}
