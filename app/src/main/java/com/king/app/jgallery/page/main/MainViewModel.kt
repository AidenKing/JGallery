package com.king.app.jgallery.page.main

import android.app.Application
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.jgallery.JGApplication
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.utils.DebugLog
import com.king.app.plate.base.observer.NextErrorObserver
import io.reactivex.rxjava3.core.Observable
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

    var folderList = mutableListOf<FolderItem>()
    var onFoldersChanged = MutableLiveData<List<FolderItem>>()

    var folderImages = MutableLiveData<List<FileItem>>()

    var currentFolder: FolderItem? = null

    val IMAGE = "image"
    val VIDEO = "video"
    private val DURATION = "duration"
    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        DURATION,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT
    )
    private val IMAGE_PROJECTION = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT,
        MediaStore.Images.Media.MIME_TYPE,
        MediaStore.Images.Media.SIZE
    )

    private val VIDEO_PROJECTION = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.DURATION
    )

    private val SELECTION_ALL_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )
    private val ORDER_BY = MediaStore.Files.FileColumns.DATE_ADDED + " DESC"

    fun loadAll() {
        getAllResource()
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<List<FileItem>>(getComposite()) {
                override fun onNext(t: List<FileItem>?) {
                    allImages.value = t
                    sortAlbum(SettingProperty.getAlbumSortType())
                }

                override fun onError(e: Throwable?) {
                    messageObserver.value = e?.message
                }
            })
    }

    private fun getAllResource(): Observable<List<FileItem>> = Observable.create {

        var list = mutableListOf<FileItem>()

        var videoMin = 0
        val condition: String =
            if (videoMin > 0) "$DURATION <= $videoMin and $DURATION> 0"
            else "$DURATION> 0"
        val selection = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " and " + condition + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + " AND " + MediaStore.MediaColumns.WIDTH + ">0")

        val data = ContentResolverCompat.query(
            getApplication<JGApplication>().contentResolver,
            QUERY_URI, PROJECTION, selection, SELECTION_ALL_ARGS, ORDER_BY,
            CancellationSignal()
        )
        if (data != null) {
            val count: Int = data.count
            if (count > 0) {
                data.moveToFirst()
                do {
                    val path: String = data.getString(
                        data.getColumnIndexOrThrow(
                            IMAGE_PROJECTION.get(1)
                        )
                    )
                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                    if (TextUtils.isEmpty(path) || !File(path).exists()) {
                        continue
                    }
                    val pictureType: String = data.getString(
                        data.getColumnIndexOrThrow(
                            IMAGE_PROJECTION.get(6)
                        )
                    )
                    val eqImg: Boolean = pictureType.startsWith(IMAGE)
                    val duration = if (eqImg) 0 else data.getInt(
                        data.getColumnIndexOrThrow(
                            VIDEO_PROJECTION.get(7)
                        )
                    )
                    val w = if (eqImg) data.getInt(
                        data.getColumnIndexOrThrow(
                            IMAGE_PROJECTION.get(4)
                        )
                    ) else 0
                    val h = if (eqImg) data.getInt(
                        data.getColumnIndexOrThrow(
                            IMAGE_PROJECTION.get(5)
                        )
                    ) else 0
                    var item = FileItem(pictureType, path)
                    if (duration > 0) {
                        item.duration = duration.toString()
                    }
                    var folder = getImageFolder(path)
                    folder.childNum += 1
                    list.add(item)
                } while (data.moveToNext())
            }
        }
        it.onNext(list)
        it.onComplete()
    }

    private fun getImageFolder(path: String): FolderItem {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile
        for (folder in folderList) {
            if (folder.name == folderFile.name) {
                return folder
            }
        }
        var folder = FolderItem(folderFile.name, folderFile.path)
        // 第一张作为封面
        folder.imgUrl = path
        folderList.add(folder)
        return folder
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
                    if (type == VIDEO) {
                        // TODO 查询视频时长
                    }
                    var item = FileItem(type!!, f.path)
                    list.add(item)
                }
            }
        }
        folderImages.value = list
    }

    private fun getFileType(name: String): String? {
        var extra = name.substring(name.lastIndexOf(".") + 1)
        return when(extra) {
            "png", "jpg", "jpeg", "gif", "bmp", "webp" -> IMAGE
            "mp4", "avi", "mkv", "wmv", "rmvb", "mov", "mpeg", "3gp", "rm", "flv" -> VIDEO
            else -> null
        }
    }

    fun sortAlbum(sortType: Int) {
        when(sortType) {
            Constants.SORT_TYPE_NAME -> folderList.sortBy { it.name.toLowerCase() }
            Constants.SORT_TYPE_DATE -> folderList.sortByDescending { File(it.path).lastModified() }
        }
        onFoldersChanged.value = folderList
    }

}