package com.king.app.jgallery.page.file

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.king.app.jactionbar.JActionbar
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.AlbumModel
import com.king.app.jgallery.model.bean.FileAdapterFolder
import com.king.app.jgallery.model.bean.FileAdapterItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.utils.FileUtil
import com.king.app.jgallery.utils.FormatUtil
import com.king.app.plate.base.observer.NextErrorObserver
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:55
 */
class FolderViewModel(application: Application): BaseViewModel(application) {

    var directories = MutableLiveData<List<FileAdapterFolder>>()
    var fileItems = MutableLiveData<MutableList<Any>>()

    var originFileItems = mutableListOf<Any>()

    var movingFilesCount = ObservableField<String>()

    var root = File(Constants.STORAGE_ROOT)
    var currentPath: String? = null
    var isOnlyFolder = false
    var dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm")

    var mSortType: Int = Constants.SORT_TYPE_NAME

    var historyStack = Stack<String>()

    var moveImages = MutableLiveData<List<String>>()
    var copyImages = MutableLiveData<List<String>>()

    var albumModel = AlbumModel()

    fun loadDirectory(path: String) {
        loadDirectory(path, true)
    }

    private fun loadDirectory(path: String, pushStack: Boolean) {
        var folder = File(path)
        if (folder.isDirectory) {
            // 先将当前path入栈
            if (pushStack) {
                currentPath?.let { historyStack.push(it) }
            }

            currentPath = path
            var dirs = mutableListOf<FileAdapterFolder>()
            createDirectories(folder, dirs)
            directories.value = dirs

            loadFileItems(folder)
                .flatMap { sortFiles(it) }
                .compose(applySchedulers())
                .subscribe(object : NextErrorObserver<MutableList<Any>>(getComposite()) {
                    override fun onNext(t: MutableList<Any>) {
                        originFileItems = t
                        fileItems.value = t
                    }

                    override fun onError(e: Throwable?) {
                        e?.printStackTrace()
                        messageObserver.value = e?.message
                    }

                })
        }
    }

    private fun sortFiles(list: MutableList<Any>): ObservableSource<MutableList<Any>> = ObservableSource {
        // 文件夹在前，文件在后；再按sortType
        list.sortWith(Comparator<Any> { left, right -> // 先比较文件/文件夹
            var weight1Left = if(left is FileAdapterFolder ) 0 else 1
            var weight1Right = if(right is FileAdapterFolder ) 0 else 1
            var result = weight1Left - weight1Right
            // 相同再比较sortType
            if (result == 0) {
                when(mSortType) {
                    Constants.SORT_TYPE_NAME -> {
                        if (left is FileAdapterFolder && right is FileAdapterFolder) {
                            result = compareString(left.file.name, right .file.name)
                        }
                        else if (left is FileAdapterItem && right is FileAdapterItem) {
                            result = compareString(left.file.name, right .file.name)
                        }
                    }
                    Constants.SORT_TYPE_DATE -> {
                        if (left is FileAdapterFolder && right is FileAdapterFolder) {
                            result = compareLong(right.file.lastModified(), left.file.lastModified())
                        }
                        else if (left is FileAdapterItem && right is FileAdapterItem) {
                            result = compareLong(right.file.lastModified(), left.file.lastModified())
                        }
                    }
                }
            }
            result
        })
        it.onNext(list)
        it.onComplete()
    }

    private fun compareString(left: String, right: String): Int {
        return left.toLowerCase().compareTo(right.toLowerCase())
    }

    private fun compareLong(left: Long, right: Long): Int {
        var result = left - right;
        return when {
            result < 0 -> -1
            result >0 -> 1
            else -> 0
        }
    }

    private fun loadFileItems(folder: File): Observable<MutableList<Any>> = Observable.create {

        var itemList = mutableListOf<Any>()
        var files = if (isOnlyFolder) {
            folder.listFiles { dir, name -> dir.isDirectory}
        }
        else {
            folder.listFiles()
        }
        for (file in files) {
            if (file.isDirectory) {
                itemList.add(FileAdapterFolder(file))
            }
            else {
                var item = FileAdapterItem(file)
                item.isImage = AlbumModel.isImage(file.name)
                item.date = dateFormat.format(Date(file.lastModified()))
                item.size = FormatUtil.formatSize(file.length())
                if (!item.isImage) {
                    if (AlbumModel.isVideo(file.name)) {
                        item.iconRes = R.drawable.baseline_videocam_blue_800_36dp
                    }
                    else {
                        item.iconRes = R.drawable.baseline_device_unknown_grey_700_36dp
                    }
                }
                itemList.add(item)
            }
        }
        it.onNext(itemList)
        it.onComplete()
    }

    private fun createDirectories(
        folder: File,
        dirs: MutableList<FileAdapterFolder>
    ) {
        if (folder.path == root.path) {
            var item = FileAdapterFolder(folder)
            item.extraName = "内部存储"
            dirs.add(item)
            return
        }
        else {
            createDirectories(folder.parentFile, dirs)
            var item = FileAdapterFolder(folder)
            item.extraName = ">  ${folder.name}"
            dirs.add(item)
        }
    }

    fun sortItemsBy(sortType: Int) {
        mSortType = sortType
        loadDirectory(currentPath!!)
    }

    /**
     * 添加至快速访问只支持文件夹
     */
    fun addToShortcut() {
        fileItems.value?.let {
            var bean = SettingProperty.getShortcut()
            var newPaths = mutableListOf<String>()
            for (item in it) {
                if (item is FileAdapterFolder) {
                    if (item.isCheck && item.file.isDirectory && !isShortcutExist(item.file.path, bean.paths)) {
                        newPaths.add(item.file.path)
                    }
                }
            }
            bean.paths.addAll(newPaths)
            SettingProperty.setShortcut(bean)
            messageObserver.value = "添加成功"
        }
    }

    private fun isShortcutExist(path: String, list: MutableList<String>): Boolean {
        for (text in list) {
            if (path == text) {
                return true
            }
        }
        return false
    }

    fun backHistory(): Boolean {
        return try {
            var top = historyStack.pop()
            // 执行返回，当前目录不加入到历史栈中
            loadDirectory(top, false)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteFiles() {
        fileItems.value?.let {
            for (item in it) {
                if (item is FileAdapterFolder) {
                    if (item.isCheck) {
                        FileUtil.deleteFile(File(item.file.path))
                    }
                }
                else if (item is FileAdapterItem) {
                    if (item.isCheck) {
                        FileUtil.deleteFile(File(item.file.path))
                    }
                }
            }
            messageObserver.value = "删除成功"
            loadDirectory(currentPath!!, false)
        }
    }

    fun createFolder(name: String?): Boolean {
        var path = "$currentPath/$name"
        var file = File(path)
        if (file.exists()) {
            messageObserver.value = "目标文件夹已存在，请重新命名"
            return false
        }
        file.mkdir()
        loadDirectory(currentPath!!, false)
        return true
    }

    fun getSelectedPath(): List<String> {
        var list = mutableListOf<String>()
        fileItems.value?.let {
            for (item in it) {
                if (item is FileAdapterFolder) {
                    if (item.isCheck) {
                        list.add(item.file.path)
                    }
                }
                else if (item is FileAdapterItem) {
                    if (item.isCheck) {
                        list.add(item.file.path)
                    }
                }
            }
        }
        return list
    }

    fun prepareMoveFiles(): Boolean {
        var list = getSelectedPath()
        if (list.isEmpty()) {
            messageObserver.value = "请选择要移动的文件"
            return false
        }
        movingFilesCount.set("${list.size}个项目")
        moveImages.value = list
        return true
    }

    fun prepareCopyFiles(): Boolean {
        var list = getSelectedPath()
        if (list.isEmpty()) {
            messageObserver.value = "请选择要复制的文件"
            return false
        }
        movingFilesCount.set("${list.size}个项目")
        copyImages.value = list
        return true
    }

    fun executeMoveTo() {
        moveImages.value?.let {
            var targetList = mutableListOf<String>()
            for (item in it) {
                var target = FileUtil.moveFile(item, currentPath)
                targetList.add(target)
            }
            // 通知系统资源库扫描
//            albumModel.notifyScanFiles(getApplication<JGApplication>(), targetList, object : MediaScanner.OnCompleteListener {
//                // 要在资源库扫描完毕后再刷新，否则移动后的数据刷新不过来
//                override fun onComplete() {
//                    DebugLog.e()
//                    loadDirectory(currentPath!!, false)
//                }
//            })
            messageObserver.value = "移动成功"
            loadDirectory(currentPath!!, false)
        }
    }

    fun executeCopyTo() {
        copyImages.value?.let {
            var targetList = mutableListOf<String>()
            for (item in it) {
                var target = FileUtil.copyFile(item, currentPath)
                targetList.add(target)
            }
            // 通知系统资源库扫描
//            albumModel.notifyScanFiles(getApplication<JGApplication>(), targetList, object : MediaScanner.OnCompleteListener {
//                // 要在资源库扫描完毕后再刷新，否则复制后的数据刷新不过来
//                override fun onComplete() {
//                    DebugLog.e()
//                    loadDirectory(currentPath!!, false)
//                }
//            })
            messageObserver.value = "复制成功"
            loadDirectory(currentPath!!, false)
        }
    }

    fun getToRenameFolder(): String {
        return getSelectedPath()[0]
    }

    fun prepareMoreMenu(actionbar: JActionbar) {
        var list = getSelectedPath()
        var countFolder = 0
        for (path in list) {
            if (File(path).isDirectory) {
                countFolder ++
            }
        }
        // 只有文件夹支持添加至快速访问
        actionbar.updateMenuItemVisible(R.id.menu_shortcut, countFolder == list.size && countFolder > 0)

        // 只有一个文件夹才支持重命名
        actionbar.updateMenuItemVisible(R.id.menu_rename, countFolder == list.size && list.size == 1)
    }

    fun renameFolder(name: String) {
        var file = File(getToRenameFolder())
        var dest = File("${file.parent}/$name")
        if (dest.exists()) {
            messageObserver.value = "目标文件已存在，请重新命名"
            return
        }
        // 用renameTo有个小问题，目录下的资源会变成新增资源，无法保留原资源的lastModify
        file.renameTo(dest)
        messageObserver.value = "重命名成功"
        loadDirectory(currentPath!!, false)
    }

    fun filterSearch(keyword: String) {
        var result = if (keyword.trim().isEmpty()) {
            originFileItems
        } else {
            originFileItems.filter {
                when (it) {
                    is FileAdapterFolder -> it.file.name.toLowerCase().contains(keyword.toLowerCase())
                    is FileAdapterItem -> it.file.name.toLowerCase().contains(keyword.toLowerCase())
                    else -> false
                }
            }
        }
        fileItems.value = result.toMutableList()
    }

}