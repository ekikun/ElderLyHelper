package com.eki.elderlyhelper

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.eki.common.base.BaseActivity
import com.eki.common.utils.Constant
import com.eki.common.utils.JsonParser
import com.eki.common.utils.ToastUtils
import com.eki.elderlyhelper.databinding.ActivityMainBinding
import com.iflytek.cloud.RecognizerResult
import kotlinx.android.synthetic.main.activity_main.*

/**
 *   主界面，可导航到三个组件和语音播报功能介绍
 */
class MainActivity : BaseActivity<ActivityMainBinding>(),MainAdapter.setonClickedLisentener{

    private var helperText:String? = null

    private var dataList:MutableList<MainData> = mutableListOf()

    private val mainAdapter:MainAdapter = MainAdapter(this)


    override fun initData(savedInstanceState: Bundle?) {
        initTitle()
        setData()
        mainAdapter.getData(dataList)
        mainAdapter.notifyDataSetChanged()
        window.setStatusBarColor(getColor(com.eki.common.R.color.white))
        requestPermission()
        helpDialogFragment.show(supportFragmentManager, "main")
        mBinding?.run {
            rv_main.layoutManager = GridLayoutManager(this@MainActivity,2)
            rv_main.adapter = mainAdapter
            btnControlMain.setOnClickListener {
                startListening()
            }
        }
    }

    override fun executeAfterListening(results: RecognizerResult?) {
        val text = JsonParser.parseIatResult(results?.resultString)
        when{
            text.contains("放大镜")->{
                startMagnifier()
            }
            text.contains("小秘书")->{
                startSchedule()
            }
            text.contains("扫描阅读")->{
                startOcr()
            }
            text.contains("文字识别")->{
                startOcr()
            }text.contains("功能介绍")->{
                listenFunctionDetail()
            }
            else ->{
                ToastUtils.show("无效命令，请重新读")
                startSpeaking("无效命令，请重新读")
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_main

    override fun executeAfrPermitted() {

    }

    @SuppressLint("InflateParams")
    override fun getDialogView(): View {
        return layoutInflater.inflate(com.eki.magnifier.R.layout.helperdialog_layout,null).apply {
            val textView = this.findViewById<TextView>(com.eki.magnifier.R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_main).replace('$', ' ')
                    .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)
        }
    }


    fun startMagnifier(){
        ARouter.getInstance().build(Constant.ROUTER_MAGNIFIER)
                .navigation()
    }

    fun startSchedule(){
        ARouter.getInstance().build(Constant.ROUTER_SCHEDULE)
                .navigation()
    }

    fun startOcr(){
        ARouter.getInstance().build(Constant.ROUTER_OCR)
                .navigation()
    }

    fun listenFunctionDetail(){
        startSpeaking(getString(R.string.function_brief))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun setData(){
        dataList.add(MainData("放大镜", resources.getDrawable(R.drawable.ic_manifiger), "mag"))
        dataList.add(MainData("小秘书", resources.getDrawable(R.drawable.ic_schedule),"sch"))
        dataList.add(MainData("扫描阅读",resources.getDrawable(R.drawable.ic_ocr_reader), "ocr"))
        dataList.add(MainData("功能介绍",resources.getDrawable(R.drawable.ic_about), "about"))
    }

    override fun onItemClicked(function: String) {
        when{
            function.equals("mag")->{
                startMagnifier()
            }
            function.equals("sch")->{
                startSchedule()
            }
            function.equals("ocr")->{
                startOcr()
            }
            function.equals("about")->{
                listenFunctionDetail()
            }
        }
    }

    override fun initTitle() {
        mBinding?.toolbarMain?.apply {
            tvTitle.text = "长者小帮手"
            setSupportActionBar(toolbar)
            toolbar.inflateMenu(R.menu.common_menu)
        }
    }
}


