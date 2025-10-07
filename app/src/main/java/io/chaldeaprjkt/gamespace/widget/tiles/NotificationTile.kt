/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022 Nameless-AOSP
 * SPDX-FileCopyrightText: 2023 the risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.chaldeaprjkt.gamespace.R

class NotificationTile @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : BaseTile(context, attrs) {

    private var activeMode = true
        set(value) {
            field = value
            appSettings.danmakuNotification = value
            summary?.text = if (value) {
                systemSettings.headsup = false
                context.getString(R.string.notification_danmaku)
            } else {
                systemSettings.headsup = true
                context.getString(R.string.state_default)
            }
            isSelected = value
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        title?.text = context.getString(R.string.notification_mode_title)
        activeMode = appSettings.danmakuNotification
        icon?.setImageResource(R.drawable.ic_action_heads_up)
    }

    override fun onClick(v: View?) {
        super.onClick(v)
        activeMode = !activeMode
    }
}
