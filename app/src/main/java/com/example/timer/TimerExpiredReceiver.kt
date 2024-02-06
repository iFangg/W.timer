package com.example.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timer.util.NotifUtil
import com.example.timer.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Exception().printStackTrace()
        NotifUtil.showTimerExpired(context)

        Timer.onTimerFinished(context)
    }
}