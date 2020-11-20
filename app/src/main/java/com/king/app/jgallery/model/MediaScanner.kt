package com.king.app.jgallery.model

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.webkit.MimeTypeMap
import com.king.app.jgallery.utils.DebugLog

/**
 * @description:
 * @author：Jing
 * @date: 2020/11/20 21:48
 */
class MediaScanner: MediaScannerConnection.MediaScannerConnectionClient {
    /**
     * 扫描对象
     */
    var mediaScanConn: MediaScannerConnection

    constructor(context: Context) {
        mediaScanConn = MediaScannerConnection(context, this)
    }

    /**文件路径集合 */
    private var filePaths = listOf<String>()

    /**文件MimeType集合 */
    private var mimeTypes = mutableListOf<String>()

    /**
     * 扫描文件
     * @author YOLANDA
     * @param filePaths
     */
    fun scanFiles(filePaths: List<String>) {
        this.filePaths = filePaths
        for (path in filePaths) {
            var extension = path.substring(path.lastIndexOf(".") + 1)
            var type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            mimeTypes.add(type!!)
        }
        mediaScanConn.connect() //连接扫描服务
    }

    /**
     * @author YOLANDA
     */
    override fun onMediaScannerConnected() {
        DebugLog.e()
        for (i in filePaths.indices) {
            mediaScanConn.scanFile(filePaths[i], mimeTypes[i]) //服务回调执行扫描
        }
    }

    private var scanTimes = 0

    /**
     * 扫描一个文件完成
     * @author YOLANDA
     * @param path
     * @param uri
     */
    override fun onScanCompleted(path: String?, uri: Uri?) {
        DebugLog.e(uri?.toString())
        scanTimes++
        if (scanTimes == filePaths.size) { //如果扫描完了全部文件
            mediaScanConn.disconnect() //断开扫描服务
            scanTimes = 0 //复位计数
        }
    }
}