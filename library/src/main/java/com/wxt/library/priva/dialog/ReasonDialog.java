package com.wxt.library.priva.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.priva.util.ForbidFastClick;

import java.util.ArrayList;
import java.util.List;

public class ReasonDialog implements OnClickListener, DialogInterface.OnDismissListener {

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (listener != null) {
            listener.onDialogDismiss(isClickConfirm);
        }
    }

    public interface PermissionReasonDialogDismissListener {
        void onDialogDismiss(boolean isConfirm);
    }

    private Dialog dialog;
    private TextView titleTv;
    private RecyclerView contentRecyclerView;
    private Button cancleBtn, confirmBtn;

    private List<String> list;
    private RecyclerViewAdapter adapter;
    private boolean isClickConfirm;

    private PermissionReasonDialogDismissListener listener;

    public ReasonDialog setDismissListener(PermissionReasonDialogDismissListener listener) {
        this.listener = listener;
        return this;
    }

    public ReasonDialog(Context context) {
        create(context);
    }

    private void create(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialog = new Dialog(context, R.style.Update_Dialog);

        dialog.setCanceledOnTouchOutside(false);

        View layout = inflater.inflate(R.layout.update_dialog, null);
        titleTv = (TextView) layout.findViewById(R.id.update_dialog_title_tv);
        contentRecyclerView = (RecyclerView) layout.findViewById(R.id.update_dialog_recyclerview);
        cancleBtn = (Button) layout.findViewById(R.id.update_dialog_cancle);
        confirmBtn = (Button) layout.findViewById(R.id.update_dialog_confirm);

        cancleBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

        cancleBtn.setVisibility(View.GONE);
        confirmBtn.setText("再次申请");

        dialog.setOnDismissListener(this);

        dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        dialog.setContentView(layout);

        Window window = dialog.getWindow();
        // 设置窗口的大小
        window.setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.8f), dialog.getWindow().getAttributes().height);


        list = new ArrayList<>();
        adapter = new RecyclerViewAdapter(context, list);
        contentRecyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        contentRecyclerView.setAdapter(adapter);
    }

    public ReasonDialog setTitle(String title) {
        if (!TextUtils.isEmpty(title))
            titleTv.setText(title);
        return this;
    }

    public ReasonDialog setContent(String content, String split) {
        if (TextUtils.isEmpty(content))
            return this;
        if (TextUtils.isEmpty(split))
            split = "@";
        String[] ss = content.split(split);

        list.clear();
        for (int i = 0; i < ss.length; i++) {
            list.add(ss[i]);
        }
        adapter.notifyDataSetChanged();
        return this;
    }

    public ReasonDialog setContent(List<String> list) {
        if (list == null || list.size() == 0) {
            return this;
        }
        this.list.clear();
        for (int i = 0; i < list.size(); i++) {
            this.list.add(list.get(i));
        }
        adapter.notifyDataSetChanged();
        return this;
    }

    public void dismiss(){
        dialog.setOnDismissListener(null);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void show() {
        if (list.size() == 0) {
            list.add("新版本发布了！快来体验吧！");
            adapter.notifyDataSetChanged();
        }

        contentRecyclerView.measure(0, 0);
        int height = Math.min(contentRecyclerView.getMeasuredHeight(), 260);
        contentRecyclerView.getLayoutParams().height = height;

        if (dialog != null) {
            dialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        if (ForbidFastClick.isFastDoubleClick()) {
            return;
        }
        if (v.getId() == R.id.update_dialog_cancle) {
            isClickConfirm = false;
            dialog.dismiss();
        } else if (v.getId() == R.id.update_dialog_confirm) {
            isClickConfirm = true;
            dialog.dismiss();
        }
    }

    private class RecyclerViewAdapter extends BaseAdapter {

        public RecyclerViewAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public int onCreateViewLayoutID(int viewType) {
            return R.layout.upload_dialog_item;
        }

        @Override
        public void onBindViewHolder(int viewType, View view, final int position) {
            TextView textView = (TextView) view.findViewById(R.id.upload_dialog_item_tv);
            // 设置
            textView.setText((String) getObjcet(position));
        }
    }
}
