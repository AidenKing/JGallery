package com.king.app.jgallery.model.setting

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
    }

}