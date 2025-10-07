/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.widget.tiles

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.utils.di.ServiceViewEntryPoint
import io.chaldeaprjkt.gamespace.utils.entryPointOf

abstract class BaseTile @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs), View.OnClickListener {
    init {
        isClickable = true
        isFocusable = true
        prepareLayout()
    }

    val appSettings by lazy { context.entryPointOf<ServiceViewEntryPoint>().appSettings() }
    val systemSettings by lazy { context.entryPointOf<ServiceViewEntryPoint>().systemSettings() }

    val title: TextView?
        get() = findViewById(R.id.tile_title)

    val summary: TextView?
        get() = findViewById(R.id.tile_summary)

    val icon: ImageView?
        get() = findViewById(R.id.tile_icon)

    private fun prepareLayout() {
        LayoutInflater.from(context).inflate(R.layout.panel_tile, this, true)
        setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        isSelected = !isSelected
    }
}
