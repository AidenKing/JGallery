package com.king.app.jgallery.page.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.jgallery.R
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.AdapterImgFolderItemBinding
import com.king.app.jgallery.model.bean.FolderItem

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:48
 */
class ImageFolderAdapter: BaseBindingAdapter<AdapterImgFolderItemBinding, FolderItem>() {

    var selection = -1

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterImgFolderItemBinding = AdapterImgFolderItemBinding.inflate(inflater, parent, false)

    override fun onBindItem(binding: AdapterImgFolderItemBinding, position: Int, bean: FolderItem) {
        binding.bean = bean
        if (selection == position) {
            binding.root.setBackgroundColor(binding.root.resources.getColor(R.color.folder_focus))
        }
        else {
            binding.root.setBackgroundColor(binding.root.resources.getColor(R.color.white))
        }
    }

    override fun onClickItem(
        v: View,
        position: Int,
        bean: FolderItem
    ) {
        var lastSelection = selection
        selection = position
        if (lastSelection != -1) {
            notifyItemChanged(lastSelection)
        }
        notifyItemChanged(position)
        super.onClickItem(v, position, bean)
    }
}