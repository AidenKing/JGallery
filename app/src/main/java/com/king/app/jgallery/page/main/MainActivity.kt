package com.king.app.jgallery.page.main

import android.Manifest
import androidx.lifecycle.Observer
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseActivity
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.databinding.ActivityMainBinding
import com.king.app.jgallery.utils.AppUtil
import com.tbruyelle.rxpermissions2.RxPermissions

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    var ftImage = ImageFragment()

    override fun getContentView(): Int =
        R.layout.activity_main

    override fun createViewModel(): MainViewModel = generateViewModel(MainViewModel::class.java)

    override fun initView() {

    }

    override fun initData() {
        if (AppUtil.isAndroidP()) {
            AppUtil.closeAndroidPDialog()
        }

        RxPermissions(this)
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe({ isGrant ->
                initCreate()
            }, { throwable ->
                throwable.printStackTrace()
                finish()
            })
    }

    private fun initCreate() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ftImage, "ImageFragment")
            .commit()

        mModel.allImages.observe(this, Observer { ftImage.showItems(it) })
    }

}