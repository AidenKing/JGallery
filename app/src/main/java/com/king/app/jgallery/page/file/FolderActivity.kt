package com.king.app.jgallery.page.file

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseActivity
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.base.adapter.HeadChildBindingAdapter
import com.king.app.jgallery.databinding.ActivityFolderBinding
import com.king.app.jgallery.model.bean.FileAdapterFolder
import com.king.app.jgallery.model.bean.FileAdapterItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.utils.OpenFileUtil
import com.king.app.jgallery.view.dialog.SimpleDialogs
import java.io.File

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:55
 */
class FolderActivity: BaseActivity<ActivityFolderBinding, FolderViewModel>() {

    companion object {
        var SELECT_MODE = "select_mode"
        val START_PATH = "start_path"
        val DATA_SELECTED_FOLDER = "data_selected_folder"
    }

    var dirAdapter = DirectoryAdapter()
    var itemAdapter = FolderAdapter()

    var isMovingFiles = false
    var isCopyingFiles = false

    override fun getContentView(): Int = R.layout.activity_folder

    override fun createViewModel(): FolderViewModel = generateViewModel(FolderViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel

        initActionBar()

        initCopyBar()

        initItemList()

        // directory bar
        mBinding.rvDir.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        dirAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FileAdapterFolder> {
            override fun onClickItem(view: View, position: Int, data: FileAdapterFolder) {
                mModel.loadDirectory(data.file.path)
            }
        })
        mBinding.rvDir.adapter = dirAdapter

    }

    private fun initItemList() {

        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        itemAdapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<FileAdapterFolder> {
            override fun onClickHead(view: View, position: Int, data: FileAdapterFolder) {
                if (isSelector()) {
                    var intent = Intent()
                    intent.putExtra(DATA_SELECTED_FOLDER, data.file.path)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                else {
                    mModel.loadDirectory(data.file.path)
                }
            }
        }
        itemAdapter.onHeadLongClickListener = object : HeadChildBindingAdapter.OnHeadLongClickListener<FileAdapterFolder> {
            override fun onLongClickHead(view: View, position: Int, data: FileAdapterFolder) {
                // 移动、复制过程中不允许进入选择模式
                if (isMovingFiles || isCopyingFiles) {
                    return
                }
                itemAdapter.toggleSelect()
                toggleMenu()
            }
        }
        itemAdapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<FileAdapterItem> {
            override fun onClickItem(view: View, position: Int, data: FileAdapterItem) {
                var intent = OpenFileUtil.openFile(data.file.path)
                startActivity(intent)
            }
        }
        itemAdapter.onItemLongClickListener = object : HeadChildBindingAdapter.OnItemLongClickListener<FileAdapterItem> {
            override fun onLongClickItem(view: View, position: Int, data: FileAdapterItem) {
                // 移动、复制过程中不允许进入选择模式
                if (isMovingFiles || isCopyingFiles) {
                    return
                }
                itemAdapter.toggleSelect()
                toggleMenu()
            }
        }
        mBinding.rvList.adapter = itemAdapter
    }

    private fun initCopyBar() {
        mBinding.tvCopyCancel.setOnClickListener {
            isMovingFiles = false
            isCopyingFiles = false
            mBinding.clCopy.visibility = View.GONE
        }
        mBinding.tvCopyConfirm.setOnClickListener {
            if (isMovingFiles) {
                mModel.executeMoveTo()
            }
            else if (isCopyingFiles) {
                mModel.executeCopyTo()
            }
            isMovingFiles = false
            isCopyingFiles = false
            mBinding.clCopy.visibility = View.GONE
        }
    }

    private fun initActionBar() {
        mBinding.actionbar.setOnBackListener { finish() }
        mBinding.actionbar.setOnSearchListener { mModel.filterSearch(it) }
        mBinding.actionbar.registerPopupMenu(R.id.menu_sort)
        mBinding.actionbar.setPopupMenuProvider { iconMenuId, anchorView ->
            when(iconMenuId) {
                R.id.menu_sort -> getSortPopup(anchorView!!)
                else -> null
            }
        }
        mBinding.actionbar.setOnPrepareMoreListener { mModel.prepareMoreMenu(mBinding.actionbar) }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_shortcut -> {
                    mModel.addToShortcut()
                    itemAdapter.cancelSelect()
                }
                R.id.menu_delete -> deleteFiles()
                R.id.menu_add -> SimpleDialogs().openInputDialog(
                    this@FolderActivity
                    , "新建目录"
                ) { name -> mModel.createFolder(name) }
                R.id.menu_rename -> SimpleDialogs().openInputDialog(
                    this@FolderActivity
                    , "重命名"
                    , File(mModel.getToRenameFolder()).name
                ) { name ->
                    mModel.renameFolder(name)
                    itemAdapter.cancelSelect()
                }
                R.id.menu_move -> {
                    if (mModel.prepareMoveFiles()) {
                        mBinding.clCopy.visibility = View.VISIBLE
                        mBinding.tvCopyConfirm.text = "移动到此处"
                        isMovingFiles = true
                        itemAdapter.cancelSelect()
                    }
                }
                R.id.menu_copy -> {
                    if (mModel.prepareCopyFiles()) {
                        mBinding.clCopy.visibility = View.VISIBLE
                        mBinding.tvCopyConfirm.text = "复制到此处"
                        isCopyingFiles = true
                        itemAdapter.cancelSelect()
                    }
                }
            }
        }
        toggleMenu()
    }

    override fun initData() {
        mModel.directories.observe(this, Observer {
            dirAdapter.list = it
            dirAdapter.notifyDataSetChanged()
            // 自动滚动到最末
            if (it.isNotEmpty()) {
                mBinding.rvDir.scrollToPosition(it.size - 1)
            }
        })
        mModel.fileItems.observe(this, Observer {
            itemAdapter.list = it
            itemAdapter.notifyDataSetChanged()
        })

        mModel.isOnlyFolder = isSelector()
        mModel.loadDirectory(getStartPath())
    }

    private fun getSortPopup(anchorView: View): PopupMenu? {
        val menu = PopupMenu(this, anchorView)
        menu.menuInflater.inflate(R.menu.album_sort, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_sort_name -> {
                    mModel.sortItemsBy(Constants.SORT_TYPE_NAME)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_NAME)
                }
                R.id.menu_sort_date -> {
                    mModel.sortItemsBy(Constants.SORT_TYPE_DATE)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_DATE)
                }
            }
            true
        }
        return menu
    }

    private fun deleteFiles() {
        SimpleDialogs().showConfirmCancelDialog(this, "删除文件将不可恢复，确定删除吗？"
            , DialogInterface.OnClickListener { dialog, which ->
                mModel.deleteFiles()
                itemAdapter.cancelSelect()
            }
            , null)
    }

    private fun getStartPath(): String {
        var path = intent.getStringExtra(START_PATH)
        return path?:Constants.STORAGE_ROOT
    }

    private fun isSelector(): Boolean {
        return intent.getBooleanExtra(SELECT_MODE, false)
    }

    override fun onBackPressed() {
        when {
            itemAdapter.isSelectMode -> {
                itemAdapter.toggleSelect()
            }
            mModel.backHistory() -> {
                return
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun toggleMenu() {
        if (itemAdapter.isSelectMode) {
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_move, true)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_copy, true)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_delete, true)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_shortcut, true)
        }
        else {
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_move, false)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_copy, false)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_delete, false)
            mBinding.actionbar.updateMenuItemVisible(R.id.menu_shortcut, false)
        }
    }
}