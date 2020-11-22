package com.king.app.jgallery.page.main

import android.graphics.Rect
import android.graphics.drawable.StateListDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.R
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.base.adapter.HeadChildBindingAdapter
import com.king.app.jgallery.databinding.FragmentImageBinding
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.utils.DebugLog
import com.king.app.jgallery.utils.ScreenUtils
import com.king.app.jgallery.view.widget.extend.FixedFastScroller


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 10:04
 */
class ImageFragment: AbsChildFragment<FragmentImageBinding, EmptyViewModel>() {

    val SPAN_COUNT = 4
    var adapter: RecentAdapter = RecentAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentImageBinding = FragmentImageBinding.inflate(inflater)

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initView(view: View) {
        super.initView(view)

        toggleMenu()

        var manager = GridLayoutManager(context, SPAN_COUNT)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getSpanSize(position, SPAN_COUNT)
            }
        }
        mBinding.rvList.layoutManager = manager
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
        addFastScroller()

//        mBinding.rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    Glide.with(context!!).resumeRequests() //恢复Glide加载图片
//                } else {
//                    Glide.with(context!!).pauseRequests() //禁止Glide加载图片
//                }
//            }
//        })

        adapter.onItemClickListener = object : HeadChildBindingAdapter.OnItemClickListener<FileItem> {
            override fun onClickItem(view: View, position: Int, data: FileItem) {
                getMainViewModel().openImageBySystem.value = data.url
            }
        }
        adapter.onItemLongClickListener = object : HeadChildBindingAdapter.OnItemLongClickListener<FileItem> {
            override fun onLongClickItem(view: View, position: Int, data: FileItem) {
                adapter.toggleSelect()
                toggleMenu()
            }
        }
        mBinding.rvList.adapter = adapter
    }

    /**
     * 传统scrollbar在部分机型上不能拖动（如一加8T），只能用fastScroll
     * 但是fastScroll在数据很多的情况下，高度太低（是根据滑动计算的），为了实现固定高度：
     * 重写FastScroller替换fastScrollXXX属性
     * 实现方法copy from RecyclerView.initFastScroller（该方法无法通过继承RecyclerView覆盖，只能从外部添加）
     * 从源码里只要修改高度就可以达到固定scrollbar的效果
     */
    private fun addFastScroller() {
        /**
        app:fastScrollEnabled="true"
        app:fastScrollHorizontalThumbDrawable="@drawable/scroller_drawable"
        app:fastScrollHorizontalTrackDrawable="@drawable/scroller_bg_drawable"
        app:fastScrollVerticalThumbDrawable="@drawable/scroller_drawable"
        app:fastScrollVerticalTrackDrawable="@drawable/scroller_bg_drawable"
         */
        FixedFastScroller(
            mBinding.rvList,
            resources.getDrawable(R.drawable.scroller_drawable) as StateListDrawable,
            resources.getDrawable(R.drawable.scroller_bg_drawable) as StateListDrawable,
            resources.getDrawable(R.drawable.scroller_drawable) as StateListDrawable,
            resources.getDrawable(R.drawable.scroller_bg_drawable) as StateListDrawable,
            resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
            resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
            resources.getDimensionPixelOffset(R.dimen.fastscroll_margin)
        )
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

    fun showItems(it: MutableList<Any>?) {
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
                R.id.menu_copy -> getMainViewModel().copyFiles(adapter.getSelectedItems())
                R.id.menu_delete -> deleteFiles(adapter.getSelectedItems())
                R.id.menu_setting -> settingPage()
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