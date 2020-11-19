package com.king.app.jgallery.page.main

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.databinding.FragmentImageBinding
import com.king.app.jgallery.utils.ScreenUtils

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 10:04
 */
class ImageFragment: AbsChildFragment<FragmentImageBinding, EmptyViewModel>() {

    var adapter: ImageItemAdapter = ImageItemAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentImageBinding = FragmentImageBinding.inflate(inflater)

    override fun createViewModel(): EmptyViewModel = generateViewModel(EmptyViewModel::class.java)

    override fun initView(view: View) {
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
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        getMainViewModel().loadAll();
    }

    fun showItems(it: List<FileItem>?) {
        adapter.list = it
        adapter.notifyDataSetChanged()
    }
}