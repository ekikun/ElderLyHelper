package com.eki.textocr.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
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
import com.eki.common.utils.JsonParser
import com.eki.common.utils.ToastUtils
import com.eki.textocr.Bean.TextBean
import com.eki.textocr.R
import com.eki.textocr.databinding.ActivityTextreaderBinding
import com.eki.textocr.utils.FileUtil
import com.eki.textocr.utils.RecognizeService
import com.google.gson.Gson
import com.iflytek.cloud.RecognizerResult
import com.tencent.mmkv.MMKV
import java.lang.StringBuilder

/**
 * 文字识别类
 * @property kv (com.tencent.mmkv.MMKV..com.tencent.mmkv.MMKV?) 用于阅读记忆持久化
 * @property helperText String?
 * @property hasGotToken Boolean
 * @property ocrTexts String?
 */
@Route(path = Constant.ROUTER_OCR)
class TextReaderActivity: BaseActivity<ActivityTextreaderBinding>() {

    companion object{
        private const val REQUEST_CODE_GENERAL_BASIC = 106
    }

    private val kv by lazy{MMKV.defaultMMKV()}

    private var helperText: String? = null

    private var hasGotToken:Boolean = false

    private var ocrTexts:String?=null

    override fun initData(savedInstanceState: Bundle?) {
        initTitle()
        requestPermission()
        helpDialogFragment.show(supportFragmentManager,"OCR")
        initAccessTokenWithAkSk()
        mBinding?.run {
            tvOcr.text = kv.decodeString(Constant.LAST_SAVE)
            btnOcr.setOnClickListener {
               if(checkTokenStatus()){
                  startOcr()
               }
            }
            btnPause.setOnClickListener {
                pauseSpeaking()
            }
            btnResume.setOnClickListener {
                if(!mTts.isSpeaking){
                    if(ocrTexts==null){
                        ToastUtils.show("当前没有内容可以合成")
                    }else startSpeaking(ocrTexts!!)
                }else resumeSpeaking()
            }
            btnControlOcr.setOnClickListener {
                startListening()
            }
        }
    }

    override fun executeAfterListening(results: RecognizerResult?) {
        val text = JsonParser.parseIatResult(results?.resultString)
        when {
            text.contains("识别") -> {
                startOcr()
            }
            text.contains("返回") -> {
                finish()
            }
            text.contains("停止") -> {
                stopSpeaking()
            }
            text.contains("暂停")->{
                pauseSpeaking()
            }
            text.contains("恢复")->{
                if(!mTts.isSpeaking){
                    if(ocrTexts==null){
                        ToastUtils.show("当前没有内容可以合成")
                    }else startSpeaking(ocrTexts!!)
                }else resumeSpeaking()
            }
            text.contains("继续")->{
                if(!mTts.isSpeaking){
                    if(ocrTexts==null){
                        ToastUtils.show("当前没有内容可以合成")
                    }else startSpeaking(ocrTexts!!)
                }else resumeSpeaking()
            }
            else -> {
                ToastUtils.show("无效命令，请重新读")
                startSpeaking("无效命令，请重新读")
            }
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_textreader


    override fun getDialogView(): View {
        return layoutInflater.inflate(R.layout.common_helperdialog_layout, null).apply {
            val textView = this.findViewById<TextView>(R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_textreader).replace('$', ' ')
                .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)

        }
    }



    // 初始化ocr sdk的参数
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
        }, applicationContext, kv.decodeString(Constant.BAIDU_AK), kv.decodeString(Constant.BAIDU_SK))
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
                val gson = Gson()
                val wordsList = gson.fromJson(it,TextBean::class.java).words_result
                val stringBuilder:StringBuilder = StringBuilder().apply {
                    append("\n\n\n          ")
                }
                for(item in wordsList){
                    stringBuilder.append("${item.words}")
                }
                ocrTexts = stringBuilder.toString()
                kv.encode(Constant.LAST_SAVE, ocrTexts) // 对当前内容持久化存储，下次进入界面将显示
                startSpeaking(ocrTexts!!)
            }
        }
    }


    @SuppressLint("ResourceAsColor")
    override fun showTtsProgress(beginPos: Int, endPos: Int){
        super.showTtsProgress(beginPos, endPos)
        mBinding?.tvOcr?.apply {
            val style = SpannableStringBuilder(ocrTexts).apply {
                setSpan(ForegroundColorSpan(R.color.red), beginPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            text = style
        }
    }

    override fun executeAfrPermitted() {

    }

    fun startOcr(){
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

    override fun initTitle() {
        mBinding?.toolbarOcr?.apply {
            tvTitle.text = "扫描阅读"
            setSupportActionBar(toolbar)
            toolbar.inflateMenu(R.menu.common_menu)
        }
    }
}