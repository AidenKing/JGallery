package com.king.app.jgallery.page.selector

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.king.app.jgallery.JGApplication
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.AlbumModel
import com.king.app.jgallery.model.bean.AlbumData
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.utils.FileUtil
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

    var albumData = AlbumData(mutableListOf(), mutableListOf())

    fun loadAlbum() {
        albumModel.getAllResource(getApplication())
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<AlbumData>(getComposite()) {
                override fun onNext(t: AlbumData) {
                    albumData = t
                    folderList.value = t.folders
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message
                }
            })
    }

    fun executeMoveTo(source: Array<String>, data: FolderItem) {
        var targetList = mutableListOf<String>()
        for (path in source) {
            var time = File(path).lastModified()
            var target = FileUtil.moveFile(path, data.path)
            // 移动完成后恢复lastModify（保持其日期排序位置）
            File(target).setLastModified(time)
            targetList.add(target)
        }
        // 通知系统资源库扫描
        albumModel.notifyScanFiles(getApplication<JGApplication>(), targetList)
        messageObserver.value = "移动成功"
    }
}