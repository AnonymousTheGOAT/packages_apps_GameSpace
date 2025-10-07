/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.data

import android.app.GameManager

/**
 * data class for setting up the Game Mode API Intervention
 */
data class GameConfig(val mode: Int, val downscaleFactor: Float, val useAngle: Boolean = false) {
    override fun toString(): String =
        hashMapOf<String, Any>().apply {
            put("mode", mode)
            put("downscaleFactor", "%.1f".format(downscaleFactor))
            // intentionally optional as game may already using it by default
            if (useAngle) put("useAngle", true)
        }.map { (k, v) -> "$k=$v" }.joinToString(",")

    companion object {
        fun Iterable<GameConfig>.asConfig() = this.joinToString(":") { it.toString() }
    }

    object ModeBuilder {
        var useAngle = false

        fun build() = listOf(
            GameConfig(GameManager.GAME_MODE_PERFORMANCE, .7f, useAngle),
            GameConfig(GameManager.GAME_MODE_BATTERY, .8f, useAngle)
        )
    }
}
