package com.king.app.jgallery.page.main

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.R
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.FragmentAlbumBinding
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.utils.ScreenUtils

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 13:34
 */
class AlbumFragment:AbsChildFragment<FragmentAlbumBinding, EmptyViewModel>() {

    var itemAdapter = ImageItemAdapter()
    var folderAdapter = ImageFolderAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentAlbumBinding = FragmentAlbumBinding.inflate(inflater)

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView(view: View) {
        super.initView(view)

        toggleMenu()

        mBinding.rvItems.layoutManager = GridLayoutManager(context, 3)
        mBinding.rvItems.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                var position = parent.getChildLayoutPosition(view)
                outRect.left = if (position % 3 == 0) 0 else ScreenUtils.dp2px(1f)
                outRect.top = if (position / 3 == 0) 0 else ScreenUtils.dp2px(1f)
            }
        })
        itemAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FileItem> {
            override fun onClickItem(view: View, position: Int, data: FileItem) {
                getMainViewModel().openImageBySystem.value = data.url
            }
        })
        itemAdapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<FileItem> {
            override fun onLongClickItem(view: View, position: Int, data: FileItem) {
                itemAdapter.toggleSelect()
                toggleMenu()
            }
        })
        mBinding.rvItems.adapter = itemAdapter

        mBinding.rvFolders.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mBinding.rvFolders.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                var position = parent.getChildLayoutPosition(view)
                outRect.top = if (position > 0) ScreenUtils.dp2px(8f) else 0
            }
        })
    }

    override fun initData() {
        folderAdapter.selection = 0
        folderAdapter.list = getMainViewModel().albumData.folders
        folderAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FolderItem> {
            override fun onClickItem(view: View, position: Int, data: FolderItem) {
                getMainViewModel().selectFolder(data)
            }
        })
        mBinding.rvFolders.adapter = folderAdapter

        if (getMainViewModel().albumData.folders.size > 0) {
            getMainViewModel().selectFolder(getMainViewModel().albumData.folders[0])
        }
    }

    fun cancelSelection() {
        itemAdapter.cancelSelect()
    }

    fun showAlbumItems(it: List<FileItem>?) {
        itemAdapter.list = it
        itemAdapter.notifyDataSetChanged()
    }

    fun showFolders(it: List<FolderItem>?) {
        folderAdapter.list = it
        folderAdapter.notifyDataSetChanged()
        // 重新选中当前folder
        if (folderAdapter.selection != -1) {
            getMainViewModel().selectFolder(getMainViewModel().albumData.folders[folderAdapter.selection])
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            toggleMenu()
        }
    }

    private fun toggleMenu() {
        actionbar.updateMenuItemVisible(R.id.menu_sort, true)
        if (itemAdapter.isSelectMode) {
            actionbar.updateMenuItemVisible(R.id.menu_move, true)
            actionbar.updateMenuItemVisible(R.id.menu_copy, true)
            actionbar.updateMenuItemVisible(R.id.menu_delete, true)
        }
        else {
            actionbar.updateMenuItemVisible(R.id.menu_move, false)
            actionbar.updateMenuItemVisible(R.id.menu_copy, false)
            actionbar.updateMenuItemVisible(R.id.menu_delete, false)
        }
        actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_move -> getMainViewModel().moveFiles(itemAdapter.getSelectedItems())
                R.id.menu_copy -> getMainViewModel().copyFiles(itemAdapter.getSelectedItems())
                R.id.menu_delete -> deleteFiles(itemAdapter.getSelectedItems())
                R.id.menu_setting -> settingPage()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (itemAdapter.isSelectMode) {
            itemAdapter.toggleSelect()
            toggleMenu()
            return true
        }
        return false
    }
}