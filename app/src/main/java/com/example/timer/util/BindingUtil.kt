package com.example.timer.util

import android.view.LayoutInflater
import com.example.timer.databinding.ActivityMainBinding
import com.example.timer.databinding.ContentMainBinding
import com.example.timer.databinding.FragmentFirstBinding
import com.example.timer.databinding.FragmentSecondBinding

object BindingUtil {
    fun inflateMainBinding(inflater: LayoutInflater) = ActivityMainBinding.inflate((inflater))
    fun inflateContentBinding(inflater: LayoutInflater) = ContentMainBinding.inflate((inflater))
    fun inflateFragment1Binding(inflater: LayoutInflater) = FragmentFirstBinding.inflate((inflater))
    fun inflateFragment2Binding(inflater: LayoutInflater) = FragmentSecondBinding.inflate((inflater))
}