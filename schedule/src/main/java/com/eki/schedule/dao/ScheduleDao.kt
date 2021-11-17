package com.eki.schedule.dao

import android.app.PendingIntent
import android.telecom.Call
import androidx.lifecycle.LiveData
import androidx.room.*
import com.eki.schedule.entity.ScheduleEntity
import java.sql.Timestamp

@Dao
interface ScheduleDao{
    @Query("select * from scheduleentity")
    suspend fun getSchedules():MutableList<ScheduleEntity>

    @Insert
    suspend fun addSchedule(scheduleEntity: ScheduleEntity)

    @Query("delete from scheduleentity where timestamp=:timestamp")
    suspend fun deleteSchedule(timestamp: Long)

    @Query("delete from scheduleentity")
    suspend fun deleteAll()

    @Query("update scheduleentity set isDone=:isDone where timestamp=:timestamp")
    suspend fun updateIsDone(timestamp: Long, isDone:Boolean)

   @Update
    suspend fun update(scheduleEntity: ScheduleEntity)

    @Query("select * from scheduleentity where timestamp=:timestamp")
    suspend fun queryById(timestamp: Long):ScheduleEntity?
}