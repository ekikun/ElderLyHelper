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
import com.tencent.mmkv.MMKV

class MainApp:Application(){

    private val TAG  = "APPLICATION"

    val kv by lazy { MMKV.defaultMMKV() }

    override fun onCreate(){
        super.onCreate()
        initMMKV()
        if(!kv.containsKey("XUNFEIKEY")){
            kv.encode("XUNFEIKEY","=eb45d932")
        }
        if(!kv.containsKey("BAIDU_AK")){
            kv.encode("BAIDU_AK","qrPisDYU6Y5nKyvLddzoufgB")
        }
        if(!kv.containsKey("BAIDU_SK")){
            kv.encode("BAIDU_SK","twFjH6l69lMvyBYFpHbv4NZ7eV2NGC76")
        }
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
        SpeechUtility.createUtility(this.applicationContext, SpeechConstant.APPID +kv.decodeString("XUNFEIKEY"))
    }

    fun initArouter(){
        ARouter.openLog()
        ARouter.openDebug()
        ARouter.init(this)
    }

    fun initMMKV(){
        MMKV.initialize(this.applicationContext)
    }

}