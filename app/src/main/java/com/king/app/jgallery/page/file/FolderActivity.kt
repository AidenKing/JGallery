package com.king.app.jgallery.page.file

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

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:55
 */
class FolderActivity: BaseActivity<ActivityFolderBinding, FolderViewModel>() {

    companion object {
        var SELECT_MODE = "select_mode"
        val START_PATH = "start_path"
    }

    var dirAdapter = DirectoryAdapter()
    var itemAdapter = FolderAdapter()

    override fun getContentView(): Int = R.layout.activity_folder

    override fun createViewModel(): FolderViewModel = generateViewModel(FolderViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { finish() }
        mBinding.actionbar.registerPopupMenu(R.id.menu_sort)
        mBinding.actionbar.setPopupMenuProvider { iconMenuId, anchorView ->
            when(iconMenuId) {
                R.id.menu_sort -> getSortPopup(anchorView!!)
                else -> null
            }
        }
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_shortcut -> {
                    mModel.addToShortcut()
                    itemAdapter.cancelSelect()
                }
            }
        }
        toggleMenu()

        mBinding.rvDir.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mBinding.rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        dirAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FileAdapterFolder> {
            override fun onClickItem(view: View, position: Int, data: FileAdapterFolder) {
                mModel.loadDirectory(data.file.path)
            }
        })
        mBinding.rvDir.adapter = dirAdapter

        itemAdapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<FileAdapterFolder> {
            override fun onClickHead(view: View, position: Int, data: FileAdapterFolder) {
                mModel.loadDirectory(data.file.path)
            }
        }
        itemAdapter.onHeadLongClickListener = object : HeadChildBindingAdapter.OnHeadLongClickListener<FileAdapterFolder> {
            override fun onLongClickHead(view: View, position: Int, data: FileAdapterFolder) {
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
                itemAdapter.toggleSelect()
                toggleMenu()
            }
        }
        mBinding.rvList.adapter = itemAdapter
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

    private fun getStartPath(): String {
        var path = intent.getStringExtra(START_PATH)
        return path?:Constants.STORAGE_ROOT
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