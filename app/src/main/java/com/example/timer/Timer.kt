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
    private var finishTime: Long = 0
    lateinit var timer: CountDownTimer
    private var timerExists = false

    fun getInAppStatus(): Boolean {
        return inApp
    }

    fun setInAppStatus(boolean: Boolean) {
        inApp = boolean
    }

    fun getTimerLenSeconds(): Long {
        return timerLengthSeconds
    }

    fun getSecondsRemaining(): Long {
        return secondsRemaining
    }

    fun setSecondsRemaining(sec: Long) {
        secondsRemaining = sec
    }

    fun getFinishTime(): Long {
        return finishTime
    }

    fun setFinishTime(num: Long) {
        finishTime = num
    }

    private fun setNewTimerLength(context: Context) {
        val lenMin = PrefUtil.getTimerLen(context)
        println("lenMin: $lenMin")
//        if (!inApp) {
//            Exception().printStackTrace()
//        }
        timerLengthSeconds = (lenMin * 60L)
    }

    private fun setPreviousTimerLength(context: Context) {
        timerLengthSeconds = PrefUtil.getPrevTimerLenSeconds(context)
    }

    fun exists(): Boolean {
        return timerExists
    }

    fun initTimer(context: Context) {
        println("starting state is: $timerState, ${PrefUtil.getAlarmSetTime(context)}")
        PrefUtil.setTimerState(timerState, context)
        if (timerState == TimerState.Stopped) {
            setNewTimerLength(context)
        }
//        else {
//            setPreviousTimerLength(context)
//        }

        secondsRemaining = if (timerState == TimerState.Stopped) timerLengthSeconds else PrefUtil.getSecondsRemaining(context)
        val wakeup = secondsRemaining * 1000 + nowSec
        println("seconds remaining: $secondsRemaining, wakeup: $wakeup (secR + $nowSec)")
        PrefUtil.setAlarmSetTime(wakeup, context)
        setFinishTime(wakeup)
//        TimerController.setAlarm(context, 0, secondsRemaining)
        println("result alarm time is ${PrefUtil.getAlarmSetTime(context)}")


        if (secondsRemaining <= 0) {
//            println("timer is up?")
            onTimerFinished(context)
        }
        else if (timerState == TimerState.Running && !timerExists) startTimer(context)

        notifySubscribers()
    }

    fun startTimer(context: Context) {
        timerState = TimerState.Running
        PrefUtil.setTimerState(timerState, context)
//        println("seconds left: ${PrefUtil.getSecondsRemaining(context)} - ${getSecondsRemaining()}")

        println("new timer created")
        timerExists = true
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onTick(msTilDone: Long) {
                secondsRemaining = msTilDone / 1000
                println("millisInFuture: ${secondsRemaining * 1000}, seconds remaining: ${PrefUtil.getSecondsRemaining(context)}")
                PrefUtil.setSecondsRemaining(secondsRemaining, context)
                setSecondsRemaining(secondsRemaining)

                notifySubscribers()
//                updateCountdownUI()
            }
            override fun onFinish() {
                println("timer has finished?!")
                onTimerFinished(context)
            }
        }

        timer.start()
    }

    fun pauseTimer(context: Context) {
        timer.cancel()
        PrefUtil.setTimerState(TimerState.Paused, context)

        notifySubscribers()
    }

    // aka stopTimer
    fun onTimerFinished(context: Context) {
        Exception().printStackTrace()
        println("I was stopped")
        timer.cancel()
        setNewTimerLength(context)

        timerState = TimerState.Stopped
        PrefUtil.setTimerState(TimerState.Stopped, context)
        PrefUtil.setSecondsRemaining(timerLengthSeconds, context)
        secondsRemaining = timerLengthSeconds

        notifySubscribers()
    }
}