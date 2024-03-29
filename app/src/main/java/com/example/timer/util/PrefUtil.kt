package com.example.timer.util

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.example.timer.MainActivity
import com.example.timer.Timer

class PrefUtil {
    companion object {
        private const val TIMER_LENGTH_ID = "com.example.timer.timer_length"
        fun getTimerLen(context: Context): Int {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(TIMER_LENGTH_ID, 1);
        }

        fun setTimerLen(context: Context, timerLength: Int) {
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt(TIMER_LENGTH_ID, timerLength)
            editor.apply()
        }

        private const val PREVIOUS_TIMER_LEN_SEC_ID = "com.example.timer.previous_timer_len_id"

        fun getPrevTimerLenSeconds(context: Context): Long {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LEN_SEC_ID, 0)
        }

        fun setPrevLenSeconds(seconds: Long, context: Context) {
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LEN_SEC_ID, seconds)
            editor.apply()
        }

        private const val TIMER_STATE_ID = "com.example.timer.timer_state"

        fun getTimerState(context: Context): Timer.TimerState {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return Timer.TimerState.entries[ordinal]
        }

        fun setTimerState(state: Timer.TimerState, context: Context) {
//            Exception().printStackTrace()
//            println("before: ${getTimerState(context)}")
            if (state == getTimerState(context)) {
//                println("same state, no need to change")
                return
            }
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
//            println("after: ${getTimerState(context)}")
        }

        private const val SECONDS_REMAINING_ID = "com.example.timer.seconds_remaining"

        fun getSecondsRemaining(context: Context): Long {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID, 0)
        }

        fun setSecondsRemaining(seconds: Long, context: Context) {
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID, seconds)
            editor.apply()
        }

        private const val ALARM_SET_TIME_ID = "com.example.timer.backgrounded_time"

        fun getAlarmSetTime(context: Context): Long {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID, 0)
        }

        fun setAlarmSetTime(time: Long, context: Context) {
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID, time)
            editor.apply()
        }
    }
}