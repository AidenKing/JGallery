package com.king.app.jgallery

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.VmPolicy


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/18 17:34
 */
class JGApplication: Application() {
    companion object {
        lateinit var instance:JGApplication
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // android 7开始，intent发送uri暴露file://的方法存在权限问题，用该方法避免
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
    }
}