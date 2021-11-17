package com.eki.schedule.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.eki.schedule.R
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
class AlarmReciver:BroadcastReceiver() {

    companion object{
        const val CHANNEL_ID = "CHANNEL_ONE"
        const val NOTIFICATION_ID = 1
    }

    private lateinit var mContext:Context

    private var content:String? = null

    private var builder:NotificationCompat.Builder? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        mContext = context!!
        content = intent?.getStringExtra("detail")
        createChannel()
        createNotification()
    }


    fun createNotification(){
        val intent = Intent(mContext, ScheduleActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0)

        builder = NotificationCompat.Builder(mContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_clock)
            .setContentTitle("您有新的通知！")
            .setContentText(content)
            .setLargeIcon(BitmapFactory.decodeResource(mContext.resources,R.drawable.ic_clock))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        NotificationManagerCompat.from(mContext).notify(NOTIFICATION_ID, builder!!.build())
    }

    fun createChannel(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "channel test"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {

                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                   mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

}