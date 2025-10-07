/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022-2024 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.gamebar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.UserHandle

class GameBroadcastReceiver : BroadcastReceiver() {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            GAME_START -> context.onGameStart(intent)
            GAME_STOP -> context.onGameStop()
        }
    }

    private fun Context.onGameStart(intent: Intent) {
        val app = intent.getStringExtra(SessionService.EXTRA_PACKAGE_NAME) ?: return
        handler.post {
            resendBroadcast(intent)
            SessionService.start(this, app)
        }
    }

    private fun Context.onGameStop() {
        handler.post {
            resendBroadcast(Intent(GAME_STOP))
            SessionService.stop(this)
        }
    }

    private fun Context.resendBroadcast(prevIntent: Intent) {
        val intent =
            (prevIntent.clone() as Intent).apply {
                setPackage(null)
                component = null
            }
        val flags = PackageManager.ResolveInfoFlags.of(0)
        packageManager
            .queryBroadcastReceivers(intent, flags)
            .mapNotNull { it.activityInfo?.packageName }
            .filter { it != packageName }
            .forEach {
                (intent.clone() as Intent).apply {
                    setPackage(it)
                    sendBroadcastAsUser(
                        this,
                        UserHandle.CURRENT,
                        android.Manifest.permission.MANAGE_GAME_MODE,
                    )
                }
            }
    }

    companion object {
        const val GAME_START = "io.chaldeaprjkt.gamespace.action.GAME_START"
        const val GAME_STOP = "io.chaldeaprjkt.gamespace.action.GAME_STOP"
    }
}
