package com.king.app.jgallery.page.main

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.king.app.jactionbar.JActionbar
import com.king.app.jgallery.base.BaseFragment
import com.king.app.jgallery.base.BaseViewModel

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
}