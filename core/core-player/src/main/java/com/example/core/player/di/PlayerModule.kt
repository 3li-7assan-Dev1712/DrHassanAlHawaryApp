package com.example.core.player.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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
    fun providePlayer(@ApplicationContext context: Context, audioCache: Cache): ExoPlayer {
        // Streams go through the disk cache (AudioCacheModule). DefaultDataSource reads local
        // files (downloaded audios) and content URIs itself and only hands remote URIs to this
        // factory, so downloads are not copied into the cache. On a cache read or write error
        // the request falls back to the network instead of failing playback.
        val cachedHttp = CacheDataSource.Factory()
            .setCache(audioCache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(DefaultDataSource.Factory(context, cachedHttp))
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    @Provides
    @ServiceScoped
    fun provideMediaSession(player: ExoPlayer, @ApplicationContext context: Context): MediaSession {
        return MediaSession.Builder(context, player).build()
    }
}
