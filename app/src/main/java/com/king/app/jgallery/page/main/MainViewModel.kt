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
import com.king.app.jgallery.model.bean.FileAdapterFolder
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.utils.DebugLog
import com.king.app.jgallery.utils.FileUtil
import com.king.app.jgallery.utils.FormatUtil
import com.king.app.plate.base.observer.NextErrorObserver
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:52
 */
class MainViewModel(application: Application): BaseViewModel(application) {

    var titleText = ObservableField<String>()

    var allImages = MutableLiveData<MutableList<Any>>()
    var openImageBySystem = MutableLiveData<String>()

    var albumData = AlbumData(mutableListOf(), mutableListOf())

    var onFoldersChanged = MutableLiveData<List<FolderItem>>()

    var folderImages = MutableLiveData<List<FileItem>>()

    var moveImages = MutableLiveData<Array<FileItem>>()
    var copyImages = MutableLiveData<Array<FileItem>>()

    var refreshPage = MutableLiveData<Boolean>()

    var openFolder = MutableLiveData<String>()

    var shortCuts = MutableLiveData<MutableList<Any>>()

    var currentFolder: FolderItem? = null

    var albumModel = AlbumModel()

    fun loadAll() {
        albumModel.getAllResource(getApplication())
            .flatMap {
                albumData = it
                toRecentItems(it.items)
            }
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<MutableList<Any>>(getComposite()) {
                override fun onNext(t: MutableList<Any>) {
                    DebugLog.e("${t.size} items loaded")
                    allImages.value = t
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message
                }

                override fun onComplete() {
                    DebugLog.e("onComplete")
                    // 全部完成再对folders进行排序
                    sortAlbum(SettingProperty.getAlbumSortType())
                }
            })
    }

    private fun toRecentItems(items: MutableList<FileItem>): ObservableSource<MutableList<Any>> = ObservableSource {
        var list = mutableListOf<Any>()
        var sdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        var today = sdf.format(Date())
        var day = ""
        for (item in items) {
            var time = item.lastModify.toLong() * 1000
            var itemDay = sdf.format(Date(time))
            if (itemDay != day) {
                day = itemDay
                list.add(if (day == today) "今天" else day)
            }
            list.add(item)
        }
        it.onNext(list)
        it.onComplete()
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
                    var type: String? = AlbumModel.getFileType(f.name) ?: continue
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

    fun copyFiles(items: List<FileItem>) {
        if (items.isEmpty()) {
            messageObserver.value = "请选择要复制的文件"
            return
        }
        copyImages.value = items.toTypedArray()
    }

    fun deleteFiles(items: List<FileItem>) {
        for (item in items) {
            var file = File(item.url)
            if (file.exists()) {
                file.delete()
            }
        }
        refreshPage.postValue(true)
    }

    fun executeMoveTo(source: Array<FileItem>, path: String) {
        var targetList = mutableListOf<String>()
        for (item in source) {
            var target = FileUtil.moveFile(item.url, path)
            // 修改url
            item.url = target
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

    fun executeCopyTo(source: Array<FileItem>, path: String) {
        var targetList = mutableListOf<String>()
        for (item in source) {
            var target = FileUtil.copyFile(item.url, path)
            targetList.add(target)
        }
        // 通知系统资源库扫描
        albumModel.notifyScanFiles(getApplication<JGApplication>(), targetList, object : MediaScanner.OnCompleteListener {
            // 要在资源库扫描完毕后再刷新，否则复制后的数据刷新不过来
            override fun onComplete() {
                DebugLog.e()
                refreshPage.postValue(true)
            }
        })
        messageObserver.value = "复制成功"
    }

    fun openFolder(path: String) {
        openFolder.value = path
    }

    fun getShortCuts() {
        var bean = SettingProperty.getShortcut()
        var invalidPaths = mutableListOf<String>()
        var list = mutableListOf<Any>()
        for (path in bean.paths) {
            var file = File(path)
            if (file.exists() && file.isDirectory) {
                var item = FileAdapterFolder(file)
                list.add(item)
            }
            else {
                invalidPaths.add(path)
            }
        }
        if (invalidPaths.size > 0) {
            for (path in invalidPaths) {
                bean.paths.remove(path)
            }
            SettingProperty.setShortcut(bean)
        }
        shortCuts.value = list
    }

    fun removeShortCut(folder: FileAdapterFolder) {
        var bean = SettingProperty.getShortcut()
        bean.paths.remove(folder.file.path)
        SettingProperty.setShortcut(bean)
        var list = shortCuts.value!!
        list.remove(folder)
        shortCuts.value = list
    }

}