package com.eki.schedule.recyclerview

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.eki.schedule.R
import com.eki.schedule.databinding.ItemTodoLayoutBinding
import com.eki.schedule.entity.ScheduleEntity
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
class RvScheduleAdapter(context:Context): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var mScheduleList:MutableList<ScheduleEntity>? = null

    val isSelectMode:MutableLiveData<Boolean> = MutableLiveData(false)

    private val mContext = context

    val deleteMap:MutableMap<Int, ScheduleEntity> = mutableMapOf()

   interface RvAdapterListener{
       fun itemLongClickedListener()
       fun itemClickedListener(position: Int)
       fun itemSetDoneListener(timeStamp:Long, isDone:Boolean, position: Int)
   }

   fun  setOnItemClickedListener(itemClickedListener: RvAdapterListener){
       listener = itemClickedListener
   }

    private var listener:RvAdapterListener? = null

    fun setData(list:MutableList<ScheduleEntity>){
        mScheduleList = list
    }

    fun addData(item: ScheduleEntity){
        mScheduleList?.add(0, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo_layout, parent, false)
        return  MyHolder(itemView).also {
           v ->
           v.itemView.setOnLongClickListener {
               isSelectMode.postValue(true)
               listener?.itemLongClickedListener()
               return@setOnLongClickListener true
           }

       }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item  = mScheduleList?.get(position)
        if(!isSelectMode.value!!){
            holder.itemView.setOnClickListener {
                Log.d("Adapter", "item in $position clicked")
                listener?.itemClickedListener(position)
            }
        }
        val tvDetail = holder.itemView.findViewById<TextView>(R.id.tv_detail).apply {
            text = item?.detail
        }
        holder.itemView.findViewById<TextView>(R.id.tv_alarm_time).apply {
            text = item?.timeFormat
        }
        holder.itemView.findViewById<TextView>(R.id.tv_isRepeat).apply {
            text = if(item?.isRepeat!!) "重复" else "不重复"
        }
        holder.itemView.findViewById<CheckBox>(R.id.check_done).apply {
            isChecked = item?.isDone?:false
            if(isSelectMode.value!!&&isChecked) item?.is2Delete = true
            setOnCheckedChangeListener { buttonView, isChecked ->
                if(isSelectMode.value!!){
                    if(isChecked){
                        item?.is2Delete = true
                        deleteMap.put(position, item!!)
                        Log.d("Adapter", "item: ${item?.detail} ready to delete, in position${position}")
                    }
                }else{
                    if(isChecked){
                        item?.isDone = true
                        tvDetail.paintFlags = Paint. STRIKE_THRU_TEXT_FLAG
                        tvDetail.setTextColor(mContext.getColor(R.color.gray))
                        listener?.itemSetDoneListener(item?.timestamp!!, !item?.isDone, position)
                    }else{
                        item?.isDone = false
                        tvDetail.paintFlags = Paint.ANTI_ALIAS_FLAG
                        tvDetail.setTextColor(mContext.getColor(R.color.black))
                        listener?.itemSetDoneListener(item?.timestamp!!, !item?.isDone, position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int =   mScheduleList?.size?:0

    class MyHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

    }
}