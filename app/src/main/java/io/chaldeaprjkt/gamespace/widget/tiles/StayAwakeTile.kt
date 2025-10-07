/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.utils.di.ServiceViewEntryPoint
import io.chaldeaprjkt.gamespace.utils.entryPointOf

class StayAwakeTile @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseTile(context, attrs) {

    private val screenUtils by lazy { context.entryPointOf<ServiceViewEntryPoint>().screenUtils() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        shouldStayAwake = appSettings.stayAwake
        title?.text = context.getString(R.string.stay_awake_title)
        icon?.setImageResource(R.drawable.ic_awake)
    }

    private var shouldStayAwake: Boolean = false
        set(value) {
            field = value
            if (value) {
                summary?.text = context.getString(R.string.state_enabled)
            } else {
                summary?.text = context.getString(R.string.state_disabled)
            }
            appSettings.stayAwake = value
            isSelected = value
            screenUtils.stayAwake = value
        }

    override fun onClick(v: View?) {
        super.onClick(v)
        shouldStayAwake = !shouldStayAwake
    }
}
