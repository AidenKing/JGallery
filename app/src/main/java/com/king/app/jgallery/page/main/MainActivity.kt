package com.king.app.jgallery.page.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.king.app.jactionbar.JActionbar
import com.king.app.jgallery.R
import com.king.app.jgallery.base.BaseActivity
import com.king.app.jgallery.databinding.ActivityMainBinding
import com.king.app.jgallery.model.fingerprint.FingerprintHelper
import com.king.app.jgallery.model.fingerprint.OnFingerResultListener
import com.king.app.jgallery.model.setting.Constants
import com.king.app.jgallery.model.setting.SettingProperty
import com.king.app.jgallery.page.file.FolderActivity
import com.king.app.jgallery.page.selector.AlbumSelectorActivity
import com.king.app.jgallery.utils.AppUtil
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    val REQUEST_MOVE_TO = 1;
    val REQUEST_COPY_TO = 2;
    val REQUEST_FOLDER = 3;

    var ftImage = ImageFragment()
    var ftAlbum: AlbumFragment? = null
    var ftFile: FileHomeFragment? = null
    var ftCurrent: Fragment = ftImage

    override fun getContentView(): Int =
        R.layout.activity_main

    override fun createViewModel(): MainViewModel = generateViewModel(MainViewModel::class.java)

    override fun initView() {
        mBinding.model = mModel
    }

    override fun initData() {
        if (AppUtil.isAndroidP()) {
            AppUtil.closeAndroidPDialog()
        }

        RxPermissions(this)
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe({ isGrant ->
                if (SettingProperty.isEnableFingerPrint()) {
                    checkFingerprint()
                }
                else {
                    initCreate()
                }
            }, { throwable ->
                throwable.printStackTrace()
                finish()
            })
    }

    private fun checkFingerprint() {
        var helper = FingerprintHelper()
        helper.onFingerResultListener = object : OnFingerResultListener {
            override fun fingerResult(result: Boolean) {
                initCreate()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn28(this)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            helper.startBiometricPromptIn23(supportFragmentManager)
        }
        else {
            initCreate()
        }
    }

    fun getJActionBar(): JActionbar {
        return mBinding.actionbar
    }

    private fun initCreate() {

        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_ft, ftImage, "ImageFragment")
            .commit()
        mBinding.tvImage.isSelected = true

        mBinding.tvImage.setOnClickListener { showImagePage() }
        mBinding.tvAlbum.setOnClickListener { showAlbumPage() }
        mBinding.tvFile.setOnClickListener { showFolderPage() }
        mBinding.actionbar.setPopupMenuProvider { iconMenuId, anchorView ->
            when(iconMenuId) {
                R.id.menu_sort -> getSortPopup(anchorView!!)
                else -> null
            }
        }

        mModel.allImages.observe(this, Observer { ftImage.showItems(it) })
        mModel.folderImages.observe(this, Observer { ftAlbum?.showAlbumItems(it) })
        mModel.onFoldersChanged.observe(this, Observer { ftAlbum?.showFolders(it) })
        mModel.openImageBySystem.observe(this, Observer { openImageBySystem(it) })
        mModel.moveImages.observe(this, Observer { moveTo() })
        mModel.copyImages.observe(this, Observer { copyTo() })
        mModel.refreshPage.observe(this, Observer { ftImage.refreshPage() })
        mModel.openFolder.observe(this, Observer { openFolder(it) })
    }

    private fun getSortPopup(anchorView: View): PopupMenu? {
        val menu = PopupMenu(this, anchorView)
        menu.menuInflater.inflate(R.menu.album_sort, menu.menu)
        menu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_sort_name -> {
                    mModel.sortAlbum(Constants.SORT_TYPE_NAME)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_NAME)
                }
                R.id.menu_sort_date -> {
                    mModel.sortAlbum(Constants.SORT_TYPE_DATE)
                    SettingProperty.setAlbumSortType(Constants.SORT_TYPE_DATE)
                }
            }
            true
        }
        return menu
    }

    private fun showImagePage() {
        mBinding.tvImage.isSelected = true
        mBinding.tvAlbum.isSelected = false
        mBinding.tvFile.isSelected = false
        mModel.updateTitle("")

        var transaction = supportFragmentManager.beginTransaction()
        transaction.show(ftImage)
        if (ftCurrent != ftImage) {
            transaction.hide(ftCurrent)
        }
        transaction.commit()

        ftCurrent = ftImage
    }

    private fun showAlbumPage() {
        mBinding.tvAlbum.isSelected = true
        mBinding.tvImage.isSelected = false
        mBinding.tvFile.isSelected = false
        mModel.updateFolderTitle()
        mBinding.actionbar.registerPopupMenu(R.id.menu_sort)

        var transaction = supportFragmentManager.beginTransaction()
        if (ftAlbum == null) {
            ftAlbum = AlbumFragment()
            transaction.add(R.id.fl_ft, ftAlbum!!, "AlbumFragment")
        }
        else {
            transaction.show(ftAlbum!!)
        }
        if (ftCurrent != ftAlbum) {
            transaction.hide(ftCurrent)
        }
        transaction.commit()

        ftCurrent = ftAlbum!!
    }

    private fun showFolderPage() {
        mBinding.tvAlbum.isSelected = false
        mBinding.tvImage.isSelected = false
        mBinding.tvFile.isSelected = true
        mModel.updateTitle("")

        // 预加载快捷访问
        mModel.getShortCuts()

        var transaction = supportFragmentManager.beginTransaction()
        if (ftFile == null) {
            ftFile = FileHomeFragment()
            transaction.add(R.id.fl_ft, ftFile!!, "FileHomeFragment")
        }
        else {
            transaction.show(ftFile!!)
        }
        if (ftCurrent != ftFile) {
            transaction.hide(ftCurrent)
        }
        transaction.commit()

        ftCurrent = ftFile!!
    }

    private fun openImageBySystem(path: String) {
        // 用fileProvider的方式只能对私有路径下的图片有效
        // 用StrictMode的方式，加载Application里面
//        val it = Intent(Intent.ACTION_VIEW)
//        val mUri = FileProvider.getUriForFile(this, "$packageName.fileProvider", File(path))
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
//        intent.flags =
//            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        it.setDataAndType(mUri, "image/*")
//        startActivity(it)
        val it = Intent(Intent.ACTION_VIEW)
        val mUri = Uri.parse("file://$path")
        it.setDataAndType(mUri, "image/*")
        startActivity(it)
    }

    override fun onBackPressed() {
        var fragments = supportFragmentManager.fragments
        for (ft in fragments) {
            if (ft.isVisible) {
                var child = ft as AbsChildFragment<*, *>
                if (child.onBackPressed()) {
                    return
                }
                break
            }
        }
        super.onBackPressed()
    }

    private fun moveTo() {
        var intent = Intent(this, AlbumSelectorActivity::class.java)
        startActivityForResult(intent, REQUEST_MOVE_TO)
    }

    private fun copyTo() {
        var intent = Intent(this, AlbumSelectorActivity::class.java)
        startActivityForResult(intent, REQUEST_COPY_TO)
    }

    private fun openFolder(path: String) {
        var intent = Intent(this, FolderActivity::class.java)
        intent.putExtra(FolderActivity.START_PATH, path)
        startActivityForResult(intent, REQUEST_FOLDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_MOVE_TO -> {
                if (resultCode == Activity.RESULT_OK) {
                    mModel.executeMoveTo(mModel.moveImages.value!!, data!!.getStringExtra(AlbumSelectorActivity.DATA_FOLDER))
                    ftImage.cancelSelection()
                    ftAlbum?.cancelSelection()
                }
            }
            REQUEST_COPY_TO -> {
                if (resultCode == Activity.RESULT_OK) {
                    mModel.executeCopyTo(mModel.copyImages.value!!, data!!.getStringExtra(AlbumSelectorActivity.DATA_FOLDER))
                    ftImage.cancelSelection()
                    ftAlbum?.cancelSelection()
                }
            }
            REQUEST_FOLDER -> {
                // 刷新快捷访问
                mModel.getShortCuts()
            }
        }
    }
}