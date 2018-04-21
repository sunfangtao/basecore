package com.wxt.library.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.contanst.Constant;
import com.wxt.library.guideviewpager.GuideBanner;
import com.wxt.library.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2018/2/9.
 * 不支持自定义外层布局
 * TODO 引导页面(引导页内容暂不支持有网络提供)
 * 点击立即体验按钮时，必须调用closeGuide()方法
 */

public abstract class BaseGuideActivity extends BaseParseHelperActivity {

    private Class guideClazz;
    private Class loginClazz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.guideClazz = (Class) getIntent().getSerializableExtra(Constant.IntentKey.INDEX_ACTIVITY);
        this.loginClazz = (Class) getIntent().getSerializableExtra(Constant.IntentKey.LOGIN_ACTIVITY);
        setContentView(R.layout.activity_guide);
        setBannerView((GuideBanner) findViewById(R.id.banner_guide_viewpager));
    }

    private void setBannerView(GuideBanner bannerView) {
        List<View> viewList = setGuideViews(LayoutInflater.from(this));
        if (viewList != null && viewList.size() > 0) {
            bannerView.setData(viewList);
        }
    }

    protected abstract List<View> setGuideViews(LayoutInflater inflater);

    protected final void closeGuide() {
        Intent intent = new Intent(this, this.loginClazz == null ? this.guideClazz : this.loginClazz);
        startActivity(intent);
        sharedPreferenceUtil.saveParam(Util.getApplicationName(this), Constant.SharePreferenceKey.FIRST_RUN_APP, Constant.IS_SHOW_GUIDE);
        finish();
    }

}
