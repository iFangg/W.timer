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
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        const val TIMER_NOTIF_ID = 0

//        val countDownTimer = object : CountDownTimer()

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

        fun showTimerRunning(context: Context, wakeupTime: Long) {
            println("running time: $wakeupTime")
            val stopIntent = Intent(context, NotificationActionReceiver::class.java)
            stopIntent.action = Constants.ACTION_STOP
            val pendingStopIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val pauseIntent = Intent(context, NotificationActionReceiver::class.java)
            pauseIntent.action = Constants.ACTION_PAUSE
            val pendingPauseIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
            val nBuilder = getBasicNotificationBuilder(context, false)
            nBuilder.setContentTitle("Timer is Running")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_pause, "Pause", pendingPauseIntent)
                .addAction(R.drawable.ic_stop, "Stop", pendingStopIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            val countDownTimer = object : CountDownTimer((wakeupTime + PrefUtil.getSecondsRemaining(context)) * 1000, 1000) {
                private var started = true
                override fun onTick(secondsRemaining: Long) {
                    println("$wakeupTime")
                    println("${secondsRemaining}s remaining")
                    val state = PrefUtil.getTimerState(context)
                    println("state is: $state")
                    if (state != MainActivity.TimerState.Running) {
                        started = false
                        when (state) {
                            MainActivity.TimerState.Paused -> {
                                pauseTimer()
                                return
                            }

                            else -> {
                                onFinish()
                                return
                            }
                        }
                    } else {
                        if (!started) {
                            started = true
                            resumeTimer()
                        }
                        // Update the countdown in the notification text
                        nBuilder.setContentText("End: ${df.format(Date(wakeupTime))} (in ${formatTime(secondsRemaining)})")
                        PrefUtil.setSecondsRemaining(secondsRemaining, context)
    //                    NotificationManagerCompat.notify()
                        nManager.notify(TIMER_NOTIF_ID, nBuilder.build())
                    }
                }

                override fun onFinish() {
                    Exception().printStackTrace()
                    cancel()
                    showTimerExpired(context)
                }

                fun pauseTimer() {
                    cancel()
                    showTimerPaused(context, PrefUtil.getSecondsRemaining(context))
                }

                fun resumeTimer() {
                    started = true
                    showTimerRunning(context, wakeupTime)
                }
            }

            PrefUtil.setTimerState(MainActivity.TimerState.Running, context)
//            nManager.notify(TIMER_NOTIF_ID, nBuilder.build())
            countDownTimer.start()
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
            val nBuilder = getBasicNotificationBuilder(context, false)
            nBuilder.setContentTitle("Timer is paused.")
                .setContentText("${formatTime(wakeupTime)} remaining, resume?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_play, "Resume", resumePendingIntent)
                .addAction(R.drawable.ic_stop, "Stop", pendingStopIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            nManager.notify(TIMER_NOTIF_ID, nBuilder.build())
        }

        fun showTimerExpired(context: Context) {
            val startIntent = Intent(context, NotificationActionReceiver::class.java)
            startIntent.action = Constants.ACTION_START
            val pendingIntent = PendingIntent.getBroadcast(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val hideIntent = Intent(context, NotificationActionReceiver::class.java)
            hideIntent.action = Constants.ACTION_HIDE
            val pendingHideIntent = PendingIntent.getBroadcast(context, 0, hideIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            val nBuilder = getBasicNotificationBuilder(context, true)
            nBuilder.setContentTitle("Timer Expired!")
                .setContentText("Start Again?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_play, "Start", pendingIntent)
                .addAction(R.drawable.ic_cross, "Hide", pendingHideIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER, true)

            nManager.notify(TIMER_NOTIF_ID, nBuilder.build())
        }

        fun hideTimerNotif(context: Context) {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_NOTIF_ID)
        }

        private fun getBasicNotificationBuilder(context: Context, playSound: Boolean): NotificationCompat.Builder {
            val notifSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val fullscreenIntent = Intent(context, NotificationActionReceiver::class.java)
            val fullscreenPendingIntent = PendingIntent.getActivity(context, 0, fullscreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            val nBuilder = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(0)
                .setFullScreenIntent(fullscreenPendingIntent, true)
                .setOnlyAlertOnce(true)
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