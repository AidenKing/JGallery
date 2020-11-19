package com.king.app.jgallery.page.main

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:22
 */
data class FileItem(
    var type: String,
    var url: String,
    var duration: String? = null
): BaseObservable() {
    @Bindable
    var isCheck = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isCheck)
        }
}
data class FolderItem (
    var name: String,
    var path: String,
    var imgUrl: String? = null,
    var childNum: Int = 0
)