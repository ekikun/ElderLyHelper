package com.eki.schedule.widget

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import com.eki.schedule.R
import com.eki.schedule.databinding.EditBottomsheetLayoutBinding
import com.eki.schedule.entity.ScheduleEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import android.text.format.DateFormat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData



class EditBottomDialog(context: Context): BottomSheetDialogFragment(){

    private var mDialog:BottomSheetDialog?=null

    private var listener:editListener? = null

    private var mBinding:EditBottomsheetLayoutBinding? = null

    private var imm:InputMethodManager? = null

    private var time:Long? = null

    private val mContext = context

    private var isRepeat = false

    private var isOnEditing = false

    private var editScheduleEntity:ScheduleEntity? = null

    val timeLiveData:MutableLiveData<Long> = MutableLiveData()

    interface editListener{
        fun setTime(time: MutableLiveData<Long>)
        fun done(scheduleEntity: ScheduleEntity)
        fun editDone(scheduleEntity: ScheduleEntity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
       return LayoutInflater.from(context).inflate(R.layout.edit_bottomsheet_layout, container,false).also {
           mBinding = DataBindingUtil.bind(it)
       }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        initEvent()
        this.requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun getTheme(): Int {
        return R.style.bottomSheetDialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as editListener
        }catch (e:Exception){

        }
    }

    fun initEvent(){
        Handler().postDelayed({
            imm?.toggleSoftInput(0, InputMethodManager.SHOW_FORCED)
        }, 0,1)
        /**
         * 对于添加模式和选择模式对应的逻辑都是不同的, 一个生成scheduleEntity, 一个对已有的进行修改
         */
        mBinding?.run {
            btnAlarm.setOnClickListener {
                if(!isOnEditing){
                    listener?.setTime(timeLiveData)
                    timeLiveData.observe(this@EditBottomDialog, {
                        time = it
                    })
                }else{
                    listener?.setTime(timeLiveData)
                    timeLiveData.observe(this@EditBottomDialog, {
                        editScheduleEntity?.time = it
                        editScheduleEntity?.timeFormat = DateFormat.format("yyyy-MM-dd HH:mm:ss", it).toString()
                    })
                }
            }
            btnDone?.setOnClickListener{
                if(!isOnEditing){
                    val detail = customEditText.text.trim().toString()
                    val timeFormat = DateFormat.format("yyyy-MM-dd HH:mm:ss", time?:0)
                    val timestamp = System.currentTimeMillis()
                    val scheduleEntity = ScheduleEntity(timestamp,detail, time!!, timeFormat.toString(), false, false,isRepeat)
                    listener?.done(scheduleEntity)
                }else{
                    editScheduleEntity?.detail = mBinding?.customEditText?.text?.trim().toString()
                    listener?.editDone(editScheduleEntity!!)
                    editScheduleEntity = null
                    isOnEditing = false
                    customEditText.setText("")
                    cbRepeat.isChecked = false
                }
            }
            customEditText.run {
                isFocusable = true
                isFocusableInTouchMode = true
                if(isOnEditing){
                    setText(editScheduleEntity?.detail)
                }
            }
            if(isOnEditing){
                cbRepeat.isChecked = editScheduleEntity?.isRepeat!!
            }
            cbRepeat.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isOnEditing){
                    editScheduleEntity?.isRepeat = isChecked
                }else isRepeat = isChecked
            }
        }
        mDialog?.setOnDismissListener {
            lifecycleScope.launch {
                hideInput()
            }
        }
    }

    fun hideInput(){
        try {
            imm?.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }catch (e: Exception){

        }
    }

    /**
     * 编辑模式下调用此函数打开dialog
     * @param manager FragmentManager
     * @param tag String
     * @param scheduleEntity ScheduleEntity 传入被修改的日程对象
     */
    fun editShow(manager:FragmentManager, tag:String ,scheduleEntity: ScheduleEntity){
        isOnEditing = true
        editScheduleEntity = scheduleEntity
        this.show(manager, tag)
    }

}