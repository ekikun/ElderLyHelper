package com.eki.schedule.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
object ScheduleDataBaseManager {

    val DB_NAME = "SCHEDULEDB"

    private val MIGRATIONS = arrayOf(Migration1)

    fun getDb(context:Context):ScheduleDataBase{
       return  Room.databaseBuilder(context, ScheduleDataBase::class.java, DB_NAME)
                .addCallback(CreatedCallBack)
                .fallbackToDestructiveMigration()
                .build()
    }

    private object CreatedCallBack : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            //在新装app时会调用，调用时机为数据库build()之后，数据库升级时不调用此函数
            MIGRATIONS.map {
                it.migrate(db)
            }
        }
    }

    private object Migration1 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {

        }
    }

}


