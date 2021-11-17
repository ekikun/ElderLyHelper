package com.eki.schedule.entity;

import android.app.PendingIntent;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ScheduleEntity {

    public ScheduleEntity(long timestamp,String detail,Long time, String timeFormat, Boolean isDone, Boolean is2Delete, Boolean isRepeat){
        this.timestamp = timestamp;
        this.detail = detail;
        this.time = time;
        this.timeFormat = timeFormat;
        this.isDone = isDone;
        this.is2Delete = false;
        this.isRepeat = isRepeat;
    }
    @PrimaryKey
    public long timestamp;

    @ColumnInfo(name = "detail")
    public String detail;

    @ColumnInfo(name = "time")
    public Long time;

    @ColumnInfo(name = "timeFormat")
    public String timeFormat;

    @ColumnInfo(name = "isDone")
    public Boolean isDone;

    @ColumnInfo(name = "is2Delete")
    public Boolean is2Delete;

    @ColumnInfo(name = "alarmRequestCode")
    public int alarmRequestCode;

    @ColumnInfo(name = "isRepeat")
    public Boolean isRepeat;
}
