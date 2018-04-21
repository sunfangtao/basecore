package com.wxt.basecode2

import android.content.Intent
import android.os.Bundle
import com.wxt.library.base.activity.BaseWelcomeActivity
import com.wxt.library.listener.GuideListener

class WelcomeActivity : BaseWelcomeActivity(), GuideListener {

    override fun isNeedLogin(): Boolean {
        return false
    }

    override fun setIndexActivity(): Class<*> = MainActivity::class.java

    override fun setLoginActivity(): Class<*> = MainActivity::class.java

    override fun setGuideActivity(): Class<*> = GuideActivity::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_welcome)
        super.onCreate(savedInstanceState)

//        val intent = Intent(this, AlbumSelectActivity::class.java).putExtra(SelectCityActivity.LOCATION_CITY, "青岛")
//                .putExtra(Constants.INTENT_EXTRA_LIMIT, 2).putExtra(AlbumSelectActivity.INTENT_EXTRA_COLUMN, 4)
//        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        Util.print(data?.getSerializableExtra(SelectCityActivity.SELECT_KEY) as City)
//        Util.print((data?.getSerializableExtra(AlbumSelectActivity.INTENT_EXTRA_IMAGES) as List<Image>)[0].path)
    }
}
