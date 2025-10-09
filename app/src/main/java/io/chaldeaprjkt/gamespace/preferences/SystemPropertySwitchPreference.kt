/*
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences

import android.content.Context
import android.os.SystemProperties
import android.util.AttributeSet
import lineageos.preference.SelfRemovingSwitchPreference

class SystemPropertySwitchPreference : SelfRemovingSwitchPreference {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int,
    ) : super(context, attrs, defStyle)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context) : super(context, null)

    override fun isPersisted(): Boolean {
        return !SystemProperties.get(getKey(), "").isEmpty()
    }

    override fun putBoolean(key: String, value: Boolean) {
        SystemProperties.set(key, value.toString())
        SystemPropPoker.getInstance().poke()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return SystemProperties.getBoolean(key, defaultValue)
    }
}
