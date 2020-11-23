package com.king.app.jgallery.model.setting

import com.google.gson.Gson
import com.king.app.jgallery.model.bean.ShortCutBean
import java.lang.Exception

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 16:07
 */
class SettingProperty: BaseProperty() {

    companion object {

        fun isEnableFingerPrint(): Boolean = getBoolean("pref_safety_fingerprint")

        fun setAlbumSortType(type: Int) {
            setInt("album_sort_type", type)
        }

        fun getAlbumSortType(): Int {
            return getInt("album_sort_type")
        }

        fun setShortcut(bean: ShortCutBean) {
            var json = Gson().toJson(bean)
            setString("shortcut_bean", json)
        }

        fun getShortcut(): ShortCutBean {
            return try {
                var json = getString("shortcut_bean")
                Gson().fromJson(json, ShortCutBean::class.java)
            } catch (e: Exception) {
                ShortCutBean(mutableListOf())
            }
        }
    }

}