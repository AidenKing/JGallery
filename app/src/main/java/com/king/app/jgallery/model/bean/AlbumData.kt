package com.king.app.jgallery.model.bean

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.king.app.jgallery.BR

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/20 16:51
 */
data class AlbumData (
    var folders: MutableList<FolderItem>,
    var items: MutableList<FileItem>
)

data class FileItem(
    var type: String,
    var url: String,
    var duration: String? = null,
    var lastModify: Int = 0// 从数据库里取出来的是unix时间戳，单位是秒；java date getTime单位是毫秒
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