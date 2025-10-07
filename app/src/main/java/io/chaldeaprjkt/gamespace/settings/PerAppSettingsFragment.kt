/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022-2024 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.settings

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.LayoutPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import dagger.hilt.android.AndroidEntryPoint
import io.chaldeaprjkt.gamespace.R
import io.chaldeaprjkt.gamespace.data.GameConfig
import io.chaldeaprjkt.gamespace.data.SystemSettings
import io.chaldeaprjkt.gamespace.data.UserGame
import io.chaldeaprjkt.gamespace.utils.GameModeUtils
import io.chaldeaprjkt.gamespace.utils.getAppInfoForPackage
import javax.inject.Inject

@AndroidEntryPoint(SettingsBasePreferenceFragment::class)
class PerAppSettingsFragment :
    Hilt_PerAppSettingsFragment(), Preference.OnPreferenceChangeListener {

    @Inject lateinit var settings: SystemSettings

    @Inject lateinit var gameModeUtils: GameModeUtils

    private val currentGame by lazy {
        activity?.intent?.getStringExtra(PerAppSettingsActivity.EXTRA_PACKAGE)?.let {
            val flags = PackageManager.ApplicationInfoFlags.of(0)
            context?.packageManager?.getApplicationInfo(it, flags)
        }
    }

    private val currentConfig: UserGame?
        get() = settings.userGames.firstOrNull { it.packageName == currentGame?.packageName }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = context?.getString(R.string.per_app_title)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.per_app_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<LayoutPreference>("headers")?.apply {
            findViewById<ImageView>(android.R.id.icon)
                ?.setImageDrawable(currentGame?.loadIcon(requireContext().packageManager))
            findViewById<TextView>(android.R.id.title)?.text =
                currentGame?.loadLabel(requireContext().packageManager)
            setOnPreferenceClickListener {
                currentGame?.packageName?.let {
                    val appActivity = context.getAppInfoForPackage(it)
                    if (appActivity != null) {
                        val intent =
                            Intent().apply {
                                setComponent(appActivity.componentName)
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or
                                        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
                                )
                            }
                        context.startActivityAsUser(intent, Process.myUserHandle())
                    }
                }
                true
            }
        }
        findPreference<ListPreference>(PREF_PREFERRED_MODE)?.apply {
            currentConfig?.mode?.let { value = it.toString() }
            onPreferenceChangeListener = this@PerAppSettingsFragment
        }
        findPreference<SwitchPreferenceCompat>(PREF_USE_ANGLE)?.apply {
            context.resources?.getBoolean(R.bool.config_allow_per_app_angle_usage)?.let {
                isVisible = it
                if (!it) return@apply
            }

            if (gameModeUtils.findAnglePackage()?.isEnabled != true) {
                isEnabled = false
                summary = context.getString(R.string.cant_find_angle_pkg)
                return@apply
            }
            isChecked = gameModeUtils.isAngleUsed(currentGame?.packageName)
            onPreferenceChangeListener = this@PerAppSettingsFragment
        }
        findPreference<Preference>(PREF_UNREGISTER)?.apply {
            summary =
                context.getString(
                    R.string.per_app_unregister,
                    currentGame?.loadLabel(context.packageManager),
                )
            setOnPreferenceClickListener {
                activity?.setResult(
                    Activity.RESULT_OK,
                    Intent().apply { putExtra(PREF_UNREGISTER, currentGame?.packageName) },
                )
                activity?.finish()
                true
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val gameInfo = currentGame ?: return false
        when (preference.key) {
            PREF_PREFERRED_MODE -> {
                val newMode = (newValue as String).toIntOrNull() ?: 1
                gameModeUtils.setGameModeFor(gameInfo.packageName, settings, newMode)
                return true
            }
            PREF_USE_ANGLE -> {
                val newModes =
                    GameConfig.ModeBuilder.apply { useAngle = newValue as Boolean }.build()
                gameModeUtils.setIntervention(gameInfo.packageName, newModes)
                return true
            }
        }
        return false
    }

    companion object {
        const val PREF_PREFERRED_MODE = "per_app_preferred_mode"
        const val PREF_USE_ANGLE = "per_app_use_angle"
        const val PREF_UNREGISTER = "per_app_unregister"
    }
}
