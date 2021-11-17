package com.eki.schedule.database

import android.content.Context
import androidx.room.*
import com.eki.common.utils.AppHelper
import com.eki.schedule.dao.ScheduleDao
import com.eki.schedule.entity.ScheduleEntity
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized


@InternalCoroutinesApi
@Database(entities = arrayOf(ScheduleEntity::class), exportSchema = false,version = 7)
abstract class ScheduleDataBase:RoomDatabase() {
    abstract fun userDao(): ScheduleDao
    val dao: ScheduleDao by lazy { userDao() }

}
