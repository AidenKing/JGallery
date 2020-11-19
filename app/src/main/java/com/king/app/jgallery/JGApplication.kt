package com.king.app.jgallery

import android.app.Application

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
    }
}