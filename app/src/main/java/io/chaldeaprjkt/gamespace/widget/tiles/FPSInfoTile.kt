/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.chaldeaprjkt.gamespace.R

class FPSInfoTile @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    BaseTile(context, attrs) {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showFpsInfo = appSettings.showFps
        title?.text = context.getString(R.string.fps_Info_title)
        icon?.setImageResource(R.drawable.ic_fps)
    }

    private var showFpsInfo = false
        set(value) {
            field = value
            if (value) {
                summary?.text = context.getString(R.string.state_enabled)
            } else {
                summary?.text = context.getString(R.string.state_disabled)
            }
            appSettings.showFps = value
            isSelected = value
        }

    override fun onClick(v: View?) {
        super.onClick(v)
        showFpsInfo = !showFpsInfo
    }
}
