/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022-2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2023-2024 the risingOS Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.settings

import android.os.Bundle
import android.view.View
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import dagger.hilt.android.AndroidEntryPoint
import io.chaldeaprjkt.gamespace.R

@AndroidEntryPoint(SettingsBasePreferenceFragment::class)
class SettingsFragment : Hilt_SettingsFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }
}
