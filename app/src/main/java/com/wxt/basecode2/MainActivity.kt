package com.wxt.basecode2

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wxt.library.base.activity.BaseActivity
import com.wxt.library.model.MUtilItemData
import com.wxt.library.selectcity.SelectCityActivity
import com.wxt.library.selectimage.Constants
import com.wxt.library.selectimage.activity.AlbumSelectActivity
import com.wxt.library.util.Util
import com.wxt.library.view.MutilItemView

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

        val list: ArrayList<MUtilItemData> = ArrayList()
        val data1 = MUtilItemData()
        data1.leftDrawableId = R.mipmap.ic_launcher_round
        data1.title = "哈哈"
        data1.subTitle = "111"
        data1.descricption = "不告诉你"
        data1.rightDrawableId = R.mipmap.ic_launcher
        list.add(data1)

        val data2 = MUtilItemData()
        data2.leftDrawableId = R.mipmap.ic_launcher_round
        data2.title = "哈哈"
        data1.subTitle = "111"
        data2.descricption = "不告诉你"
        data2.rightDrawableId = R.mipmap.ic_launcher
        list.add(data2)

        val data3 = MUtilItemData()
//        data3.leftDrawableId = R.mipmap.ic_launcher_round
        data3.title = "哈哈"
        data3.subTitle = "111"
        data3.descricption = "不告诉你"
//        data3.rightDrawableId = R.mipmap.ic_launcher
        list.add(data3)

        val data4 = MUtilItemData()
        data4.leftDrawableId = R.mipmap.ic_launcher_round
        data4.title = "哈哈"
        data4.subTitle = "111"
//        data4.descricption = "不告诉你"
        data4.rightDrawableId = R.mipmap.ic_launcher
        list.add(data4)

        findViewById<MutilItemView>(R.id.mutilitem).setDatas(list)

        findViewById<MutilItemView>(R.id.mutilitem).setItemClickListener {
            Util.print("it="+it)
        }
//        mutilitem.setDatas(list)
        val intent = Intent(this, SelectCityActivity::class.java).putExtra(SelectCityActivity.LOCATION_CITY, "青岛")
                .putExtra(Constants.INTENT_EXTRA_LIMIT, 2).putExtra(AlbumSelectActivity.INTENT_EXTRA_COLUMN, 3)
        startActivityForResult(intent, 1)
    }

}