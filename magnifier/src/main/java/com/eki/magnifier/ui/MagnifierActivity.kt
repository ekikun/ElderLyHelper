package com.eki.magnifier.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.window.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.eki.common.base.BaseActivity
import com.eki.common.utils.AppHelper
import com.eki.common.utils.ToastUtils
import com.eki.magnifier.R
import com.eki.magnifier.databinding.ActivityMagnifierBinding
import com.eki.magnifier.viewmodel.MagnifierViewModel
import com.iflytek.cloud.RecognizerResult
import okhttp3.internal.indexOf

import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.*




@Route(path = "/magnifier/MagnifierActivity")
class MagnifierActivity : BaseActivity<ActivityMagnifierBinding>() {

    // 权限码
    private val REQUEST_CODE_CAMERA = 1001
    private val REQUEST_CODE_RECORD_AUDIO = 1002


    // 权限列表
    private val permissions:Array<String> = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val permissionCodes:Array<Int> = arrayOf(REQUEST_CODE_CAMERA, REQUEST_CODE_RECORD_AUDIO)
    private var checkPermission:Boolean = false

    private val mViewModel:MagnifierViewModel by viewModels<MagnifierViewModel>()


    private lateinit var outputDirectory: File
    private  var cameraExecutor: ExecutorService =  Executors.newSingleThreadExecutor()

    private val windowManager:WindowManager = WindowManager(this)

    private lateinit var camera:Camera

    // camera Uses cases
    private lateinit var imageCapture:ImageCapture
    private lateinit var preview:Preview
    private lateinit var imageAnalyzer:ImageAnalysis
    private  var cameraProvider:ProcessCameraProvider? = null
    private  var lensFacing = CameraSelector.LENS_FACING_BACK // 好像是用来定义前后置的？
    private lateinit var mCameraInfo:CameraInfo
    private lateinit var mCameraControl:CameraControl
    private var linearZoom = 0f



    override fun onDestroy() {
        super.onDestroy()
        mIat.stopListening()
    }

    override fun initData(savedInstanceState: Bundle?) {
        requestPermission()
        setupCamera()
        mBinding?.run {
            btnControl.setOnClickListener {
                mIat.startListening(mRecognizerListener)
            }
        }
        mViewModel.orderLiveData.observe(this, {
            when(it){
                "大"-> setLinearZoomUp()
                "小"-> setLinerZoomDown()
                "返回"-> finish()
                "不匹配"-> ToastUtils.show("无效命令,请重新输入")
            }
        })
    }

    override fun execute(results: RecognizerResult?){
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

    override fun requestPermission() {
        for(i in 0..permissions.size-1){
            when {
                ContextCompat.checkSelfPermission(
                        AppHelper.mContext,
                        permissions[i]
                ) == PackageManager.PERMISSION_GRANTED -> {
                   if(i==permissions.size-1) setupCamera()
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
                    checkPermission = true
                } else {
                   ToastUtils.show("拒绝了授权")
                }
                return
            }
            REQUEST_CODE_RECORD_AUDIO -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    if(checkPermission) setupCamera()
                } else {
                    ToastUtils.show("拒绝了授权")
                }
                return
            }
        }
    }



}