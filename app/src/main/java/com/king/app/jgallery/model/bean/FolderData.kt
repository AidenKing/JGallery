package com.king.app.jgallery.model.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.jgallery.BR
import java.io.File

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:17
 */
data class FileAdapterItem (
    var file: File,
    var date: String = "",
    var size: String = "",
    var iconRes: Int = -1,
    var isImage: Boolean = false
): BaseObservable() {
    @Bindable
    var isCheck = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isCheck)
        }
}
data class FileAdapterFolder (
    var file: File,
    var extraName: String = file.name
): BaseObservable() {
    @Bindable
    var isCheck = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.isCheck)
        }
}