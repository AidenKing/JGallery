package com.king.app.jgallery.model.fingerprint

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.FragmentManager
import com.king.app.jgallery.page.setting.FingerprintDialog
import com.king.app.jgallery.utils.DebugLog
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class FingerprintHelper {

    var onFingerResultListener: OnFingerResultListener? = null
    var keyStore: KeyStore? = null

    companion object {
        fun isDeviceSupport(context: Context): Boolean = FingerprintManagerCompat.from(context).isHardwareDetected
        fun isEnrolled(context: Context): Boolean = FingerprintManagerCompat.from(context).hasEnrolledFingerprints()
    }

    /**
     * 安卓9.0及以上的指纹识别
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun startBiometricPromptIn28(context: Context) {
        val mBiometricPrompt = BiometricPrompt.Builder(context)
            .setTitle("指纹验证")
            .setDescription("请验证指纹登录应用")
            .setNegativeButton("取消", context.getMainExecutor(), DialogInterface.OnClickListener { dialogInterface, I ->
                // ToastUtil.showToast(mContext, "Cancel")
            })
            .build()

        val mCancellationSignal = CancellationSignal()
        mCancellationSignal.setOnCancelListener {
            DebugLog.e("canceled")
        }

        val mAuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFingerResultListener?.fingerResult(false)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onFingerResultListener?.fingerResult(true)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFingerResultListener?.fingerResult(false)
            }
        }
        mBiometricPrompt.authenticate(mCancellationSignal, context.mainExecutor, mAuthenticationCallback)
    }

    /**
     * 安卓6.0以上，9.0以下的指纹识别
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun startBiometricPromptIn23(supportFragmentManager: FragmentManager) {
        initKey()
        initCipher(supportFragmentManager)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore?.load(null)
            //秘钥生成器
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder("DEFAULT_KEY_NAME",
                KeyProperties.PURPOSE_ENCRYPT or
                        KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initCipher(supportFragmentManager: FragmentManager) {
        try {
            val key = keyStore?.getKey("DEFAULT_KEY_NAME", null)
            val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val fragment23 = FingerprintDialog()
            fragment23.cipher = cipher
            fragment23.onFingerPrintListener = onFingerResultListener
            fragment23.show(supportFragmentManager, "FingerprintDialog")
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
