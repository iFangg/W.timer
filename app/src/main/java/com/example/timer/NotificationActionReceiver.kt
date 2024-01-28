package com.example.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timer.util.NotifUtil
import com.example.timer.util.PrefUtil

class NotificationActionReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Constants.ACTION_STOP -> {
                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
                NotifUtil.hideTimerNotif(context)
            }

            Constants.ACTION_PAUSE -> {
                var secsLeft = PrefUtil.getSecondsRemaining(context)
                val alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSec = MainActivity.nowSec

                secsLeft -= nowSec - alarmSetTime
                PrefUtil.setSecondsRemaining(secsLeft, context)

                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Paused, context)
                NotifUtil.showTimerPaused(context, secsLeft)
            }

            Constants.ACTION_RESUME -> {
                val secsLeft = PrefUtil.getSecondsRemaining(context)
                val wakeup = MainActivity.setAlarm(context, MainActivity.nowSec, secsLeft)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                NotifUtil.showTimerRunning(context, wakeup)
            }

            Constants.ACTION_START -> {
                val secsLeft = PrefUtil.getTimerLen(context) * 60L
                val wakeup = MainActivity.setAlarm(context, MainActivity.nowSec, secsLeft)
                PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
                PrefUtil.setSecondsRemaining(secsLeft, context)
                NotifUtil.showTimerRunning(context, wakeup)
            }
        }
    }
}