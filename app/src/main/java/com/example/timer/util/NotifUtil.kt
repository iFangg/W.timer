package com.example.timer.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.timer.Constants
import com.example.timer.MainActivity
import com.example.timer.NotificationActionReceiver
import com.example.timer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotifUtil {
    companion object {
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "App timer"
        private const val TIMER_ID = 0
        private const val UPDATE_INTERVAL_MILLIS = 1000L
        private var tickingRunnable: Runnable? = null
//        private lateinit var nBuilder: NotificationCompat.Builder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
//        private lateinit var nManager: NotificationManager

        private fun formatTime(seconds: Long): String {
//            println(seconds)
            val hours = seconds / 3600
            val minutes = seconds / 60
            val secs = seconds % 60
            println("$hours, $minutes, $secs")
            val format = SimpleDateFormat("HH:mm:ss", Locale.US)

            if (hours > 0) return "%02d:%02d:%02d".format(hours, minutes, secs)
            var formattedString = ""
            if (minutes > 0) formattedString += "%02d:".format(minutes)
            formattedString += "%02d".format(secs)
            if (minutes <= 0) formattedString += " seconds"

            return formattedString
        }

        private fun scheduleTickingNotifUpdates(context: Context, nBuilder: NotificationCompat.Builder, nManager: NotificationManager, wakeupTime: Long) {
            val handler = Handler(Looper.getMainLooper())
            tickingRunnable = object : Runnable {
                override fun run() {
                    val secsLeft = PrefUtil.getSecondsRemaining(context)
                    if (secsLeft < 0) {
                        stopTickingNotifs(context)
                        return
                    }

                    val nextSec = secsLeft - UPDATE_INTERVAL_MILLIS / 1000
                    println("secs left ${PrefUtil.getSecondsRemaining(context)}")
                    // Update the notification content
                    nBuilder.setContentText("End: ${SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(Date(wakeupTime))} (in ${formatTime(secsLeft)})")
                    PrefUtil.setSecondsRemaining(nextSec, context)
                    nManager.notify(TIMER_ID, nBuilder.build())

                    // Schedule the next update
                    handler.postDelayed(this, UPDATE_INTERVAL_MILLIS)
                }
            }

            handler.postDelayed(tickingRunnable as Runnable, UPDATE_INTERVAL_MILLIS)
        }

        private fun stopTickingNotifs(context: Context) {
            println("stop!!")
            tickingRunnable?.let {
                Handler(Looper.getMainLooper()).removeCallbacks(it)
                tickingRunnable = null
                showTimerExpired(context)
            }
        }

        private fun resumeTickingNotifs(context: Context, nBuilder: NotificationCompat.Builder, nManager: NotificationManager, wakeupTime: Long) {
            if (tickingRunnable == null)
                scheduleTickingNotifUpdates(context, nBuilder, nManager, wakeupTime)
        }

        fun showTimerRunning(context: Context, wakeupTime: Long) {
            println("running time: $wakeupTime")
            val stopIntent = Intent(context, NotificationActionReceiver::class.java)
            stopIntent.action = Constants.ACTION_STOP
            val pendingStopIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val pauseIntent = Intent(context, NotificationActionReceiver::class.java)
            pauseIntent.action = Constants.ACTION_PAUSE
            val pendingPauseIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is Running")
                .setContentText("End: ${df.format(Date(wakeupTime))} (in ${formatTime(wakeupTime)})")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_pause, "Pause", pendingPauseIntent)
                .addAction(R.drawable.ic_stop, "Stop", pendingStopIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)
//            nManager.notify(TIMER_ID, nBuilder.build())
            nBuilder.setSound(null)

            PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
            scheduleTickingNotifUpdates(context, nBuilder, nManager, wakeupTime)
        }

        fun showTimerExpired(context: Context) {
            val startIntent = Intent(context, NotificationActionReceiver::class.java)
            startIntent.action = Constants.ACTION_START
            val pendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer Expired!")
                .setContentText("Start Again?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_play, "Start", pendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun showTimerPaused(context: Context, wakeupTime: Long){
            val resumeIntent = Intent(context, NotificationActionReceiver::class.java)
            resumeIntent.action = Constants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,
                0, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val stopIntent = Intent(context, NotificationActionReceiver::class.java)
            stopIntent.action = Constants.ACTION_STOP
            val pendingStopIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

//            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is paused.")
                .setContentText("${formatTime(wakeupTime)} remaining, resume?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_play, "Resume", resumePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", pendingStopIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun hideTimerNotif(context: Context) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
            stopTickingNotifs(context)
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notifSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)
            if (playSound) nBuilder.setSound(notifSound)
            return nBuilder
        }

        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent {
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        }

//        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel(channelID: String, channelName: String, playSound: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}