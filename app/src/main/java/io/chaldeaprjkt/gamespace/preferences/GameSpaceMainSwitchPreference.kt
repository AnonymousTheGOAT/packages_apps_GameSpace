/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences

import android.content.Context
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.System.GAMESPACE_GAME_LIST
import android.util.AttributeSet
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.MainSwitchPreference

class GameSpaceMainSwitchPreference : MainSwitchPreference {

    private val db by lazy { PreferenceManager.getDefaultSharedPreferences(context) }
    private val resolver = context.contentResolver

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int,
    ) : super(context, attrs, defStyle) {
        setPreferenceDataStore(DataStore())
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setPreferenceDataStore(DataStore())
    }

    constructor(context: Context) : super(context, null) {
        setPreferenceDataStore(DataStore())
    }

    private inner class DataStore : PreferenceDataStore() {
        fun reset() {
            Settings.System.putStringForUser(
                resolver,
                GAMESPACE_GAME_LIST,
                "",
                UserHandle.USER_CURRENT,
            )
        }

        fun save() {
            val userGames =
                Settings.System.getStringForUser(
                    resolver,
                    GAMESPACE_GAME_LIST,
                    UserHandle.USER_CURRENT,
                )
            db.edit().putString(GAMESPACE_GAME_LIST, userGames).apply()
        }

        fun restore() {
            val userGames = db.getString(GAMESPACE_GAME_LIST, "")
            Settings.System.putStringForUser(
                resolver,
                GAMESPACE_GAME_LIST,
                userGames,
                UserHandle.USER_CURRENT,
            )
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return db.getBoolean(KEY_ENABLE, true)
        }

        override fun putBoolean(key: String, value: Boolean) {
            db.edit().putBoolean(KEY_ENABLE, value).commit()

            if (value) {
                restore()
                return
            }

            save()
            reset()
        }
    }

    companion object {
        private const val KEY_ENABLE = "gamespace_enable"
    }
}
