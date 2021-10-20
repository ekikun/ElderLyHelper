package com.eki.elderlyhelper
import android.app.Application
import android.util.Log
import androidx.multidex.BuildConfig
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.ocr.sdk.OCR
import com.baidu.ocr.sdk.OnResultListener
import com.baidu.ocr.sdk.exception.OCRError
import com.baidu.ocr.sdk.model.AccessToken
import com.eki.common.utils.AppHelper
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechUtility

class MainApp:Application(){

    private val TAG  = "APPLICATION"

    override fun onCreate(){
        super.onCreate()
        initOcrSdk()
        initXunfeiSdk()
        initArouter()
        AppHelper.init(this.applicationContext)
    }

    fun initOcrSdk(){
        OCR.getInstance(this.applicationContext).initAccessToken(object : OnResultListener<AccessToken>{
            override fun onResult(result: AccessToken?){
                val token:String = result!!.accessToken
                Log.d(TAG, token)
            }

            override fun onError(error: OCRError?) {
                val eroMsg:String = error!!.toString()
                Log.d(TAG, eroMsg)
            }

        }, this.applicationContext)
    }

    fun initXunfeiSdk(){
        SpeechUtility.createUtility(this.applicationContext, SpeechConstant.APPID +"=eb45d932")
    }

    fun initArouter(){
        ARouter.init(this)
        if (BuildConfig.DEBUG){
            ARouter.openLog()
            ARouter.openDebug()
        }
    }
}