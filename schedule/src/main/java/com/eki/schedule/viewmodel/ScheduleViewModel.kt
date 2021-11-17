package com.eki.schedule.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.eki.schedule.database.ScheduleDataBase
import com.eki.schedule.database.ScheduleDataBaseManager
import com.eki.schedule.entity.ScheduleEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.experimental.property.inject


@InternalCoroutinesApi
class ScheduleViewModel():ViewModel(){

    var scheduleListLiveData:MutableList<ScheduleEntity>? = null



    var mContext:Context?=null

    fun setContext(context: Context){
       mContext = context
       mContext?.let { dataBase = ScheduleDataBaseManager.getDb(context) }
    }


    private var dataBase:ScheduleDataBase?=null

   suspend fun getData() = dataBase?.dao?.getSchedules()

     fun addSchedule(scheduleEntity: ScheduleEntity){
        viewModelScope.launch {
           dataBase?.dao?.addSchedule(scheduleEntity)
        }
    }

    fun updateSchedule(scheduleEntity: ScheduleEntity){
        viewModelScope.launch {
            dataBase?.dao?.update(scheduleEntity)
        }
    }

    fun deleteSchedule(timeStamp: Long){
        viewModelScope.launch {
            dataBase?.dao?.deleteSchedule(timeStamp)
        }
    }

    fun  deleteAll(){
        viewModelScope.launch {
            dataBase?.dao?.deleteAll()
        }
    }

    suspend fun updateIsDone(timeStamp:Long, isDone:Boolean){
        dataBase?.dao?.updateIsDone(timeStamp, isDone)
    }


}