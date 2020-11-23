package com.king.app.jgallery.model

import android.content.Context
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import com.king.app.jgallery.JGApplication
import com.king.app.jgallery.model.bean.AlbumData
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.utils.FormatUtil
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/20 16:49
 */
class AlbumModel {

    companion object {
        val IMAGE = "image"
        val VIDEO = "video"

        fun getFileType(name: String): String? {
            var extra = name.substring(name.lastIndexOf(".") + 1)
            return when(extra) {
                "png", "jpg", "jpeg", "gif", "bmp", "webp" -> IMAGE
                "mp4", "avi", "mkv", "wmv", "rmvb", "mov", "mpeg", "3gp", "rm", "flv" -> VIDEO
                else -> null
            }
        }

        fun isImage(name: String): Boolean {
            return getFileType(name.toLowerCase()) == IMAGE
        }

        fun isVideo(name: String): Boolean {
            return getFileType(name.toLowerCase()) == VIDEO
        }
    }

    private val QUERY_URI = MediaStore.Files.getContentUri("external")
    private val PROJECTION = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DURATION,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.WIDTH,
        MediaStore.MediaColumns.HEIGHT
    )
//    private val IMAGE_PROJECTION = arrayOf(
//        MediaStore.Images.Media._ID,
//        MediaStore.Images.Media.DATA,
//        MediaStore.Images.Media.DISPLAY_NAME,
//        MediaStore.Images.Media.DATE_ADDED,
//        MediaStore.Images.Media.WIDTH,
//        MediaStore.Images.Media.HEIGHT,
//        MediaStore.Images.Media.MIME_TYPE,
//        MediaStore.Images.Media.SIZE
//    )
//
//    private val VIDEO_PROJECTION = arrayOf(
//        MediaStore.Video.Media._ID,
//        MediaStore.Video.Media.DATA,
//        MediaStore.Video.Media.DISPLAY_NAME,
//        MediaStore.Video.Media.DATE_ADDED,
//        MediaStore.Video.Media.WIDTH,
//        MediaStore.Video.Media.HEIGHT,
//        MediaStore.Video.Media.MIME_TYPE,
//        MediaStore.Video.Media.DURATION
//    )

    private val SELECTION_ALL_ARGS = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
    )

    /**
     * 按修改时间排序（移动的文件做了恢复modify时间的处理，按照modify date排序）
     */
    private val ORDER_BY = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"

    fun getAllResource(context: Context): Observable<AlbumData> = Observable.create {

        var list = mutableListOf<FileItem>()
        var folders = mutableListOf<FolderItem>()
        var albumData = AlbumData(folders, list)

        var videoMin = 0
        val condition: String =
            if (videoMin > 0) "${MediaStore.MediaColumns.DURATION} <= $videoMin and ${MediaStore.MediaColumns.DURATION}> 0"
            else "${MediaStore.MediaColumns.DURATION}> 0"
        val selection = ("(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" +
                " and " + condition + ")"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                + " AND " + MediaStore.MediaColumns.WIDTH + ">0")

        val data = ContentResolverCompat.query(
            context.contentResolver,
            QUERY_URI, PROJECTION, selection, SELECTION_ALL_ARGS, ORDER_BY,
            CancellationSignal()
        )
        if (data != null) {
            val count: Int = data.count
            if (count > 0) {
                data.moveToFirst()
                do {
                    val path = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    // 如原图路径不存在或者路径存在但文件不存在,就结束当前循环
                    if (TextUtils.isEmpty(path) || !File(path).exists()) {
                        continue
                    }
                    val pictureType = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                    var item = FileItem(
                        pictureType,
                        path
                    )
                    item.lastModify = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))
                    val eqImg = pictureType.startsWith(IMAGE)
                    if (eqImg) {
                        val width = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH))
                        val height = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT))
                    }
                    else {
                        val duration = data.getInt(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION))
                        if (duration > 0) {
                            item.duration = FormatUtil.formatTime(duration.toLong())
                        }
                    }
                    var folder = getImageFolder(path, albumData)
                    folder.childNum += 1
                    list.add(item)
                } while (data.moveToNext())
            }
        }
        it.onNext(albumData)
        it.onComplete()
    }

    private fun getImageFolder(path: String, data: AlbumData): FolderItem {
        val imageFile = File(path)
        val folderFile = imageFile.parentFile
        for (folder in data.folders) {
            if (folder.name == folderFile.name) {
                return folder
            }
        }
        var folder = FolderItem(
            folderFile.name,
            folderFile.path
        )
        // 第一张作为封面
        folder.imgUrl = path
        data.folders.add(folder)
        return folder
    }

    /**
     * 移动、复制图片后要通知系统扫描资源，否则ContentResolver不能及时读不到
     */
    fun notifyScanFile(context: Context, target: String, onCompleteListener: MediaScanner.OnCompleteListener) {
        MediaScanner(context, onCompleteListener).scanFiles(listOf(target))
    }

    /**
     * 移动、复制图片后要通知系统扫描资源，否则ContentResolver不能及时读不到
     */
    fun notifyScanFiles(context: Context, targets: List<String>, onCompleteListener: MediaScanner.OnCompleteListener) {
        MediaScanner(context, onCompleteListener).scanFiles(targets)
    }

}