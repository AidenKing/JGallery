package com.king.app.jgallery.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.king.app.jgallery.R

/**
 * 描述:DialogFragment基类
 *
 * <br></br>创建时间: 2020/1/21
 */
abstract class BindingDialogFragment<T : ViewDataBinding> : DialogFragment() {

    protected lateinit var mBinding: T

    private var windowParams: WindowManager.LayoutParams? = null

    /**
     * 是否可以点击空白处取消
     *
     * @return
     */
    val cancelable: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = cancelable
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogFragment)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        windowParams = dialog!!.window!!.attributes
        mBinding = getBinding(inflater)
        val view = mBinding.root
        initView(view)
        return mBinding.root
    }

    protected abstract fun getBinding(inflater: LayoutInflater): T

    protected abstract fun initView(view: View)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    fun setWidth(width: Int) {
        windowParams!!.width = width
        dialog!!.window!!.attributes = windowParams
    }

    fun setHeight(height: Int) {
        windowParams!!.height = height
        dialog!!.window!!.attributes = windowParams
    }

    /**
     * 设置dialog的偏移位置
     *
     * @param x 负数向左，正数向右
     * @param y 负数向上，正数向下
     */
    fun setPositionOffset(x: Int, y: Int) {

        windowParams!!.x = x
        windowParams!!.y = y
        dialog!!.window!!.attributes = windowParams
    }

    /**
     * album_selector dialog
     *
     * @param x
     * @param y
     */
    protected fun move(x: Int, y: Int) {

        windowParams!!.x += x
        windowParams!!.y += y
        dialog!!.window!!.attributes = windowParams//must have
    }

}
