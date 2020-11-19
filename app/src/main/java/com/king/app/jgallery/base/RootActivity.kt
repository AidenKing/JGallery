package com.king.app.jgallery.base

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.king.app.jgallery.R
import com.king.app.jgallery.view.dialog.ProgressDialogFragment

/**
 * 描述:
 *
 * 作者：景阳
 *
 * 创建时间: 2020/1/21 13:05
 */
abstract class RootActivity : AppCompatActivity() {

    private var progressDialogFragment: ProgressDialogFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        // full screen
        //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //prevent from task manager take screenshot
        //also prevent from system screenshot
        //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (updateStatusBarColor()) {
            //            ScreenUtils.setStatusBarColor(this, getResources().getColor(R.color.status_bar_bg));
        }

        super.onCreate(savedInstanceState)
    }

    /**
     * 仅LoginActivity不应用，单独覆写
     * @return
     */
    open fun updateStatusBarColor(): Boolean = true

    fun showConfirmMessage(msg: String, listener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(null)
            .setMessage(msg)
            .setPositiveButton(getString(R.string.ok), listener)
            .show()
    }

    fun showConfirmCancelMessage(
        msg: String,
        okListener: DialogInterface.OnClickListener,
        cancelListener: DialogInterface.OnClickListener?
    ) {
        AlertDialog.Builder(this)
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
        AlertDialog.Builder(this)
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
        AlertDialog.Builder(this)
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
        progressDialogFragment!!.show(supportFragmentManager, "ProgressDialogFragment")
    }

    fun dismissProgress() {
        if (progressDialogFragment != null) {
            progressDialogFragment!!.dismissAllowingStateLoss()
        }
    }

    fun showMessageShort(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun showMessageLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}
