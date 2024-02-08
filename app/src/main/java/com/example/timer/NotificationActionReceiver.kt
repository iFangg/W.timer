package com.example.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import com.example.timer.util.NotifUtil
import com.example.timer.util.PrefUtil

class NotificationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_HIDE, Constants.ACTION_STOP -> {
                TimerController.removeAlarm(context)
                PrefUtil.setTimerState(Timer.TimerState.Stopped, context)
                Timer.onTimerFinished(context)
                if (intent.action == Constants.ACTION_HIDE) NotifUtil.hideTimerNotif(context)
                else NotifUtil.showTimerExpired(context)
            }

            Constants.ACTION_PAUSE -> {
                val secsLeft = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getTimerLen(context) * 60L
                val nowSec = Timer.nowSec

                println("secsLeft: $secsLeft ($nowSec - $alarmSetTime)")
//                secsLeft -= nowSec - alarmSetTime
//                println("secsLeft: $secsLeft ($nowSec - $alarmSetTime)")
                PrefUtil.setSecondsRemaining(secsLeft, context)

//                TimerController.removeAlarm(context)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pauseIntent = Intent(context, TimerExpiredReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                alarmManager.cancel(pendingIntent)

                Timer.pauseTimer(context)
                PrefUtil.setTimerState(Timer.TimerState.Paused, context)
                NotifUtil.showTimerPaused(context)
            }

            Constants.ACTION_RESUME -> {
                // todo update end time of timer to reflect time paused
//                val secsLeft = PrefUtil.getSecondsRemaining(context)
//                val wakeup = TimerController.setAlarm(context, Timer.nowSec, secsLeft)
                val wakeup = PrefUtil.getAlarmSetTime(context)
                Timer.startTimer(context)
                PrefUtil.setTimerState(Timer.TimerState.Running, context)
                NotifUtil.showTimerRunning(context, wakeup)
            }

            Constants.ACTION_START -> {
                val secsLeft = PrefUtil.getTimerLen(context) * 60L
//                println("Seconds left $secsLeft")
                val wakeup = TimerController.setAlarm(context, Timer.nowSec, secsLeft)
                Timer.startTimer(context)
                PrefUtil.setTimerState(Timer.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secsLeft, context)
                NotifUtil.showTimerRunning(context, wakeup)
            }
        }
    }
}