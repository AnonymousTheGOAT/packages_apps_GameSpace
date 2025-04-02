/*
 * SPDX-FileCopyrightText: The CyanogenMod Project
 * SPDX-FileCopyrightText: The LineageOS project
 * SPDX-FileCopyrightText: TheParasiteProject
 * SPDX-License-Identifier: Apache-2.0
 */

package io.chaldeaprjkt.gamespace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.chaldeaprjkt.gamespace.services.GameSpaceManagerService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Intent(context ?: return, GameSpaceManagerService::class.java).also {
            context.startService(it)
        }
    }
}
