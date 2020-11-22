package com.king.app.jgallery.page.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.jgallery.base.adapter.HeadChildBindingAdapter
import com.king.app.jgallery.databinding.AdapterImageDateBinding
import com.king.app.jgallery.databinding.AdapterImageItemBinding
import com.king.app.jgallery.model.bean.FileItem

/**
 * @description:
 * @author：Jing
 * @date: 2020/11/21 21:37
 */
class RecentAdapter:
    HeadChildBindingAdapter<AdapterImageDateBinding, AdapterImageItemBinding, String, FileItem>() {

    var isSelectMode = false

    override val itemClass: Class<*>
        get() = FileItem::class.java

    override fun onCreateHeadBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterImageDateBinding = AdapterImageDateBinding.inflate(from, parent, false)

    override fun onCreateItemBind(
        from: LayoutInflater,
        parent: ViewGroup
    ): AdapterImageItemBinding = AdapterImageItemBinding.inflate(from, parent, false)

    override fun onBindHead(binding: AdapterImageDateBinding?, position: Int, head: String) {
        binding!!.date = head
    }

    override fun onBindItem(binding: AdapterImageItemBinding?, position: Int, item: FileItem) {
        binding!!.bean = item
        binding.cbCheck.visibility = if (isSelectMode) View.VISIBLE else View.GONE
    }

    override fun onClickItem(v: View, position: Int, bean: FileItem) {
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
            var items = list?.filterIsInstance<FileItem>()
            items?.let {
                for (item in it) {
                    item.isCheck = false
                }
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<FileItem> {
        var result = mutableListOf<FileItem>()
        var items = list?.filterIsInstance<FileItem>()
        items?.let {
            for (item in it) {
                if (item.isCheck) {
                    result.add(item)
                }
            }
        }
        return result
    }

    fun getSpanSize(position: Int, spanCount: Int): Int {
        return if (getItemViewType(position) == TYPE_HEAD) spanCount
        else 1
    }
}