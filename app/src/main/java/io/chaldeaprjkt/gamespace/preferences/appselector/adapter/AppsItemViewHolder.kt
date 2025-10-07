/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences.appselector.adapter

import android.content.pm.ApplicationInfo
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.chaldeaprjkt.gamespace.R

class AppsItemViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
    private val pm by lazy { v.context.packageManager }

    fun bind(app: ApplicationInfo, onClick: (ApplicationInfo) -> Unit) {
        v.findViewById<TextView>(R.id.app_name)?.text = app.loadLabel(pm)
        v.findViewById<TextView>(R.id.app_summary)?.text = app.packageName
        v.findViewById<ImageView>(R.id.app_icon)?.setImageDrawable(app.loadIcon(pm))
        v.findViewById<ViewGroup>(R.id.app_item)?.setOnClickListener {
            onClick.invoke(app)
        }
    }
}
