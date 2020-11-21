package com.king.app.jgallery.page.selector

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseActivity
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.ActivityAlbumSelectorBinding
import com.king.app.jgallery.model.bean.FolderItem
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.page.main.ImageFolderAdapter
import com.king.app.jgallery.utils.ScreenUtils
import com.king.app.jgallery.view.dialog.SimpleDialogs

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/20 16:44
 */
class AlbumSelectorActivity:BaseActivity<ActivityAlbumSelectorBinding, AlbumSelectorViewModel>() {

    companion object {
        val DATA_FOLDER = "folder_name"
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
                onSelectAlbum(data.path)
            }
        })
        mBinding.rvList.adapter = adapter

        mBinding.actionbar.registerPopupMenu(R.id.menu_sort)
        mBinding.actionbar.setOnMenuItemListener {
            when(it) {
                R.id.menu_add -> SimpleDialogs().openInputDialog(
                    this@AlbumSelectorActivity
                    , "新建相册（位于Pictures目录下）"
                ) { name -> mModel.createAlbum(name) }
            }
        }
        mBinding.actionbar.setPopupMenuProvider { iconMenuId, anchorView ->
            when(iconMenuId) {
                R.id.menu_sort -> getSortPopup(anchorView!!)
                else -> null
            }
        }
    }

    override fun initData() {
        mModel.folderList.observe(this, Observer { showFolders(it) })
        mModel.newAlbumCreated.observe(this, Observer { onSelectAlbum(it) })

        mModel.loadAlbum()
    }

    private fun onSelectAlbum(path: String) {
        var intent = Intent()
        intent.putExtra(DATA_FOLDER, path)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun getSortPopup(anchorView: View): PopupMenu? {
        val menu = PopupMenu(this, anchorView)
        menu.menuInflater.inflate(R.menu.album_sort, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_sort_name -> {
                    mModel.sortAlbum(Constants.SORT_TYPE_NAME)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_NAME)
                }
                R.id.menu_sort_date -> {
                    mModel.sortAlbum(Constants.SORT_TYPE_DATE)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_DATE)
                }
            }
            true
        }
        return menu
    }

    private fun showFolders(it: List<FolderItem>?) {
        adapter.list = it
        adapter.notifyDataSetChanged()
    }
}