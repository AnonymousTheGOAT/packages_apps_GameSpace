/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.preferences.appselector.adapter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.chaldeaprjkt.gamespace.R

class AppsAdapter(private val pm: PackageManager, private val apps: List<ApplicationInfo>) :
    ListAdapter<ApplicationInfo, AppsItemViewHolder>(DiffCallback(pm)) {

    private lateinit var onClick: (ApplicationInfo) -> Unit

    init {
        submitList(apps)
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppsItemViewHolder {
        return AppsItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.app_selector_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppsItemViewHolder, position: Int) {
        holder.bind(currentList[position]) {
            if (::onClick.isInitialized) {
                onClick.invoke(it)
            }
        }
    }

    fun onItemClick(action: (ApplicationInfo) -> Unit) {
        onClick = action
    }

    fun filterWith(text: String?) {
        val rText = ".*${text}.*".toRegex(RegexOption.IGNORE_CASE)
        apps.filter { it.loadLabel(pm).contains(rText) }
            .takeIf { it.isNotEmpty() }
            ?.run(::submitList) ?: submitList(apps)
    }

    private class DiffCallback(private val pm: PackageManager) :
        DiffUtil.ItemCallback<ApplicationInfo>() {
        override fun areItemsTheSame(oldItem: ApplicationInfo, newItem: ApplicationInfo) =
            oldItem.loadLabel(pm) == newItem.loadLabel(pm)

        override fun areContentsTheSame(oldItem: ApplicationInfo, newItem: ApplicationInfo) =
            oldItem.packageName == newItem.packageName
    }
}
