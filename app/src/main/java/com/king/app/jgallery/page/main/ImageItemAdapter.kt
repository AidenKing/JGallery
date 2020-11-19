package com.king.app.jgallery.page.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.AdapterImageItemBinding

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
}