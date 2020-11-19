package com.king.app.jgallery.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.king.app.jgallery.R
import com.king.app.jgallery.view.dialog.ProgressDialogFragment

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/22 13:26
 */
abstract class RootFragment : Fragment() {

    private var progressDialogFragment: ProgressDialogFragment? = null

    fun showConfirmMessage(msg: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), okListener)
            .setNegativeButton(getString(R.string.cancel), cancelListener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okText: String,
        okListener: DialogInterface.OnClickListener,
        cancelText: String,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(okText, okListener)
            .setNegativeButton(cancelText, cancelListener)
            .show()
    }

    fun showYesNoMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(activity)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.yes), okListener)
            .setNegativeButton(getString(R.string.no), cancelListener)
            .show()
    }

    fun showProgress(msg: String) {
        var msg = msg
        progressDialogFragment = ProgressDialogFragment()
        if (TextUtils.isEmpty(msg)) {
            msg = resources.getString(R.string.loading)
        }
        progressDialogFragment!!.setMessage(msg)
        progressDialogFragment!!.show(childFragmentManager, "ProgressDialogFragment")
    }

    fun dismissProgress() {
        if (progressDialogFragment != null) {
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
    }

    fun showMessageShort(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessageLong(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

}
