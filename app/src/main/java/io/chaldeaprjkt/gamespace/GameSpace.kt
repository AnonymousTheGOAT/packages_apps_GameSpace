/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */

package io.chaldeaprjkt.gamespace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp(Application::class) class GameSpace : Hilt_GameSpace()
