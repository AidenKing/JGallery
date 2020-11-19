package com.king.app.jgallery.page.main

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.FragmentAlbumBinding
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
        folderAdapter.list = getMainViewModel().folderList
        folderAdapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FolderItem> {
            override fun onClickItem(view: View, position: Int, data: FolderItem) {
                getMainViewModel().selectFolder(data)
            }
        })
        mBinding.rvFolders.adapter = folderAdapter

        if (getMainViewModel().folderList.size > 0) {
            getMainViewModel().selectFolder(getMainViewModel().folderList[0])
        }
    }

    fun showAlbumItems(it: List<FileItem>?) {
        itemAdapter.list = it
        itemAdapter.notifyDataSetChanged()
    }

    fun showFolders(it: List<FolderItem>?) {
        folderAdapter.list = it
        folderAdapter.notifyDataSetChanged()
    }
}