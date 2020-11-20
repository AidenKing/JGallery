package com.king.app.jgallery.page.selector

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseActivity
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.ActivityAlbumSelectorBinding
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.page.main.ImageFolderAdapter
import com.king.app.jgallery.utils.ScreenUtils

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/20 16:44
 */
class AlbumSelectorActivity:BaseActivity<ActivityAlbumSelectorBinding, AlbumSelectorViewModel>() {

    companion object {
        val EXTRA_SOURCE = "source"
    }

    var adapter = ImageFolderAdapter()

    override fun getContentView(): Int = R.layout.activity_album_selector

    override fun createViewModel(): AlbumSelectorViewModel = generateViewModel(AlbumSelectorViewModel::class.java)

    override fun initView() {
        mBinding.actionbar.setOnBackListener { onBackPressed() }

        mBinding.rvList.layoutManager = GridLayoutManager(this, 4)
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
        adapter.setOnItemClickListener(object : BaseBindingAdapter.OnItemClickListener<FolderItem> {
            override fun onClickItem(view: View, position: Int, data: FolderItem) {
                mModel.executeMoveTo(getSource(), data)
            }
        })
        mBinding.rvList.adapter = adapter
    }

    override fun initData() {
        mModel.folderList.observe(this, Observer { showFolders(it) })

        mModel.loadAlbum()
    }

    private fun getSource(): Array<String> {
        return intent.getStringArrayExtra(EXTRA_SOURCE)
    }

    private fun showFolders(it: List<FolderItem>?) {
        adapter.list = it
        adapter.notifyDataSetChanged()
    }
}