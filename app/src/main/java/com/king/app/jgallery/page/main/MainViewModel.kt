package com.king.app.jgallery.page.main

import android.app.Application
import android.media.MediaMetadataRetriever
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.jgallery.JGApplication
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.AlbumModel
import com.king.app.jgallery.model.MediaScanner
import com.king.app.jgallery.model.bean.AlbumData
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.utils.DebugLog
import com.king.app.jgallery.utils.FileUtil
import com.king.app.jgallery.utils.FormatUtil
import com.king.app.plate.base.observer.NextErrorObserver
import java.io.File


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:52
 */
class MainViewModel(application: Application): BaseViewModel(application) {

    var titleText = ObservableField<String>()

    var allImages = MutableLiveData<List<FileItem>>()
    var openImageBySystem = MutableLiveData<String>()

    var albumData = AlbumData(mutableListOf(), mutableListOf())

    var onFoldersChanged = MutableLiveData<List<FolderItem>>()

    var folderImages = MutableLiveData<List<FileItem>>()

    var moveImages = MutableLiveData<Array<FileItem>>()

    var refreshPage = MutableLiveData<Boolean>()

    var currentFolder: FolderItem? = null

    var albumModel = AlbumModel()

    fun loadAll() {
        albumModel.getAllResource(getApplication())
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<AlbumData>(getComposite()) {
                override fun onNext(t: AlbumData) {
                    albumData = t
                    allImages.value = t.items
                    sortAlbum(SettingProperty.getAlbumSortType())
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message
                }
            })
    }

    fun updateTitle(title:String) {
        titleText.set(title)
    }

    fun updateFolderTitle() {
        if (currentFolder != null) {
            var title = "${currentFolder!!.name}\n${currentFolder!!.childNum}张图片"
            updateTitle(title)
        }
    }

    fun selectFolder(folderItem: FolderItem) {
        currentFolder = folderItem
        updateFolderTitle()

        var list = mutableListOf<FileItem>()
        var file = File(folderItem.path)
        if (file.exists()) {
            var files = file.listFiles()
            files.let {
                for (f in it) {
                    var type: String? = getFileType(f.name) ?: continue
                    var item =
                        FileItem(type!!, f.path)
                    if (type == AlbumModel.VIDEO) {
                        var duration = getLocalVideoDuration(f.path)
                        item.duration = FormatUtil.formatTime(duration.toLong())
                    }
                    list.add(item)
                }
                list.sortByDescending { item -> File(item.url).lastModified() }
            }
        }
        folderImages.value = list
    }

    private fun getFileType(name: String): String? {
        var extra = name.substring(name.lastIndexOf(".") + 1)
        return when(extra) {
            "png", "jpg", "jpeg", "gif", "bmp", "webp" -> AlbumModel.IMAGE
            "mp4", "avi", "mkv", "wmv", "rmvb", "mov", "mpeg", "3gp", "rm", "flv" -> AlbumModel.VIDEO
            else -> null
        }
    }

    /**
     * get Local video duration
     *
     * @return 单位是毫秒
     */
    private fun getLocalVideoDuration(videoPath: String): Int {
        val duration: Int
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(videoPath)
            duration =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
            val width =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
        return duration
    }

    fun sortAlbum(sortType: Int) {
        when(sortType) {
            Constants.SORT_TYPE_NAME -> albumData.folders.sortBy { it.name.toLowerCase() }
            Constants.SORT_TYPE_DATE -> albumData.folders.sortByDescending { File(it.path).lastModified() }
        }
        onFoldersChanged.value = albumData.folders
    }

    fun moveFiles(items: List<FileItem>) {
        if (items.isEmpty()) {
            messageObserver.value = "请选择要移动的文件"
            return
        }
        moveImages.value = items.toTypedArray()
    }

    fun executeMoveTo(source: Array<FileItem>, path: String) {
        var targetList = mutableListOf<String>()
        for (item in source) {
            var time = File(item.url).lastModified()
            var target = FileUtil.moveFile(item.url, path)
            // 修改url
            item.url = target
            // 移动完成后恢复lastModify（保持其日期排序位置）
            File(target).setLastModified(time)
            targetList.add(target)
        }
        // 通知系统资源库扫描
        albumModel.notifyScanFiles(getApplication<JGApplication>(), targetList, object : MediaScanner.OnCompleteListener {
            // 要在资源库扫描完毕后再刷新，否则移动后的数据刷新不过来
            override fun onComplete() {
                DebugLog.e()
                refreshPage.postValue(true)
            }
        })
        messageObserver.value = "移动成功"
    }

}