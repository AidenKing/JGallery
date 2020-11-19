package com.king.app.jgallery.view.dialog

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.king.app.jgallery.R
import com.king.app.jgallery.databinding.DialogLoadingBinding

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 13:37
 */
class ProgressDialogFragment : DialogFragment() {

    private lateinit var mBinding: DialogLoadingBinding

    private var message: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isCancelable = true
        setStyle(STYLE_NORMAL, R.style.LoadingDialog)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_loading, container, false)
        mBinding.tvMessage.text = message
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.window!!.setGravity(Gravity.CENTER)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        val ft = manager.beginTransaction()
        if (isAdded) {
            ft.show(this)
        } else {
            ft.add(this, tag)
        }
        ft.commitAllowingStateLoss()
    }

    fun setMessage(message: String) {
        this.message = message
    }
}
