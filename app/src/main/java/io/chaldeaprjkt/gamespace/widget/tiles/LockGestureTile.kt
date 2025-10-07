/*
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.utils.di.ServiceViewEntryPoint
import io.chaldeaprjkt.gamespace.utils.entryPointOf

class LockGestureTile @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseTile(context, attrs) {

    private val screenUtils by lazy { context.entryPointOf<ServiceViewEntryPoint>().screenUtils() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        shouldLockGesture = appSettings.lockGesture
        title?.text = context.getString(R.string.lock_gesture_title)
        icon?.setImageResource(R.drawable.ic_gesture)
    }

    private var shouldLockGesture: Boolean = false
        set(value) {
            field = value
            if (value) {
                summary?.text = context.getString(R.string.state_enabled)
            } else {
                summary?.text = context.getString(R.string.state_disabled)
            }
            appSettings.lockGesture = value
            isSelected = value
            screenUtils.lockGesture = value
        }

    override fun onClick(v: View?) {
        super.onClick(v)
        shouldLockGesture = !shouldLockGesture
    }
}
