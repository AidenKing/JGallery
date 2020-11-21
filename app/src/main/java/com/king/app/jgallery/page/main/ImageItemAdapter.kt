package com.king.app.jgallery.page.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.AdapterImageItemBinding
import com.king.app.jgallery.model.bean.FileItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:48
 */
class ImageItemAdapter: BaseBindingAdapter<AdapterImageItemBinding, FileItem>() {

    var isSelectMode = false

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterImageItemBinding = AdapterImageItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterImageItemBinding, position: Int, bean: FileItem) {
        binding.bean = bean
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
            list?.let {
                for (item in it) {
                    item.isCheck = false
                }
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<FileItem> {
        var result = mutableListOf<FileItem>()
        list?.let {
            for (item in it) {
                if (item.isCheck) {
                    result.add(item)
                }
            }
        }
        return result
    }
}