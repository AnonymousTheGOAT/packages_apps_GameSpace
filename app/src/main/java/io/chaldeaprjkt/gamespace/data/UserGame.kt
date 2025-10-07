/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.app.GameManager


data class UserGame(val packageName: String, val mode: Int = GameManager.GAME_MODE_STANDARD) {
    override fun toString(): String = "$packageName=$mode"

    companion object {
        fun fromSettings(data: String) =
            data.split("=").takeIf { it.size == 2 }
                ?.run { UserGame(first(), last().toInt()) }
                ?: UserGame(data)
    }
}
