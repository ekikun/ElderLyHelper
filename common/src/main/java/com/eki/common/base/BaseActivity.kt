package com.eki.common.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.eki.common.utils.ToastUtils
import com.iflytek.cloud.*


abstract class BaseActivity<T: ViewDataBinding>:AppCompatActivity(){

    var mBinding:T? = null

    var TAG = "BaseActivity"

   val initListener: InitListener = InitListener{
       if(it!=ErrorCode.SUCCESS) ToastUtils.show("初始化失败，错误码：${it}")
    }

     val mIat:SpeechRecognizer by lazy { SpeechRecognizer.createRecognizer(this, initListener) }

    private val mEngineType = SpeechConstant.TYPE_CLOUD

    private val resultType = "json"

    private val languageType = "zh_cn"

    private val vad_bos = "4000"

    private val vad_eos = "2000"

    private val asr_ptt = "0"

    val mRecognizerListener: RecognizerListener = object:RecognizerListener{
        override fun onVolumeChanged(p0: Int, p1: ByteArray?) {

        }

        override fun onResult(results: RecognizerResult?, isLast: Boolean) {
            if (isLast){
                execute(results)
                Log.d(TAG, "onResult 结束")
            }
        }

        override fun onBeginOfSpeech(){
            ToastUtils.show("开始说话")
        }

        override fun onEvent(p0: Int, p1: Int, p2: Int, p3: Bundle?) {

        }

        override fun onEndOfSpeech(){
            ToastUtils.show("结束说话")
        }

        override fun onError(error: SpeechError?) {
           ToastUtils.show(error!!.getPlainDescription(true))
        }

    }


    fun setParams(){
        mIat.setParameter(SpeechConstant.PARAMS, null)
        // 引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
        // 返回参数类型
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType)
        // 设置语言种类，这里设置仅支持普通话
        mIat.setParameter(SpeechConstant.LANGUAGE, languageType)

        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin")

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(
            SpeechConstant.VAD_BOS,
            vad_bos
        )

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(
            SpeechConstant.VAD_EOS,
            vad_eos
        )

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(
            SpeechConstant.ASR_PTT,
            asr_ptt
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =  DataBindingUtil.setContentView(this, getLayoutId())
        initData(savedInstanceState)
    }

    abstract fun initData(savedInstanceState: Bundle?)

    abstract fun execute(results:RecognizerResult?)

    abstract fun getLayoutId():Int

    abstract fun requestPermission()

    protected  open fun executeListening(){
        setParams() // 每次都重新设置参数
        val resultCode = mIat.startListening(mRecognizerListener)
        if(resultCode!=ErrorCode.SUCCESS){
            ToastUtils.show("识别命令失败")
        }
    }

}