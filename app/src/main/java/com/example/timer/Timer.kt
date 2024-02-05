package com.example.timer

import android.content.Context
import android.os.CountDownTimer
import com.example.timer.util.PrefUtil
import java.lang.ref.WeakReference
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

    private var inApp: Boolean = true
//    private var timerContext: WeakReference<Context>? = null
    private var timerLengthSeconds: Long = 0
    private var timerState: TimerState = TimerState.Stopped
    private var secondsRemaining: Long = 0
    lateinit var timer: CountDownTimer
//    private var timer: CountDownTimer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
//        override fun onTick(msTilDone: Long) {
//            secondsRemaining = msTilDone / 1000
//            notifySubscribers()
////                updateCountdownUI()
//        }
//
//        override fun onFinish() = onTimerFinished()
//    }

    fun getInAppStatus(): Boolean {
        return inApp
    }

    fun setInAppStatus(boolean: Boolean) {
        inApp = boolean
    }

//    fun getContext(): Context? {
//        return timerContext?.get()
//    }
//
//    fun setContext(context: Context) {
//        timerContext = WeakReference(context.applicationContext)
//    }

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

    private fun setNewTimerLength(context: Context) {
        val lenMin = PrefUtil.getTimerLen(context)
        println("lenMin: $lenMin")
        timerLengthSeconds = (lenMin * 60L)

//        val context: Context? = timerContext?.get()
//        val lenMin = context?.let { PrefUtil.getTimerLen(it) }
//        if (lenMin != null) {
//            println("lenMin: $lenMin")
//            timerLengthSeconds = (lenMin * 60L)
//        }
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength(context: Context) {
        timerLengthSeconds = PrefUtil.getPrevTimerLenSeconds(context)
//        timerLengthSeconds = timerContext?.get()?.let { PrefUtil.getPrevTimerLenSeconds(it) }!!
//        progress_countdown.max = timerLengthSeconds.toInt()
    }

    fun initTimer(context: Context) {
        println("hello")
//        val context: Context = timerContext?.get() ?: return
        println("starting state is: $timerState")
        PrefUtil.setTimerState(timerState, context)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength(context)
        } else {
            setPreviousTimerLength(context)
        }

        secondsRemaining = if (timerState == TimerState.Stopped) timerLengthSeconds else PrefUtil.getSecondsRemaining(context)
        println("seconds remaining: $secondsRemaining")
//        val alarmSetTime = timerContext?.get()?.let { PrefUtil.getAlarmSetTime(it) }!!
        val alarmSetTime = PrefUtil.getAlarmSetTime(context)
        if (alarmSetTime > 0) secondsRemaining -= nowSec - alarmSetTime
        if (secondsRemaining <= 0) onTimerFinished(context)
        else if (timerState == TimerState.Running) startTimer(context)

        notifySubscribers()
//        updateButtons()
//        updateCountdownUI()
    }


    fun startTimer(context: Context) {
        timerState = TimerState.Running
        PrefUtil.setTimerState(timerState, context)
//        val context: Context? = timerContext?.get()
//        if (context != null) PrefUtil.setTimerState(timerState, context)

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(msTilDone: Long) {
                secondsRemaining = msTilDone / 1000
                PrefUtil.setSecondsRemaining(secondsRemaining, context)

                notifySubscribers()
//                updateCountdownUI()
            }
            override fun onFinish() = onTimerFinished(context)
        }.start()
    }

    fun pauseTimer(context: Context) {
        timer.cancel()
        PrefUtil.setTimerState(TimerState.Paused, context)

        notifySubscribers()
    }

    fun onTimerFinished(context: Context) {
//        println("I was stopped")
        timer.cancel()
        setNewTimerLength(context)
//        progress_countdown.progress = 0

        PrefUtil.setTimerState(TimerState.Stopped, context)
        PrefUtil.setSecondsRemaining(timerLengthSeconds, context)
        secondsRemaining = timerLengthSeconds

        notifySubscribers()
//        val context: Context? = timerContext?.get()
//        if (context != null) {
//            PrefUtil.setTimerState(timerState, context)
//            PrefUtil.setSecondsRemaining(timerLengthSeconds, context)
//            secondsRemaining = timerLengthSeconds
//
//            notifySubscribers()
//        }
//        updateCountdownUI()
//        updateButtons()
    }
}