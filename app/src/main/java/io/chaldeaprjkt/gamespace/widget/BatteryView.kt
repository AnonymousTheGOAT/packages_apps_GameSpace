/*
 * SPDX-FileCopyrightText: 2020 The exTHmUI Open Source Project
 * SPDX-FileCopyrightText: 2021 AOSP-Krypton Project
 * SPDX-FileCopyrightText: 2022-2024 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.AttributeSet
import android.widget.TextView

import io.chaldeaprjkt.gamespace.R

@SuppressLint("AppCompatCustomView")
class BatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : TextView(context, attrs, defStyleAttr, defStyleRes) {

    private val batteryChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                val percent = (level.toFloat() / scale * 100).toInt()
                text = context.getString(R.string.battery_format, percent)
            }
        }
    }

    init {
        val batteryManager = context.getSystemService(BatteryManager::class.java)!!
        text = context.getString(
            R.string.battery_format,
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.registerReceiver(
            batteryChangeReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun onDetachedFromWindow() {
        context.unregisterReceiver(batteryChangeReceiver)
        super.onDetachedFromWindow()
    }
}
