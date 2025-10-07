/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import io.chaldeaprjkt.gamespace.utils.GameModeUtils
import javax.inject.Inject

import lineageos.providers.LineageSettings

class SystemSettings @Inject constructor(
    context: Context,
    private val gameModeUtils: GameModeUtils
) {

    private val resolver = context.contentResolver

    var headsup
        get() = Settings.Global.getInt(
            resolver, Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 1) == 1
        set(it) {
            Settings.Global.putInt(
                resolver, Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED,
                it.toInt()
            )
        }

    var autoBrightness
        get() =
            Settings.System.getIntForUser(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC,
                UserHandle.USER_CURRENT
            ) ==
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        set(auto) {
            Settings.System.putIntForUser(
                resolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                if (auto) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
                UserHandle.USER_CURRENT
            )
        }

    var threeScreenshot
        get() = LineageSettings.System.getIntForUser(
            resolver, LineageSettings.System.KEY_THREE_FINGERS_SWIPE_ACTION, 0,
            UserHandle.USER_CURRENT
        )
        set(value) {
            LineageSettings.System.putIntForUser(
                resolver, LineageSettings.System.KEY_THREE_FINGERS_SWIPE_ACTION,
                value, UserHandle.USER_CURRENT
            )
        }

    var userGames
        get() =
            Settings.System.getStringForUser(
                resolver, Settings.System.GAMESPACE_GAME_LIST,
                UserHandle.USER_CURRENT
            )
                ?.split(";")
                ?.toList()?.filter { it.isNotEmpty() }
                ?.map { UserGame.fromSettings(it) } ?: emptyList()
        set(games) {
            Settings.System.putStringForUser(
                resolver,
                Settings.System.GAMESPACE_GAME_LIST,
                if (games.isEmpty()) "" else
                    games.joinToString(";") { it.toString() },
                UserHandle.USER_CURRENT
            )
            gameModeUtils.setupBatteryMode(games.isNotEmpty())
        }

    private fun Boolean.toInt() = if (this) 1 else 0
}
