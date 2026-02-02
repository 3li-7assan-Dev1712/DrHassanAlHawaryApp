package com.example.core.player.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
@UnstableApi
object PlayerModule {

    @Provides
    @ServiceScoped
    fun providePlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @ServiceScoped
    fun provideMediaSession(player: ExoPlayer, @ApplicationContext context: Context): MediaSession {
        return MediaSession.Builder(context, player).build()
    }
}
