/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-FileCopyrightText: 2022-2024 crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.media.AudioManager
import androidx.annotation.Keep

@Keep
data class SessionState(
    var packageName: String,
    var autoBrightness: Boolean? = null,
    var headsup: Boolean? = null,
    var threeScreenshot: Int = 0,
    var ringerMode: Int = AudioManager.RINGER_MODE_NORMAL,
)
