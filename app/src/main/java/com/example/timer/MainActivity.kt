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
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import com.example.timer.databinding.ActivityMainBinding
import com.example.timer.util.NotifUtil
import com.example.timer.util.PrefUtil
import java.util.Calendar

class MainActivity : AppCompatActivity(), Subscriber {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

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
//           ^^^ Requires higher min API level ^^^
            actionBar.setIcon(drawable)
        }

        Timer.addSubscriber(this)
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
            PrefUtil.setTimerState(Timer.TimerState.Running, this)
            Timer.startTimer(this)
        }

        binding.fabPause.setOnClickListener{_ ->
            PrefUtil.setTimerState(Timer.TimerState.Paused, this)
            Timer.pauseTimer(this)
        }

        binding.fabStop.setOnClickListener{_ ->
            Timer.onTimerFinished(this)
        }
    }

    override fun onResume() {
        super.onResume()
        Timer.setInAppStatus(true)
        if (!Timer.exists()) Timer.initTimer(this)
        if (PrefUtil.getTimerState(this) == Timer.TimerState.Stopped) {
            TimerController.removeAlarm(this)
        }

        NotifUtil.hideTimerNotif(this)
    }

    override fun onPause() {
        super.onPause()
        Timer.setInAppStatus(false)
        val secondsRemaining = PrefUtil.getSecondsRemaining(this)
        val wakeUp = (Timer.nowSec + secondsRemaining * 1000)
        PrefUtil.setAlarmSetTime(wakeUp, this)
        Timer.setFinishTime(wakeUp)
        println("seconds remaining: $secondsRemaining")

        val timerState = PrefUtil.getTimerState(this)
        if (timerState == Timer.TimerState.Running) {
            NotifUtil.showTimerRunning(this, wakeUp)
        } else if (timerState == Timer.TimerState.Paused) {
            NotifUtil.showTimerPaused(this)
        }

//        PrefUtil.setPrevLenSeconds(Timer.getTimerLenSeconds(), this)
//        PrefUtil.setSecondsRemaining(secondsRemaining, this)
//        PrefUtil.setTimerState(timerState, this)
    }

    override fun update() {
        updateCountdownUI()
        updateButtons()
    }

    private fun updateCountdownUI() {
        val secondsRemaining = Timer.getSecondsRemaining()
        val minTilFin = secondsRemaining / 60
        val secInMinTilFin = secondsRemaining - minTilFin * 60
        val secStr = String.format("%02d", secInMinTilFin)

        val countdownText = getString(R.string.countdown_text, minTilFin, secStr)
        binding.contentMain.textViewCountDown.text = countdownText
        // todo add for notifications, maybe have current state as a variable here and change behaviour depending on state?
        if (!Timer.getInAppStatus()) {
            println("expired? $secondsRemaining s left")
            if (PrefUtil.getSecondsRemaining(this) <= 0) NotifUtil.showTimerExpired(this) else NotifUtil.showTimerRunning(this, Timer.getFinishTime())
        }
    }

    private fun updateButtons() {
//        println("state: ${PrefUtil.getTimerState(this)}")
        // refactor?
        when (PrefUtil.getTimerState(this)) {
            Timer.TimerState.Running -> {
                binding.fabPlay.isEnabled = false
                binding.fabPause.isEnabled = true
                binding.fabStop.isEnabled = true
            }
            Timer.TimerState.Paused -> {
                binding.fabPlay.isEnabled = true
                binding.fabPause.isEnabled = false
                binding.fabStop.isEnabled = true
            }
            else -> {
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