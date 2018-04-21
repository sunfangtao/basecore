package com.wxt.basecode2

import android.view.LayoutInflater
import android.view.View
import com.wxt.library.base.activity.BaseGuideActivity
import java.util.ArrayList

/**
 * 引导页
 * 必须继承BaseGuideActivity
 * 需要实现setGuideViews，用于返回引导页面显示的数据 List大小就是页面个数
 * 完成引导功能后，需要调用closeGuide()方法，否则将认为下次仍然需要引导
 * 实现GuideListener的类中返回GuideActivity.class进行关联绑定
 */
class GuideActivity : BaseGuideActivity() {

    override fun setGuideViews(inflater: LayoutInflater): List<View> {
        val list = ArrayList<View>()
        // 最后一个引导页面，需要实现点击关闭引导页面功能
        val view: View = inflater.inflate(R.layout.item_receiver_select_title, null, false)

        list.add(inflater.inflate(R.layout.item_shop, null, false))
        list.add(view)

        view.findViewById<View>(R.id.check_box).setOnClickListener { closeGuide() }

        return list
    }

}