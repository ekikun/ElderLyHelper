package com.eki.schedule.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alibaba.android.arouter.facade.annotation.Route
import com.eki.common.base.BaseActivity
import com.eki.common.utils.AppHelper
import com.eki.common.utils.JsonParser
import com.eki.common.utils.ToastUtils
import com.eki.schedule.R
import com.eki.schedule.databinding.ActivityLatLayoutBinding
import com.iflytek.cloud.RecognizerResult
import org.koin.androidx.scope.getScopeId
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.properties.Delegates

@Route(path = "/schedule/LatActivity")
class LatActivity:BaseActivity<ActivityLatLayoutBinding>(){

    var helperText:String? = null

    private val FIRST_TIME = 1

    private val SECONDE_TIME = 2

    private val switchLiveData:MutableLiveData<Int> = MutableLiveData<Int>(FIRST_TIME)

    private lateinit var timeFormat:String

    private var time = 0L

    private var isRepeat = false

    private var pre:String? = null

    private lateinit var detail:String

    override fun initData(savedInstanceState: Bundle?) {
        helpDialogFragment.show(supportFragmentManager, "lat")
        initTitle()
        switchLiveData.observe(this,{
            when(it){
                FIRST_TIME->{
                    mBinding?.run{
                        editContent?.hint = "今天/明天/后天/几月几日, 上午/下午/晚上 几点几分/几点整 重复/不重复"
                        tvTimes.text = "第一次说"
                    }
                }
                SECONDE_TIME->{
                    mBinding?.editContent?.setText("")
                    mBinding?.run {
                        editContent?.hint = " 计划做的事情,例如 吃药"
                        tvTimes.text = "第二次说"
                    }
                }
                else->{

                }
            }
        })
        mBinding?.run {
            btnSay.setOnClickListener {
                startListening()
            }
            btnListen.setOnClickListener {
                helpDialogFragment.show(supportFragmentManager, "lat")
            }
            btnDone.setOnClickListener {
               val intent = Intent()
                intent.putExtra("DataSchedule", encodeReturn())
                setResult(RESULT_OK,intent)
                finish()

            }
        }
    }

    override fun executeAfterListening(results: RecognizerResult?) {
        val text = JsonParser.parseIatResult(results?.resultString)
        Log.d(TAG, text)
        mBinding?.editContent?.setText(text)
        when(switchLiveData.value){
            FIRST_TIME->{
                encodeDate()
                pre = "$timeFormat $time ${System.currentTimeMillis()}"
                mBinding?.editContent?.setText(pre)
                switchLiveData.postValue(SECONDE_TIME)
            }
            SECONDE_TIME -> {
                detail = text
                mBinding?.editContent?.setText("$pre\n $text")
                switchLiveData.postValue(FIRST_TIME)
            }
            else->{

            }
        }
    }

    override fun getLayoutId(): Int  = R.layout.activity_lat_layout


    override fun onDialogNegativeClick(dialog: DialogFragment) {
        stopSpeaking()
    }

    override fun getDialogView(): View {
        return layoutInflater.inflate(R.layout.helperdialog_layout, null).apply {
            val textView = this.findViewById<TextView>(R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_lat).replace('$', ' ')
                    .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)
        }
    }

    /**
     *   对第一次返回的值进行正则匹配
     */
   fun encodeDate(){
        val editText = mBinding?.editContent?.text?.trim().toString()
        val p1:Pattern = Pattern.compile("(\\d{1,2})月(\\d{1,2})日")
        val p2:Pattern = Pattern.compile("(\\d{1,2}):(\\d{1,2})")
        val m1 = p1.matcher(editText)
        val m2 = p2.matcher(editText)
       var monthAndDay:String? = ""
       val calendar = Calendar.getInstance()
       val nowMonth = calendar.get(Calendar.MONTH)+1
       var year = calendar.get(Calendar.YEAR)
       var month = ""
       var date = ""
       var hour = ""
       var min = ""
       if(m1.find()){
          // m1匹配成功，即日期格式为x月x日
          monthAndDay  = m1.group(0).apply {
               replace("月",":")
               replace("日","")
           }
           monthAndDay?.split(":").apply {
               if(this?.get(0)?.toInt()!! <nowMonth){
                   year+=1
               }
               month = this.get(0)
               date = this.get(1)
           }
       }else{
           // 日期为今天/明天/后天
           val dayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
           Log.d(TAG, "$dayOfMonth")
           month = nowMonth.toString()
           if(editText.contains("今天")){
                date = calendar.get(Calendar.DATE).toString()
           }else if(editText.contains("明天")){
               if(calendar.get(Calendar.DATE)+1>dayOfMonth){
                   month = "${month.toInt()+1}"
                   date = "1"
               }else date = (calendar.get(Calendar.DATE)+1).toString()
           }else if(editText.contains("后天")){
                if(calendar.get(Calendar.DATE)+2>dayOfMonth){
                    month = "${month.toInt()+1}"
                    date = "2"
                }else date = (calendar.get(Calendar.DATE)+2).toString()
           }
       }
       if(m2.find()){
           // m2匹配成功，即时间格式为几点几分
           val timeCn = m2.group(0)
           timeCn.split(":").apply {
               hour = this.get(0)
               min = this.get(1)
               if(editText.contains("下午")||editText.contains("晚上")){
                   hour = "${hour.toInt()+12}"
               }
           }
       }else{
           // 匹配的时间为xx点整或者xx点半
           val p = Pattern.compile("(\\d{1,2})")
           val m = p.matcher(editText)
           if(m.find()) hour = m.group(0)
           if(editText.contains("下午")||editText.contains("晚上")){
               hour = "${hour.toInt()+12}"
           }
           min = "00"
       }
       timeFormat = "$year-$month-$date $hour:$min:00"
       time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).parse(timeFormat).time //获取Long的时间值
       if(editText.contains("不")) isRepeat = false else true
   }

    fun encodeReturn() = "$detail;$time;$timeFormat;$isRepeat" // 结果字符串格式，在Schedule中方便直接用split方法提取内容

    override fun initTitle() {
        mBinding?.toolbarLat?.apply {
            tvTitle.text = "语音添加"
            setSupportActionBar(toolbar)
            toolbar.inflateMenu(R.menu.common_menu)
        }
    }

    override fun executeAfrPermitted() {

    }

}