/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-FileCopyrightText: 2023 risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.content.Context
import android.media.AudioManager
import com.google.gson.Gson
import javax.inject.Inject

class GameSession @Inject constructor(
    private val context: Context,
    private val appSettings: AppSettings,
    private val systemSettings: SystemSettings,
    private val gson: Gson,
) {

    private val db by lazy { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    private var state
        get() = db.getString(KEY_SAVED_SESSION, "")
            .takeIf { !it.isNullOrEmpty() }
            ?.let {
                try {
                    gson.fromJson(it, SessionState::class.java)
                } catch (e: RuntimeException) {
                    null
                }
            }
        set(value) = db.edit()
            .putString(KEY_SAVED_SESSION, value?.let {
                try {
                    gson.toJson(value)
                } catch (e: RuntimeException) {
                    ""
                }
            } ?: "")
            .apply()

    fun register(sessionName: String) {
        if (state?.packageName != sessionName) unregister()

        state = SessionState(
            packageName = sessionName,
            autoBrightness = systemSettings.autoBrightness,
            headsup = systemSettings.headsup,
            threeScreenshot = systemSettings.threeScreenshot,
            ringerMode = audioManager.ringerModeInternal,
        )
        if (appSettings.noAutoBrightness) {
            systemSettings.autoBrightness = false
        }
        if (appSettings.danmakuNotification) {
            systemSettings.headsup = false
        }
        if (appSettings.noThreeScreenshot) {
            systemSettings.threeScreenshot = 0
        }
        if (appSettings.ringerMode != 3) {
            audioManager.ringerModeInternal = appSettings.ringerMode
        }
    }

    fun unregister() {
        val orig = state?.copy() ?: return
        if (appSettings.noAutoBrightness) {
            orig.autoBrightness?.let { systemSettings.autoBrightness = it }
        }
        if (appSettings.danmakuNotification) {
            orig.headsup?.let { systemSettings.headsup = it }
        }
        if (appSettings.noThreeScreenshot) {
            systemSettings.threeScreenshot = orig.threeScreenshot
        }
        if (appSettings.ringerMode != 3) {
            audioManager.ringerModeInternal = orig.ringerMode
        }
        state = null
    }

    fun finalize() {
        unregister()
    }

    companion object {
        const val PREFS_NAME = "persisted_session"
        const val KEY_SAVED_SESSION = "session"
    }
}
