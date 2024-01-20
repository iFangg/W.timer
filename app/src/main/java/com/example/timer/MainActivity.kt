package com.example.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import com.example.timer.databinding.ActivityMainBinding
import com.example.timer.util.PrefUtil
import java.util.Calendar

class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    companion object {
        fun setAlarm(context: Context, currSec: Long, remainSec: Long): Long {
            val wakeUp = (currSec + remainSec) * 1000
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

            manager.setExact(AlarmManager.RTC_WAKEUP, wakeUp, pendingIntent)

            PrefUtil.setAlarmSetTime(nowSec, context)
            return wakeUp
        }

        fun removeAlarm(context: Context) {
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            // todo: double check flags
            val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            manager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0, context)
        }

        private val nowSec: Long
            get() = Calendar.getInstance().timeInMillis / 1000
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var timerState: TimerState = TimerState.Stopped
    private var secondsRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

//        supportActionBar?.setIcon(R.drawable.ic_timer)
//        supportActionBar?.title = title
        supportActionBar?.let {actionBar ->  
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_timer)
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP)
//            drawable?.colorFilter = BlendModeColorFilter(ContextCompat.getColor(this, R.color.white), BlendMode.SRC_ATOP)
//            requires higher min API level
            actionBar.setIcon(drawable)
        }

        val title = " Timer"
        val spannableString = SpannableString(title)
        val textColour = ContextCompat.getColor(this, R.color.white)
        spannableString.setSpan(
            ForegroundColorSpan(textColour),
            0,
            title.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        supportActionBar?.title = spannableString

        binding.fabPlay.setOnClickListener{_ ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        binding.fabPause.setOnClickListener{_ ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        binding.fabStop.setOnClickListener{_ ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()
        initTimer()

        removeAlarm(this)
        // todo...
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
            val wakeUp = setAlarm(this, nowSec, secondsRemaining)
            // todo...
        } else if (timerState == TimerState.Paused) {
            // todo...
        }

        PrefUtil.setPrevLenSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer() {
        timerState = PrefUtil.getTimerState(this)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining = if (timerState == TimerState.Stopped) timerLengthSeconds else PrefUtil.getSecondsRemaining(this)
        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0) secondsRemaining -= nowSec - alarmSetTime
        if (secondsRemaining <= 0) onTimerFinished()
        else if (timerState == TimerState.Running) startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped
        setNewTimerLength()
//        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()
            override fun onTick(msTilDone: Long) {
                secondsRemaining = msTilDone / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength() {
        val lenMin = PrefUtil.getTimerLen(this)
        timerLengthSeconds = (lenMin * 60L)
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPrevTimerLenSeconds(this)
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun updateCountdownUI() {
        val minTilFin = secondsRemaining / 60
        val secInMinTilFin = secondsRemaining - minTilFin * 60
        val secStr = String.format("%02d", secInMinTilFin)

        val countdownText = getString(R.string.countdown_text, minTilFin, secStr)
        binding.contentMain.textViewCountDown.text = countdownText
    }

    private fun updateButtons() {
        // refactor?
        when (timerState) {
            TimerState.Running -> {
                binding.fabPlay.isEnabled = false
                binding.fabPause.isEnabled = true
                binding.fabStop.isEnabled = true
            }
            TimerState.Paused -> {
                binding.fabPlay.isEnabled = true
                binding.fabPause.isEnabled = false
                binding.fabStop.isEnabled = true
            }
            TimerState.Stopped -> {
                binding.fabPlay.isEnabled = true
                binding.fabPause.isEnabled = false
                binding.fabStop.isEnabled = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}