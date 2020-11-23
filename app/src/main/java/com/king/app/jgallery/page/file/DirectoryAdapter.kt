package com.king.app.jgallery.page.file

import android.view.LayoutInflater
import android.view.ViewGroup
import com.king.app.jgallery.base.adapter.BaseBindingAdapter
import com.king.app.jgallery.databinding.AdapterFolderDirBinding
import com.king.app.jgallery.model.bean.FileAdapterFolder

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 11:03
 */
class DirectoryAdapter: BaseBindingAdapter<AdapterFolderDirBinding, FileAdapterFolder>() {

    override fun onCreateBind(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): AdapterFolderDirBinding = AdapterFolderDirBinding.inflate(inflater, parent, false)

    override fun onBindItem(
        binding: AdapterFolderDirBinding,
        position: Int,
        bean: FileAdapterFolder
    ) {
        binding?.let {
            it.bean = bean
            // 只可能最后一个选中
            it.tvName.isSelected = (position == itemCount - 1)
        }
    }
}