package com.wxt.basecode2

import android.content.Intent
import android.os.Bundle
import com.wxt.library.base.activity.BaseActivity
import com.wxt.library.selectcity.SelectCityActivity
import com.wxt.library.selectcity.model.City
import com.wxt.library.util.Util

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_welcome)
        super.onCreate(savedInstanceState)

        val intent = Intent(this, SelectCityActivity::class.java).putExtra(SelectCityActivity.LOCATION_CITY, "青岛")
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Util.print(data?.getSerializableExtra(SelectCityActivity.SELECT_KEY) as City)
    }
}
