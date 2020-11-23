package com.king.app.jgallery.page.file

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.jgallery.R
import com.king.app.jgallery.base.adapter.HeadChildBindingAdapter
import com.king.app.jgallery.databinding.AdapterFileFolderBinding
import com.king.app.jgallery.databinding.AdapterFileItemBinding
import com.king.app.jgallery.model.GlideApp
import com.king.app.jgallery.model.bean.FileAdapterFolder
import com.king.app.jgallery.model.bean.FileAdapterItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:36
 */
class FolderAdapter:
    HeadChildBindingAdapter<AdapterFileFolderBinding, AdapterFileItemBinding, FileAdapterFolder, FileAdapterItem>() {

    var isSelectMode = false
    var isDeleteMode = false

    var onDeleteFolderListener: OnDeleteFolderListener? = null

    override val itemClass: Class<*> = FileAdapterItem::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterFileFolderBinding = AdapterFileFolderBinding.inflate(from, parent, false)

    override fun onCreateItemBind(from: LayoutInflater, parent: ViewGroup): AdapterFileItemBinding = AdapterFileItemBinding.inflate(from, parent, false)

    override fun onBindHead(
        binding: AdapterFileFolderBinding?,
        position: Int,
        head: FileAdapterFolder
    ) {
        binding?.let {
            it.bean = head
            it.cbCheck.visibility = if (isSelectMode) View.VISIBLE else View.GONE
            it.ivDelete.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
            it.ivDelete.setOnClickListener { onDeleteFolderListener?.onDeleteFolder(position, head) }
            it.divider.visibility = if (position == 0) View.GONE else View.VISIBLE
        }
    }

    override fun onBindItem(
        binding: AdapterFileItemBinding?,
        position: Int,
        item: FileAdapterItem
    ) {
        binding?.let {
            it.bean = item
            if (item.isImage) {
                it.ivItem.setPadding(0, 0, 0, 0)
                GlideApp.with(it.ivItem.context)
                    .load(item.file.path)
                    .into(it.ivItem)
            }
            else {
                var padding = it.ivItem.resources.getDimensionPixelSize(R.dimen.file_adapter_icon_padding)
                it.ivItem.setPadding(padding, padding, padding, padding)
                it.ivItem.setImageResource(item.iconRes)
            }
            it.cbCheck.visibility = if (isSelectMode) View.VISIBLE else View.GONE
        }
    }

    override fun onClickHead(v: View, position: Int, bean: FileAdapterFolder) {
        if (isSelectMode) {
            var isCheck = bean.isCheck
            bean.isCheck = !isCheck
            notifyItemChanged(position)
        }
        else {
            super.onClickHead(v, position, bean)
        }
    }

    override fun onClickItem(v: View, position: Int, bean: FileAdapterItem) {
        if (isSelectMode) {
            var isCheck = bean.isCheck
            bean.isCheck = !isCheck
            notifyItemChanged(position)
        }
        else {
            super.onClickItem(v, position, bean)
        }
    }

    fun cancelSelect() {
        if (isSelectMode) {
            toggleSelect()
        }
    }

    fun toggleSelect() {
        isSelectMode = !isSelectMode
        // 从选中到取消选中，清除已选位置
        if (!isSelectMode) {
            list?.let {
                for (item in it) {
                    if (item is FileAdapterFolder) {
                        item.isCheck = false
                    }
                    else if (item is FileAdapterItem) {
                        item.isCheck = false
                    }
                }
            }
        }
        notifyDataSetChanged()
    }

    fun toggleDelete() {
        isDeleteMode = !isDeleteMode
        notifyDataSetChanged()
    }

    interface OnDeleteFolderListener {
        fun onDeleteFolder(position: Int, folder: FileAdapterFolder)
    }
}