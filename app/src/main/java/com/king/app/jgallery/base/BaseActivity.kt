package com.king.app.jgallery.base

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.king.app.jgallery.JGApplication
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 15:50
 */
abstract class BaseActivity<T : ViewDataBinding, VM : BaseViewModel> : RootActivity() {

    companion object {
        val KEY_BUNDLE = "bundle"
    }

    lateinit var mBinding: T

    lateinit var mModel: VM

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, getContentView())
        mModel = createViewModel()
        if (mModel != null) {
            mModel!!.loadingObserver.observe(this, Observer { show ->
                if (show) {
                    showProgress("loading...")
                } else {
                    dismissProgress()
                }
            })
            mModel!!.messageObserver.observe(this, Observer { message -> showMessageShort(message) })
        }

        initView()
        initData()
    }

    abstract fun getContentView(): Int

    protected abstract fun createViewModel(): VM

    fun generateViewModel(vm: Class<VM>) = ViewModelProvider(this, ViewModelFactory(JGApplication.instance)).get(vm)

    fun emptyViewModel(): EmptyViewModel = ViewModelProvider(this, ViewModelFactory(JGApplication.instance)).get(EmptyViewModel::class.java)

    protected abstract fun initView()

    protected abstract fun initData()

    override fun onDestroy() {
        mModel?.onDestroy()
        super.onDestroy()
    }

    open fun<AC> startPage(target: Class<AC>) {
        startActivity(Intent().setClass(this, target))
    }

    open fun<AC> startPage(target: Class<AC>, bundle: Bundle) {
        var intent = Intent().setClass(this, target)
        intent.putExtra(KEY_BUNDLE, bundle)
        startActivity(intent)
    }

    open fun<AC> startPageForResult(target: Class<AC>, bundle: Bundle, requestCode: Int) {
        var intent = Intent().setClass(this, target)
        intent.putExtra(KEY_BUNDLE, bundle)
        startActivityForResult(intent, requestCode)
    }

    open fun getIntentBundle(): Bundle? {
        return getIntentBundle(intent)
    }

    open fun getIntentBundle(intent: Intent): Bundle? {
        return intent.getBundleExtra(KEY_BUNDLE)
    }
}
