package com.example.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.example.timer.util.PrefUtil
import java.util.Calendar
import kotlin.properties.Delegates

// Do you need this? since a lot of this is dealt with by PrefUtil
// consider moving some of the prefUtil things to here perhaps
class TimerController {
    companion object {
        private var wakeUp by Delegates.notNull<Long>()
        fun setAlarm(context: Context, currSec: Long, remainSec: Long): Long {
            wakeUp = (currSec + remainSec)
//            println("set alarm wakeup: $wakeUp, nowSec: ${Timer.nowSec}")
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            manager.setExact(AlarmManager.RTC_WAKEUP, wakeUp, pendingIntent)

            PrefUtil.setAlarmSetTime(wakeUp, context)
            return wakeUp
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

    }
}