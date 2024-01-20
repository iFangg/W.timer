package com.example.timer.util

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.timer.MainActivity

class PrefUtil {
    companion object {
        private const val TIMER_LENGTH_ID = "com.example.timer.timer_length"
        fun getTimerLen(context: Context): Int {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(TIMER_LENGTH_ID, 1);
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

        fun getTimerState(context: Context): MainActivity.TimerState {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID, 0)
            return MainActivity.TimerState.entries[ordinal]
        }

        fun setTimerState(state: MainActivity.TimerState, context: Context) {
            val editor = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID, ordinal)
            editor.apply()
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