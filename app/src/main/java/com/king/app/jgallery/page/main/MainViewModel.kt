package com.king.app.jgallery.page.main

import android.app.Application
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.lifecycle.MutableLiveData
import com.king.app.jgallery.JGApplication
import com.king.app.jgallery.base.BaseViewModel
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

    var allImages = MutableLiveData<List<FileItem>>()

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

    private var folderList = mutableListOf<FolderItem>()

    fun loadAll() {
        getAllResource()
            .compose(applySchedulers())
            .subscribe(object : NextErrorObserver<List<FileItem>>(getComposite()) {
                override fun onNext(t: List<FileItem>?) {
                    allImages.value = t
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
                    DebugLog.e("media mime type:", pictureType)
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
        return FolderItem(folderFile.name, folderFile.path)
    }

}