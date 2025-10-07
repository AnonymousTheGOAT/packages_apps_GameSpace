/*
 * SPDX-FileCopyrightText: 2021 Chaldeaprjkt
 * SPDX-License-Identifier: Apache-2.0
 */
package io.chaldeaprjkt.gamespace.utils.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.chaldeaprjkt.gamespace.data.AppSettings
import io.chaldeaprjkt.gamespace.data.GameSession
import io.chaldeaprjkt.gamespace.data.SystemSettings
import io.chaldeaprjkt.gamespace.utils.GameModeUtils
import io.chaldeaprjkt.gamespace.utils.ScreenUtils
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    fun provideBaseGson() = Gson()

    @Provides
    @Singleton
    fun provideScreenUtils(@ApplicationContext context: Context) = ScreenUtils(context)

    @Provides
    @Singleton
    fun provideGameModeUtils(@ApplicationContext context: Context) = GameModeUtils(context)

    @Provides
    @Singleton
    fun provideAppSettings(@ApplicationContext context: Context) = AppSettings(context)

    @Provides
    @Singleton
    fun provideSystemSettings(@ApplicationContext context: Context, gameModeUtils: GameModeUtils) =
        SystemSettings(context, gameModeUtils)

    @Provides
    @Singleton
    fun provideGameSession(
        @ApplicationContext context: Context,
        appSettings: AppSettings,
        systemSettings: SystemSettings,
        gson: Gson
    ) = GameSession(context, appSettings, systemSettings, gson)
}
