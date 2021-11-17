package com.eki.textocr.ui

import android.Manifest
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.baidu.ocr.sdk.OCR
import com.baidu.ocr.sdk.OnResultListener
import com.baidu.ocr.sdk.exception.OCRError
import com.baidu.ocr.sdk.model.AccessToken
import com.baidu.ocr.ui.camera.CameraActivity
import com.eki.common.base.BaseActivity
import com.eki.common.utils.AppHelper
import com.eki.common.utils.Constant
import com.eki.common.utils.ToastUtils
import com.eki.textocr.R
import com.eki.textocr.databinding.ActivityTextreaderBinding
import com.eki.textocr.utils.FileUtil
import com.eki.textocr.utils.RecognizeService
import com.iflytek.cloud.RecognizerResult

@Route(path = Constant.ROUTER_OCR)
class TextReaderActivity: BaseActivity<ActivityTextreaderBinding>() {

    companion object{
        private const val REQUEST_CODE_GENERAL_BASIC = 106
    }

    // 权限码
    private val REQUEST_CODE_CAMERA = 1001
    private val REQUEST_CODE_RECORD_AUDIO = 1002


    // 权限列表
    private val permissions:Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionCodes:Array<Int> = arrayOf(REQUEST_CODE_CAMERA, REQUEST_CODE_RECORD_AUDIO)

    private var helperText: String? = null

    private var hasGotToken:Boolean = false

    override fun initData(savedInstanceState: Bundle?) {
        requestPermission()
        initAccessTokenWithAkSk()
        mBinding?.run {
            btnOcr.setOnClickListener {
               if(checkTokenStatus()){
                   val intent: Intent = Intent(
                       this@TextReaderActivity,
                       CameraActivity::class.java
                   )
                   intent.putExtra(
                       CameraActivity.KEY_OUTPUT_FILE_PATH,
                       FileUtil.getSaveFile(application).getAbsolutePath()
                   )
                   intent.putExtra(
                       CameraActivity.KEY_CONTENT_TYPE,
                       CameraActivity.CONTENT_TYPE_GENERAL
                   )
                   startActivityForResult(intent, TextReaderActivity.REQUEST_CODE_GENERAL_BASIC)
               }
            }
        }
    }

    override fun executeAfterListening(results: RecognizerResult?) {

    }

    override fun getLayoutId(): Int = R.layout.activity_textreader

    override fun requestPermission() {
        for(i in 0..permissions.size-1){
            when {
                ContextCompat.checkSelfPermission(
                    AppHelper.mContext,
                    permissions[i]
                ) == PackageManager.PERMISSION_GRANTED -> {


                }
                shouldShowRequestPermissionRationale("") -> {

                }
                else -> {
                    requestPermissions(
                        arrayOf(permissions[i]),
                        permissionCodes[i]
                    )
                }
            }
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {

                } else {
                    ToastUtils.show("拒绝了授权")
                }
                return
            }
            REQUEST_CODE_RECORD_AUDIO -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {

                } else {
                    ToastUtils.show("拒绝了授权")
                }
                return
            }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }

    override fun getDialogView(): View {
        return layoutInflater.inflate(R.layout.common_helperdialog_layout, null).apply {
            val textView = this.findViewById<TextView>(R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_textreader).replace('$', ' ')
                .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)

        }
    }


    private fun initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(object : OnResultListener<AccessToken> {
            override fun onResult(result: AccessToken) {
                val token = result.accessToken
                hasGotToken = true
            }

            override fun onError(error: OCRError) {
                error.printStackTrace()
                Log.d("AK，SK方式获取token失败", "${error.message}")
            }
        }, applicationContext, "qrPisDYU6Y5nKyvLddzoufgB", "twFjH6l69lMvyBYFpHbv4NZ7eV2NGC76")
    }

    private fun checkTokenStatus():Boolean{
        if(!hasGotToken){
            ToastUtils.show("还没有获取Token")
        }
        return hasGotToken
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 识别成功回调，通用文字识别

        if (requestCode == TextReaderActivity.REQUEST_CODE_GENERAL_BASIC && resultCode == RESULT_OK) {
            RecognizeService.recognizeGeneralBasic(this,
                FileUtil.getSaveFile(applicationContext).absolutePath
            ) {
                Log.d(TAG, it)
            }
        }
    }
}