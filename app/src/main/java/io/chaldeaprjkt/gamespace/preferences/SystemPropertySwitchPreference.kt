/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences

import android.content.Context
import android.os.SystemProperties
import android.util.AttributeSet
import androidx.preference.PreferenceManager
import lineageos.preference.SelfRemovingSwitchPreference

class SystemPropertySwitchPreference : SelfRemovingSwitchPreference {

    private val db by lazy { PreferenceManager.getDefaultSharedPreferences(context) }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int,
    ) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context, null)

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        super.onSetInitialValue(restorePersistedValue, defaultValue)

        if (isEnabled()) {
            val key = key
            val value = getBoolean(key, defaultValue as? Boolean ?: false)
            SystemProperties.set(key, value.toString())
        } else {
            SystemProperties.set(key, "false")
        }
    }

    override fun isPersisted(): Boolean {
        return !SystemProperties.get(key, "").isEmpty()
    }

    override fun putBoolean(key: String, value: Boolean) {
        db.edit().putBoolean(key, value).commit()
        SystemProperties.set(key, value.toString())
        SystemPropPoker.getInstance().poke()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return db.getBoolean(key, defaultValue)
    }
}
