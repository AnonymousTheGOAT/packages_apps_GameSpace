/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.res.Resources.getSystem
import android.graphics.Point
import android.os.Process
import android.util.Log
import android.view.View
import android.view.WindowManager
import dagger.hilt.EntryPoints
import io.chaldeaprjkt.gamespace.gamebar.DraggableTouchListener

fun View.registerDraggableTouchListener(
    initPoint: () -> Point,
    listener: (x: Int, y: Int) -> Unit,
    onComplete: () -> Unit,
) = DraggableTouchListener(context, this, initPoint, listener, onComplete)

val Context.statusbarHeight
    get() =
        resources
            .getIdentifier("status_bar_height", "dimen", "android")
            .takeIf { it > 0 }
            ?.let { resources.getDimensionPixelSize(it) } ?: 24.dp

val Int.dp
    get() = (this * getSystem().displayMetrics.density).toInt()

fun WindowManager.isPortrait() =
    maximumWindowMetrics.bounds.width() < maximumWindowMetrics.bounds.height()

inline fun <reified T : Any> Context.entryPointOf(): T =
    EntryPoints.get(applicationContext, T::class.java)

@Suppress("DEPRECATION") // Deprecated for third party services.
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == serviceClass.name }

fun Context.getAppInfoForPackage(packageName: String): LauncherActivityInfo? {
    try {
        val launcherApps: LauncherApps? =
            getSystemService(LauncherApps::class.java) as LauncherApps? ?: return null
        val activityList: List<LauncherActivityInfo>? =
            launcherApps?.getActivityList(packageName, Process.myUserHandle()) ?: return null

        activityList?.let {
            for (activityInfo in it) {
                if (
                    launcherApps?.getMainActivityLaunchIntent(
                        activityInfo.componentName,
                        null,
                        activityInfo.user,
                    ) != null
                ) {
                    return activityInfo
                }
            }
        }
    } catch (e: RuntimeException) {
        Log.w("Failed to get app info for package: ${packageName}", e)
    }

    return null
}
