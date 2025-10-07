/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.UserHandle
import android.provider.Settings
import android.view.WindowManager
import com.android.internal.util.ScreenshotHelper
import com.android.systemui.screenrecord.IRemoteRecording
import javax.inject.Inject
import kotlin.system.exitProcess

/** utilities for interacting with system screenshot and recorder service */
class ScreenUtils @Inject constructor(private val context: Context) {

    private var isRecorderBound = false
    private var remoteRecording: IRemoteRecording? = null
    private var wakelock: PowerManager.WakeLock? = null
    private val recorderConnection =
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                try {
                    remoteRecording = IRemoteRecording.Stub.asInterface(service)
                } catch (e: Exception) {
                    e.printStackTrace()
                    exitProcess(1)
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                remoteRecording = null
            }
        }

    val recorder: IRemoteRecording?
        get() = remoteRecording

    private var isGestureLocked = false

    fun bind() {
        isRecorderBound =
            context.bindServiceAsUser(
                Intent().apply {
                    component =
                        ComponentName(
                            "com.android.systemui",
                            "com.android.systemui.screenrecord.RecordingService",
                        )
                },
                recorderConnection,
                Context.BIND_AUTO_CREATE,
                UserHandle.CURRENT,
            )
        if (!isRecorderBound) {
            exitProcess(1)
        }
        @Suppress("DEPRECATION") // we use it for stay-awake feature
        wakelock =
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.FULL_WAKE_LOCK,
                "GameSpace:ScreenUtils",
            )
    }

    fun unbind() {
        wakelock?.takeIf { it.isHeld }?.release()
        if (isRecorderBound) {
            context.unbindService(recorderConnection)
        }
        remoteRecording = null
        if (isGestureLocked) {
            Settings.System.putIntForUser(
                context.contentResolver,
                Settings.System.LOCK_GESTURE_STATUS,
                0,
                UserHandle.USER_CURRENT,
            )
            isGestureLocked = false
        }
    }

    fun takeScreenshot(onComplete: ((Uri?) -> Unit)? = null) {
        val handler = Handler(Looper.getMainLooper())
        ScreenshotHelper(context).takeScreenshot(
            WindowManager.ScreenshotSource.SCREENSHOT_GLOBAL_ACTIONS,
            handler,
        ) {
            handler.post { onComplete?.invoke(it) }
        }
    }

    var stayAwake = false
        get() = wakelock?.isHeld ?: false
        @SuppressLint("WakelockTimeout")
        set(enable) {
            field = enable
            if (enable) {
                wakelock?.takeIf { !it.isHeld }?.acquire()
            } else {
                wakelock?.takeIf { it.isHeld }?.release()
            }
        }

    var lockGesture = false
        get() = isGestureLocked
        set(enable) {
            Settings.System.putIntForUser(
                context.contentResolver,
                Settings.System.LOCK_GESTURE_STATUS,
                if (enable) 1 else 0,
                UserHandle.USER_CURRENT,
            )
            field = enable
            isGestureLocked = enable
        }
}
