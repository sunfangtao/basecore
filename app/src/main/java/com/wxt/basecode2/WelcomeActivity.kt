package com.wxt.basecode2

import android.content.Intent
import android.os.Bundle
import com.wxt.library.base.activity.BaseActivity
import com.wxt.library.selectcity.SelectCityActivity
import com.wxt.library.selectcity.model.City
import com.wxt.library.selectimage.Constants
import com.wxt.library.selectimage.activity.AlbumSelectActivity
import com.wxt.library.selectimage.models.Image
import com.wxt.library.util.Util

class WelcomeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_welcome)
        super.onCreate(savedInstanceState)

        val intent = Intent(this, AlbumSelectActivity::class.java).putExtra(SelectCityActivity.LOCATION_CITY, "青岛")
                .putExtra(Constants.INTENT_EXTRA_LIMIT, 2)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Util.print(data?.getSerializableExtra(SelectCityActivity.SELECT_KEY) as City)
//        Util.print((data?.getSerializableExtra(AlbumSelectActivity.INTENT_EXTRA_IMAGES) as List<Image>)[0].path)
    }
}
