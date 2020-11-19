package com.king.app.jgallery.base

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 15:02
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var loadingObserver = MutableLiveData<Boolean>()
    var messageObserver = MutableLiveData<String>()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun dispatchCommonError(e: Throwable) {
        messageObserver.value = "Load error: " + e.message
    }

    fun dispatchCommonError(errorTitle: String, e: Throwable) {
        messageObserver.value = errorTitle + ": " + e.message
    }

    fun getComposite() = compositeDisposable

    fun onDestroy() {
        compositeDisposable.clear()
    }

    fun showLoading(show: Boolean){
        loadingObserver.value=show
    }

    val former = ObservableTransformer<Any, Any> { upstream ->
        upstream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun <T> applySchedulers(): ObservableTransformer<T, T> {
        return former as ObservableTransformer<T, T>
    }

    fun getResource(): Resources = getApplication<Application>().resources
}
