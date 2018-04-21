package com.wxt.basecode2

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import com.wxt.library.base.activity.BaseActivity
import com.wxt.library.selectcity.SelectCityActivity
import com.wxt.library.selectimage.Constants
import com.wxt.library.selectimage.activity.AlbumSelectActivity
import com.wxt.library.util.MyHandler

/**
 * Created by Administrator on 2018/4/20.
 */
/**
 * APP主界面
 * 在WelcomeActivity中返回MainActivity.class进行关联绑定
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, AlbumSelectActivity::class.java).putExtra(SelectCityActivity.LOCATION_CITY, "青岛")
                .putExtra(Constants.INTENT_EXTRA_LIMIT, 2).putExtra(AlbumSelectActivity.INTENT_EXTRA_COLUMN, 3)
        startActivityForResult(intent, 1)
    }

}