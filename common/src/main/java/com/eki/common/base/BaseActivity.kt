package com.eki.common.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.eki.common.utils.ToastUtils
import com.eki.common.widget.HelpDialogFragment
import com.iflytek.cloud.*


abstract class BaseActivity<T : ViewDataBinding>:AppCompatActivity(), HelpDialogFragment.NoticeDialogListener, HelpDialogFragment.DialogViewGetter{

    var mBinding:T? = null
    var TAG = "BaseActivity"

    val helpDialogFragment = HelpDialogFragment()

    val initIatListener: InitListener = InitListener{
       if(it!=ErrorCode.SUCCESS) ToastUtils.show("初始化失败，错误码：${it}")
    }
    val mIat:SpeechRecognizer by lazy { SpeechRecognizer.createRecognizer(this, initIatListener) }
    private val mEngineType = SpeechConstant.TYPE_CLOUD
    private val mIatresultType = "json"
    private val mIatlanguageType = "zh_cn"
    private val iat_vad_bos = "4000"
    private val iat_vad_eos = "2000"
    private val iat_asr_ptt = "0"
    val mIatRecognizerListener: RecognizerListener = object:RecognizerListener{
        override fun onVolumeChanged(p0: Int, p1: ByteArray?) {

        }

        override fun onResult(results: RecognizerResult?, isLast: Boolean) {
            if (isLast){
                executeOrder(results)
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


    val initTtsListener: InitListener = InitListener{
        if(it!=ErrorCode.SUCCESS) ToastUtils.show("初始化失败，错误码：${it}")
    }
    val mTts by lazy { SpeechSynthesizer.createSynthesizer(this, initIatListener) }
    val speaker = "xiaoyan"

    // 缓冲进度
    private var mPercentForBuffering = 0
    // 播放进度
    private val mPercentForPlaying = 0
    private val mTtsListener:SynthesizerListener = object :SynthesizerListener{
        override fun onSpeakBegin() {
           ToastUtils.show("语音播报命令")
        }

        override fun onBufferProgress(p0: Int, p1: Int, p2: Int, p3: String?) {

        }

        override fun onSpeakPaused() {

        }

        override fun onSpeakResumed() {

        }


        override fun onSpeakProgress(
                percent: Int, beginPos: Int, endPos: Int,
        ) {
            mPercentForBuffering = percent

        }

        override fun onCompleted(p0: SpeechError?) {
            ToastUtils.show("播报完毕")
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {

            //	 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            //	 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                val sid: String? = obj?.getString(SpeechEvent.KEY_EVENT_SESSION_ID)
                Log.d(TAG, "session id =$sid")
            }
        }

    }




    fun setIatParams(){
        mIat.setParameter(SpeechConstant.PARAMS, null)
        // 引擎类型
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType)
        // 返回参数类型
        mIat.setParameter(SpeechConstant.RESULT_TYPE, mIatresultType)
        // 设置语言种类，这里设置仅支持普通话
        mIat.setParameter(SpeechConstant.LANGUAGE, mIatlanguageType)

        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin")

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(
                SpeechConstant.VAD_BOS,
                iat_vad_bos
        )

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(
                SpeechConstant.VAD_EOS,
                iat_vad_eos
        )

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(
                SpeechConstant.ASR_PTT,
                iat_asr_ptt
        )
    }

    fun setTtsParams(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null)
        // 根据合成引擎设置相应参数
        if (mEngineType == SpeechConstant.TYPE_CLOUD){
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD)
            // 支持实时音频返回，仅在 synthesizeToUri 条件下支持
            mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY, "1")
            //	mTts.setParameter(SpeechConstant.TTS_BUFFER_TIME,"1");

            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, speaker)
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, "50")
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, "50")
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, "50")
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL)
            mTts.setParameter(SpeechConstant.VOICE_NAME, "")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding =  DataBindingUtil.setContentView(this, getLayoutId())
        initData(savedInstanceState)
    }

    abstract fun initData(savedInstanceState: Bundle?)

    abstract fun executeOrder(results: RecognizerResult?)

    abstract fun getLayoutId():Int

    abstract fun requestPermission()

    protected  open fun startListening(){
        setIatParams() // 每次都重新设置参数
        val resultCode = mIat.startListening(mIatRecognizerListener)
        if(resultCode!=ErrorCode.SUCCESS){
            ToastUtils.show("识别命令失败")
        }
    }

    protected  fun startSpeaking(text: String){
        setTtsParams()
        val resultCode = mTts.startSpeaking(text, mTtsListener)
        if(resultCode!=ErrorCode.SUCCESS){
            ToastUtils.show("语音播报失败")
        }
    }

    protected fun stopSpeaking(){
        mTts.stopSpeaking()
    }



}