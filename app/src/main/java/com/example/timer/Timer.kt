package com.example.timer

import android.os.CountDownTimer
import com.example.timer.util.PrefUtil
import java.util.Calendar

object Timer {
    private var subscribers = mutableListOf<Subscriber>()

    fun addSubscriber(sub: Subscriber) {
        subscribers.add(sub)
    }

    fun removeSubscriber(sub: Subscriber) {
        subscribers.remove(sub)
    }

    private fun notifySubscribers() {
        subscribers.forEach { it.update() }
    }

    enum class TimerState {
        Stopped, Paused, Running
    }

    val nowSec: Long
        get() = Calendar.getInstance().timeInMillis

    lateinit var timer: CountDownTimer
    private var inApp: Boolean = true
    private val timerContext: MainActivity = MainActivity()
    private var timerLengthSeconds: Long = 0
    private var timerState: TimerState = TimerState.Stopped
    private var secondsRemaining: Long = 0

    fun getInAppStatus(): Boolean {
        return inApp
    }

    fun setInAppStatus(boolean: Boolean) {
        inApp = boolean
    }

    fun getTimerLenSeconds(): Long {
        return timerLengthSeconds
    }

    fun setTimerLenSeconds(length: Long) {
        timerLengthSeconds = length
    }

    fun getSecondsRemaining(): Long {
        return secondsRemaining
    }

    fun setSecondsRemaining(sec: Long) {
        secondsRemaining = sec
    }

    private fun setNewTimerLength() {
        val lenMin = PrefUtil.getTimerLen(timerContext)
        timerLengthSeconds = (lenMin * 60L)
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPrevTimerLenSeconds(timerContext)
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    fun initTimer() {
        timerState = PrefUtil.getTimerState(MainActivity())
        if (timerState == TimerState.Stopped) {
            setNewTimerLength()
        } else {
            setPreviousTimerLength()
        }

        secondsRemaining = if (timerState == TimerState.Stopped) timerLengthSeconds else PrefUtil.getSecondsRemaining(timerContext)
        val alarmSetTime = PrefUtil.getAlarmSetTime(timerContext)
        if (alarmSetTime > 0) secondsRemaining -= nowSec - alarmSetTime
        if (secondsRemaining <= 0) onTimerFinished()
        else if (timerState == TimerState.Running) startTimer()

        notifySubscribers()
//        updateButtons()
//        updateCountdownUI()
    }


    fun onTimerFinished() {
//        println("I was stopped")
        stopTimer()
        timerState = TimerState.Stopped
        setNewTimerLength()
//        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaining(timerLengthSeconds, timerContext)
        secondsRemaining = timerLengthSeconds

        notifySubscribers()
//        updateCountdownUI()
//        updateButtons()
    }

    fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish() = onTimerFinished()
            override fun onTick(msTilDone: Long) {
                secondsRemaining = msTilDone / 1000
                notifySubscribers()
//                updateCountdownUI()
            }
        }.start()
    }

    fun stopTimer() {
        timer.cancel()
    }
}