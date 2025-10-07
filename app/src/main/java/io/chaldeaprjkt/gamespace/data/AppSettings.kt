/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022-2024 crDroid Android Project
 * SPDX-FileCopyrightText: 2023 risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.app.Service
import android.content.Context
import android.view.WindowManager
import androidx.preference.PreferenceManager
import io.chaldeaprjkt.gamespace.utils.dp
import io.chaldeaprjkt.gamespace.utils.statusbarHeight
import javax.inject.Inject

class AppSettings @Inject constructor(private val context: Context) {

    private val db by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val wm by lazy { context.getSystemService(Service.WINDOW_SERVICE) as WindowManager }

    var enabled
        get() = db.getBoolean(KEY_ENABLE, true)
        set(it) = db.edit().putBoolean(KEY_ENABLE, it).apply()

    var x
        get() = db.getInt("offset_x", -wm.maximumWindowMetrics.bounds.width() / 2)
        set(point) = db.edit().putInt("offset_x", point).apply()

    var y
        get() = db.getInt("offset_y", context.statusbarHeight + 8.dp)
        set(point) = db.edit().putInt("offset_y", point).apply()

    var showFps
        get() = db.getBoolean("show_fps", false)
        set(point) = db.edit().putBoolean("show_fps", point).apply()

    var noAutoBrightness
        get() = db.getBoolean(KEY_AUTO_BRIGHTNESS_DISABLE, true)
        set(it) = db.edit().putBoolean(KEY_AUTO_BRIGHTNESS_DISABLE, it).apply()

    var noThreeScreenshot
        get() = db.getBoolean(KEY_3SCREENSHOT_DISABLE, false)
        set(it) = db.edit().putBoolean(KEY_3SCREENSHOT_DISABLE, it).apply()

    var stayAwake
        get() = db.getBoolean(KEY_STAY_AWAKE, false)
        set(value) = db.edit().putBoolean(KEY_STAY_AWAKE, value).apply()

    var danmakuNotification
        get() = db.getBoolean(KEY_DANMAKU_NOTIFICATION_MODE, false)
        set(value) = db.edit().putBoolean(KEY_DANMAKU_NOTIFICATION_MODE, value).apply()

    var callsMode: Int
        get() = db.getString(KEY_CALLS_MODE, "0")?.toIntOrNull() ?: 0
        set(value) = db.edit().putString(KEY_CALLS_MODE, value.toString()).apply()

    var ringerMode: Int
        get() = db.getString(KEY_RINGER_MODE, "3")?.toIntOrNull() ?: 3
        set(value) = db.edit().putString(KEY_RINGER_MODE, value.toString()).apply()

    var menuOpacity: Int
        get() = db.getInt(KEY_MENU_OPACITY, 100)
        set(value) = db.edit().putInt(KEY_MENU_OPACITY, value).apply()

    var lockGesture
        get() = db.getBoolean(KEY_LOCK_GESTURE, false)
        set(value) = db.edit().putBoolean(KEY_LOCK_GESTURE, value).apply()

    companion object {
        const val KEY_ENABLE = "gamespace_enable"
        const val KEY_AUTO_BRIGHTNESS_DISABLE = "gamespace_auto_brightness_disabled"
        const val KEY_3SCREENSHOT_DISABLE = "gamespace_tfgesture_disabled"
        const val KEY_STAY_AWAKE = "gamespace_stay_awake"
        const val KEY_DANMAKU_NOTIFICATION_MODE = "gamespace_danmaku_notification_mode"
        const val KEY_CALLS_MODE = "gamespace_calls_mode"
        const val KEY_RINGER_MODE = "gamespace_ringer_mode"
        const val KEY_MENU_OPACITY = "gamespace_menu_opacity"
        const val KEY_LOCK_GESTURE = "gamespace_lock_gesture"
    }
}
