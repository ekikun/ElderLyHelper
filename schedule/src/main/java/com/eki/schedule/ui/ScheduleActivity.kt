package com.eki.schedule.ui

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.eki.common.base.BaseActivity
import com.eki.common.utils.Constant
import com.eki.common.utils.JsonParser
import com.eki.common.utils.ToastUtils
import com.eki.common.widget.HelpDialogFragment
import com.eki.schedule.R
import com.eki.schedule.databinding.ActivityScheduleBinding
import com.eki.schedule.entity.ScheduleEntity
import com.eki.schedule.recyclerview.RvScheduleAdapter
import com.eki.schedule.viewmodel.ScheduleViewModel
import com.eki.schedule.widget.EditBottomDialog
import com.iflytek.cloud.RecognizerResult
import com.loper7.date_time_picker.dialog.CardDatePickerDialog
import kotlinx.android.synthetic.main.activity_schedule.*
import kotlinx.coroutines.*
import java.util.*
import java.util.regex.Pattern
import kotlin.random.Random

@InternalCoroutinesApi
@Route(path = Constant.ROUTER_SCHEDULE)
class  ScheduleActivity:BaseActivity<ActivityScheduleBinding>(),EditBottomDialog.setListener,HelpDialogFragment.NoticeDialogListener,
        RvScheduleAdapter.RvAdapterListener{

    private var addDialog:EditBottomDialog?=null

    private val mViewModel:ScheduleViewModel by viewModels<ScheduleViewModel>()

    private val rvAdapter = RvScheduleAdapter(this)

    private val linearLayoutManager = LinearLayoutManager(this)

    private var helperText:String? = null

    private var updatePos = -1

    private val isDeleteMode:MutableLiveData<Boolean> = MutableLiveData(false)

    val alarmManager by lazy {this.getSystemService(ALARM_SERVICE) as AlarmManager}

    var dataSchedule:String? = null

   val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            dataSchedule = result?.data?.getStringExtra("DataSchedule")
            val entity = decodeDateSchedule()
            addScheduleEntity(entity)
            buildAlarm(entity)
        }
    }

    override fun initData(savedInstanceState: Bundle?) {
        helpDialogFragment.show(supportFragmentManager, "helper")
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) // 设置默认键盘不弹出
        mBinding?.run {
            btnAddSchedule.setOnClickListener {
                addDialog = EditBottomDialog(this@ScheduleActivity)
                addDialog?.show(supportFragmentManager, "input")
            }
            rvSchedule.run {
                adapter = rvAdapter
                layoutManager = linearLayoutManager
            }
            btnControlSch.setOnClickListener {
                if(isDeleteMode.value!!){
                    lifecycleScope.launch(Dispatchers.Main){
                        withContext(this.coroutineContext){
                            for(i in rvAdapter.itemCount-1 downTo  0){
                                if(rvAdapter.mScheduleList?.get(i)?.is2Delete!!){
                                    Log.d(TAG, "remove at $i")
                                    rvAdapter.mScheduleList?.removeAt(i)
                                    rvAdapter.notifyItemRemoved(i)
                                    rvAdapter.notifyItemRangeChanged(i, rvAdapter.itemCount-i)
                                }
                            }
                        }
                        withContext(this.coroutineContext){
                            for(item in rvAdapter.deleteMap.values){
                                Log.d(TAG, "detail ready2Delete ${item?.detail}")
                                cancelAlarm(item!!)
                                mViewModel.deleteSchedule(item?.timestamp)
                            }
                            rvAdapter.deleteMap.clear()
                        }
                        isDeleteMode.postValue(false)
                    }
                }else{
                    startListening()
                }
            }
            btnAddMicro.setOnClickListener {
                startForAdd()
            }
            toolbarSchedule.tvTitle.text = "小秘书"

        }
        mViewModel.setContext(this)
        lifecycleScope.launch {
            Log.d(TAG, "Read to get data from db")
            val data = async {  mViewModel.getData() }
            rvAdapter.setData(data.await()?: mutableListOf())
            Log.d(TAG, "Get data sucecssfully, dataSize: ${rvAdapter.mScheduleList?.size}")
            rvAdapter.notifyDataSetChanged()
        }
        isDeleteMode.observe(this, {
            Log.d(TAG, "Get in this observer , $it")
            if(it==true){
                rvAdapter.notifyDataSetChanged()
                mBinding?.run {
                    btnControlSch.text = "删除"
                    toolbarSchedule.tvTitle.text = "多选"
                }
            }else{
                rvAdapter.isSelectMode.postValue(false)
                mBinding?.btnControlSch?.text = "语音控制"
                mBinding?.toolbarSchedule?.tvTitle?.text = "小秘书"
            }
        })
        rvAdapter.isSelectMode.observe(this, {

        })
        rvAdapter.setOnItemClickedListener(this)
    }


    override fun executeAfterListening(results: RecognizerResult?) {
        val text = JsonParser.parseIatResult(results?.resultString)
        if(text.contains("添加")){
            startForAdd()
        }else if(text.contains("删除")){
            val tips = "无效删除，请重新说"
            val p = Pattern.compile("(\\d{1,3})")
            val m = p.matcher(text)
            if(m.find()){
                var dPos = m.group(0).toInt()
                Log.d(TAG, "Now len ${rvAdapter.itemCount}")
                if(dPos>rvAdapter.itemCount){
                    ToastUtils.show(tips)
                    startSpeaking(tips)
                }else{
                    dPos -= 1
                    val item = rvAdapter.mScheduleList?.get(dPos)
                    rvAdapter.mScheduleList?.removeAt(dPos)
                    rvAdapter.notifyItemRemoved(dPos)
                    rvAdapter.notifyItemRangeChanged(dPos, rvAdapter.itemCount-dPos)
                    lifecycleScope.launch {
                        cancelAlarm(item!!)
                        mViewModel.deleteSchedule(item.timestamp)
                    }
                }
            }else{
                ToastUtils.show(tips)
                startSpeaking(tips)
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_schedule

    override fun requestPermission(){

    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }

    override fun getDialogView(): View {
        return layoutInflater.inflate(R.layout.helperdialog_layout,null).apply {
            val textView = this.findViewById<TextView>(R.id.text_helper)
            helperText = resources.getString(R.string.helper_text_schedule).replace('$', ' ')
                    .replace('%', '\n')
            textView.text = helperText
            startSpeaking(helperText!!)
        }
    }

    override fun setTime(time:MutableLiveData<Long>){
        CardDatePickerDialog.builder(this)
            .setTitle("选择时间")
            .setLabelText("年","月","日","时","分")
            .setOnChoose {
                Log.d(TAG, "$it")
                time.postValue(it)
            }
            .build().show()
    }

    override fun done(scheduleEntity: ScheduleEntity) {
       addScheduleEntity(scheduleEntity)
       addDialog?.dismiss()
    }

    override fun editDone(scheduleEntity: ScheduleEntity) {
        editScheduleEntity(scheduleEntity)
        updatePos = -1
        addDialog?.dismiss()
    }

    fun buildAlarm(scheduleEntity: ScheduleEntity){
        val requestCode = Random.nextInt(0, Int.MAX_VALUE)
        val alarmIntent = Intent(this, AlarmReciver::class.java).let { intent ->
            intent.putExtra("detail", scheduleEntity.detail)
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        scheduleEntity.alarmRequestCode = requestCode
        if(scheduleEntity.isRepeat){
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                scheduleEntity.time,
                86400000,
                alarmIntent
            )
        }else{
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                scheduleEntity.time,
                alarmIntent
            )
        }
        ToastUtils.show("闹钟在 ${scheduleEntity.timeFormat} 被设置, 重复: ${scheduleEntity.isRepeat}")
    }

    override fun itemLongClickedListener(){
        Log.d(TAG, "Activity in longclickedlistener")
        isDeleteMode.postValue(true)
    }

    override fun itemClickedListener(position: Int) {
        if(!isDeleteMode.value!!){
            updatePos = position
            addDialog = EditBottomDialog(this@ScheduleActivity)
            cancelAlarm(rvAdapter.mScheduleList?.get(position)!!)
            addDialog?.editShow(supportFragmentManager,"Edit",rvAdapter.mScheduleList?.get(position)!!)
        }
    }

    override fun itemSetDoneListener(timeStamp: Long, isDone:Boolean, position: Int){
        lifecycleScope.launch {
            mViewModel.updateIsDone(timeStamp, isDone)
        }
    }

    fun decodeDateSchedule():ScheduleEntity{
        var entity:ScheduleEntity
        dataSchedule?.split(";").apply {
            val timeStamp = System.currentTimeMillis()
            val detail = this?.get(0)
            val time = this?.get(1)?.toLong()
            val timeFormat = this?.get(2)
            val isDone = false
            val is2Delete = false
            val isRepeat = this?.get(3).toBoolean()
            entity = ScheduleEntity(timeStamp, detail,time,timeFormat,isDone, is2Delete, isRepeat)
        }
        return entity
    }

    fun cancelAlarm(scheduleEntity: ScheduleEntity){
        val requestCode = scheduleEntity.alarmRequestCode
        val alarmIntent = Intent(this, AlarmReciver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alarmManager.cancel(alarmIntent)
    }

    fun addScheduleEntity(scheduleEntity: ScheduleEntity){
        buildAlarm(scheduleEntity)
        rvAdapter.addData(scheduleEntity)
        Log.d(TAG, "Data add to list, data: ${scheduleEntity.detail}")
        rvAdapter.notifyItemInserted(0)
        rvAdapter.notifyItemRangeChanged(1 ,rvAdapter.itemCount)
        mBinding?.rvSchedule?.layoutManager?.scrollToPosition(0)
        mViewModel.addSchedule(scheduleEntity)
    }

    fun editScheduleEntity(scheduleEntity: ScheduleEntity){
        if(updatePos==-1) return
        buildAlarm(scheduleEntity)
        rvAdapter.mScheduleList?.set(updatePos, scheduleEntity)
        rvAdapter.notifyItemChanged(updatePos)
        mViewModel.updateSchedule(scheduleEntity)
    }

    fun startForAdd(){
        val intent = Intent(this,LatActivity::class.java)
        startForResult.launch(intent)
    }


}