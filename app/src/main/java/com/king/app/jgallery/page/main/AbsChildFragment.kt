package com.king.app.jgallery.page.main

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import com.king.app.jgallery.base.BaseFragment
import com.king.app.jgallery.base.BaseViewModel

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:52
 */
abstract class AbsChildFragment<T: ViewDataBinding, VM: BaseViewModel>: BaseFragment<T, VM>() {

    private var mainViewModel: MainViewModel ? = null

    fun getMainViewModel(): MainViewModel {
        if (mainViewModel == null) {
            mainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        }
        return mainViewModel!!
    }
}