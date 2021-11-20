package com.eki.magnifier.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.window.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.eki.common.base.BaseActivity
import com.eki.common.utils.AppHelper
import com.eki.common.utils.Constant
import com.eki.common.utils.ToastUtils
import com.eki.magnifier.R
import com.eki.magnifier.databinding.ActivityMagnifierBinding
import com.eki.magnifier.viewmodel.MagnifierViewModel
import com.iflytek.cloud.RecognizerResult
import kotlinx.android.synthetic.main.activity_magnifier.*

import kotlin.math.*

/**
 * 放大镜Activity
 * @property mViewModel MagnifierViewModel
 * @property windowManager WindowManager
 * @property camera Camera
 * @property imageCapture ImageCapture
 * @property preview Preview
 * @property cameraProvider ProcessCameraProvider?
 * @property lensFacing Int
 * @property mCameraInfo CameraInfo
 * @property mCameraControl CameraControl
 * @property linearZoom Float
 * @property helperText String?
 */
@Route(path = Constant.ROUTER_MAGNIFIER)
class MagnifierActivity : BaseActivity<ActivityMagnifierBinding>() {

    private val mViewModel:MagnifierViewModel by viewModels<MagnifierViewModel>()

    private val windowManager:WindowManager = WindowManager(this)

    private lateinit var camera:Camera

    // 相机相关参数
    private lateinit var imageCapture:ImageCapture
    private lateinit var preview:Preview
    private  var cameraProvider:ProcessCameraProvider? = null
    private  var lensFacing = CameraSelector.LENS_FACING_BACK // 用来定义前后置的
    private lateinit var mCameraInfo:CameraInfo
    private lateinit var mCameraControl:CameraControl
    private var linearZoom = 0f

    private var helperText:String? = null

    override fun onDestroy() {
        super.onDestroy()
        mIat.stopListening()
    }

    override fun initData(savedInstanceState: Bundle?) {
        initTitle()
        helpDialogFragment.show(supportFragmentManager, "missiles")
        requestPermission()
        setupCamera()
        mBinding?.run {
            toolbarMagnifier?.tvTitle.text ="放大镜"
            btnControl.setOnClickListener {
                mIat.startListening(mIatRecognizerListener)
            }

        }
        mViewModel.orderLiveData.observe(this, {
            when(it){
                "大"-> setLinearZoomUp()
                "小"-> setLinerZoomDown()
                "返回"-> finish()
                "不匹配"-> {
                    ToastUtils.show("无效命令,请重新读")
                    startSpeaking("无效命令，请重新读")
                }
            }
        })
    }

    override fun executeAfterListening(results: RecognizerResult?){
        val order = mViewModel.encodeOrder(results)
        mViewModel.orderLiveData.postValue(order)
    }

    override fun getLayoutId() = R.layout.activity_magnifier



    companion object{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }


    private fun setupCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener({
            Log.d(TAG, "Run")
             }, ContextCompat.getMainExecutor(this))
        bindCameraUseCases()
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases(){
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = windowManager.currentWindowMetrics.bounds
        Log.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Log.d(TAG, "Preview aspect ratio: $screenAspectRatio")

        // CameraProvider
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        // Preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .build()
        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .build()
        cameraProvider.unbindAll()
        try {
            //这里拿到camera对象可以取出两个重要的对象来用
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture)
            //用来聚焦、手势、闪光灯、手电等操作
            mCameraInfo = camera?.cameraInfo
            mCameraControl = camera?.cameraControl
            preview?.setSurfaceProvider(mBinding?.previewView?.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }


    /**
     * 设置屏幕分辨率
     * @param width Int
     * @param height Int
     * @return Int
     */
    private fun aspectRatio(width: Int, height: Int): Int{
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    fun setLinearZoomUp(){
        if(linearZoom<0.9f){
            ToastUtils.show("放大")
            linearZoom+=0.3f
        }
        mCameraControl.setLinearZoom(linearZoom)
        Log.d(TAG, "$linearZoom")
    }

    fun  setLinerZoomDown(){
         if(linearZoom>0.1f){
             ToastUtils.show("缩小")
             linearZoom-=0.3f
         }
        mCameraControl.setLinearZoom(linearZoom)
        Log.d(TAG, "$linearZoom")
    }


    override fun getDialogView():View{
        return layoutInflater.inflate(R.layout.helperdialog_layout,null).apply {
            val textView = this.findViewById<TextView>(R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_magnifier).replace('$', ' ')
                    .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)
        }
    }

    override fun executeAfrPermitted() {
        setupCamera()
    }

    override fun initTitle() {
        mBinding?.run {
            toolbarMagnifier.tvTitle.text = "放大镜"
            setSupportActionBar(toolbarMagnifier.toolbar)
            toolbarMagnifier.toolbar.inflateMenu(R.menu.common_menu)
        }
    }
}