package com.king.app.jgallery.page.main

import android.content.DialogInterface
import android.content.Intent
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.king.app.jactionbar.JActionbar
import com.king.app.jgallery.base.BaseFragment
import com.king.app.jgallery.base.BaseViewModel
import com.king.app.jgallery.model.bean.FileItem
import com.king.app.jgallery.page.setting.SettingsActivity
import com.king.app.jgallery.view.dialog.SimpleDialogs

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:52
 */
abstract class AbsChildFragment<T: ViewDataBinding, VM: BaseViewModel>: BaseFragment<T, VM>() {

    lateinit var actionbar: JActionbar
    private var mainViewModel: MainViewModel ? = null

    fun getMainViewModel(): MainViewModel {
        if (mainViewModel == null) {
            mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        }
        return mainViewModel!!
    }

    override fun initView(view: View) {
        if (activity is MainActivity) {
            actionbar = (activity as MainActivity).getJActionBar()
        }
    }

    open fun onBackPressed(): Boolean {
        return false
    }

    fun deleteFiles(items: List<FileItem>) {
        if (items.isEmpty()) {
            showMessageShort("请选择要删除的文件")
            return
        }
        SimpleDialogs().showConfirmCancelDialog(context, "删除文件将不可恢复，确定删除吗？"
            , DialogInterface.OnClickListener { dialog, which -> getMainViewModel().deleteFiles(items) }
            , null)
    }

    fun settingPage() {
        var intent = Intent(activity, SettingsActivity::class.java)
        startActivity(intent)
    }
}