package com.king.app.jgallery.page.main

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.widget.Switch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.R
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.FragmentImageBinding
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.utils.DebugLog
import com.king.app.jgallery.utils.ScreenUtils

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 10:04
 */
class ImageFragment: AbsChildFragment<FragmentImageBinding, EmptyViewModel>() {

    var adapter: ImageItemAdapter = ImageItemAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentImageBinding = FragmentImageBinding.inflate(inflater)

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView(view: View) {
        super.initView(view)

        toggleMenu()

        mBinding.rvList.layoutManager = GridLayoutManager(context, 4)
        mBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                var position = parent.getChildLayoutPosition(view)
                outRect.left = if (position % 4 == 0) 0 else ScreenUtils.dp2px(1f)
                outRect.top = if (position / 4 == 0) 0 else ScreenUtils.dp2px(1f)
            }
        })
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FileItem> {
            override fun onClickItem(view: View, position: Int, data: FileItem) {
                getMainViewModel().openImageBySystem.value = data.url
            }
        })
        adapter.setOnItemLongClickListener(object : BaseBindingAdapter.OnItemLongClickListener<FileItem> {
            override fun onLongClickItem(view: View, position: Int, data: FileItem) {
                adapter.toggleSelect()
                toggleMenu()
            }
        })
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        refreshPage()
    }

    fun refreshPage() {
        DebugLog.e()
        getMainViewModel().loadAll();
    }

    fun cancelSelection() {
        adapter.cancelSelect()
    }

    fun showItems(it: List<FileItem>?) {
        adapter.list = it
        adapter.notifyDataSetChanged()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            toggleMenu()
        }
    }

    private fun toggleMenu() {
        actionbar.updateMenuItemVisible(R.id.menu_sort, false)
        if (adapter.isSelectMode) {
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
                R.id.menu_move -> getMainViewModel().moveFiles(adapter.getSelectedItems())
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (adapter.isSelectMode) {
            adapter.toggleSelect()
            toggleMenu()
            return true
        }
        return false
    }
}