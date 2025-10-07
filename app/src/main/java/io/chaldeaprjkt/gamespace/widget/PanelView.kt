/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.utils.di.ServiceViewEntryPoint
import io.chaldeaprjkt.gamespace.utils.entryPointOf

class PanelView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs) {

    private val appSettings by lazy { context.entryPointOf<ServiceViewEntryPoint>().appSettings() }

    init {
        LayoutInflater.from(context).inflate(R.layout.panel_view, this, true)
        isClickable = true
        isFocusable = true
    }

    fun updateTranslationY() {
        val targetMargin = appSettings.y
        val params = layoutParams as ViewGroup.MarginLayoutParams
        val animator = ValueAnimator.ofInt(params.topMargin, targetMargin)
        animator.duration = 300L
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            params.topMargin = valueAnimator.animatedValue as Int
            layoutParams = params
        }
        animator.start()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateTranslationY()
    }
}
