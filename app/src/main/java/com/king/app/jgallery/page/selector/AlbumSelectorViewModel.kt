package com.king.app.jgallery.page.selector

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.AlbumModel
import com.king.app.jgallery.model.bean.AlbumData
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.plate.base.observer.NextErrorObserver
import java.io.File


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/20 16:43
 */
class AlbumSelectorViewModel(application: Application):BaseViewModel(application) {

    var folderList = MutableLiveData<List<FolderItem>>()
    var albumModel = AlbumModel()
    var newAlbumCreated = MutableLiveData<String>()


    var albumData = AlbumData(mutableListOf(), mutableListOf())

    fun loadAlbum() {
        albumModel.getAllResource(getApplication())
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<AlbumData>(getComposite()) {
                override fun onNext(t: AlbumData) {
                    albumData = t
                    sortAlbum(SettingProperty.getAlbumSortType())
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message
                }
            })
    }

    fun sortAlbum(sortType: Int) {
        when(sortType) {
            Constants.SORT_TYPE_NAME -> albumData.folders.sortBy { it.name.toLowerCase() }
            Constants.SORT_TYPE_DATE -> albumData.folders.sortByDescending { File(it.path).lastModified() }
        }
        folderList.value = albumData.folders
    }

    /**
     * 默认创建在/Pictures目录下
     */
    fun createAlbum(name: String) {
        var path = "${Constants.STORAGE_ROOT}/Pictures/$name"
        var file = File(path)
        if (file.exists()) {
            messageObserver.value = "目标相册已存在，请重新命名"
            return
        }
        file.mkdirs()
        newAlbumCreated.value = path
    }

}