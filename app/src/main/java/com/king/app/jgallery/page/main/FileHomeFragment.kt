package com.king.app.jgallery.page.main

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.king.app.jgallery.base.EmptyViewModel
import com.king.app.jgallery.base.adapter.HeadChildBindingAdapter
import com.king.app.jgallery.databinding.FragmentFileHomeBinding
import com.king.app.jgallery.model.bean.FileAdapterFolder
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.page.file.FolderAdapter

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/23 10:03
 */
class FileHomeFragment: AbsChildFragment<FragmentFileHomeBinding, EmptyViewModel>() {

    var adapter = FolderAdapter()

    override fun getBinding(inflater: LayoutInflater): FragmentFileHomeBinding = FragmentFileHomeBinding.inflate(inflater)

    override fun createViewModel(): EmptyViewModel = emptyViewModel()

    override fun initData() {
        mBinding.rvList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        mBinding.clPhone.setOnClickListener { getMainViewModel().openFolder(Constants.STORAGE_ROOT) }

        adapter.onHeadClickListener = object : HeadChildBindingAdapter.OnHeadClickListener<FileAdapterFolder> {
            override fun onClickHead(view: View, position: Int, data: FileAdapterFolder) {
                getMainViewModel().openFolder(data.file.path)
            }
        }
        adapter.onHeadLongClickListener = object : HeadChildBindingAdapter.OnHeadLongClickListener<FileAdapterFolder> {
            override fun onLongClickHead(view: View, position: Int, data: FileAdapterFolder) {
                adapter.toggleDelete()
            }
        }
        adapter.onDeleteFolderListener = object : FolderAdapter.OnDeleteFolderListener {
            override fun onDeleteFolder(position: Int, folder: FileAdapterFolder) {
                getMainViewModel().removeShortCut(folder)
            }
        }
        mBinding.rvList.adapter = adapter

        getMainViewModel().shortCuts.observe(this, Observer {
            adapter.list = it
            adapter.notifyDataSetChanged()
        })
    }

    override fun onBackPressed(): Boolean {
        return if (adapter.isDeleteMode) {
            adapter.toggleDelete()
            true
        } else {
            false
        }
    }
}