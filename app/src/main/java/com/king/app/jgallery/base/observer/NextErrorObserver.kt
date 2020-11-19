package com.king.app.plate.base.observer

import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/1/22 17:10
 */
abstract class NextErrorObserver<T>: Observer<T> {

    private var composite: CompositeDisposable

    constructor(composite: CompositeDisposable) {
        this.composite = composite
    }

    override fun onComplete() {

    }

    override fun onSubscribe(d: Disposable?) {
        composite.add(d)
    }
}