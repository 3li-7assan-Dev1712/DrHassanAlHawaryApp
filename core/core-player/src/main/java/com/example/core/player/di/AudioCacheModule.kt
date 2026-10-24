package com.example.core.player.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/**
 * Disk cache for streamed audio: a replay, a seek back past the buffer or resuming an earlier
 * lecture reads the bytes already downloaded instead of Firebase Storage again.
 *
 * Safe to key by URL: every audio upload gets a new Storage path and replacing a file changes
 * its URL, so the bytes behind a URL never change. In cacheDir, so Android can reclaim it when
 * storage runs low and "Clear cache" empties it.
 *
 * Singleton, not service scoped: only one SimpleCache may use a folder per process, and the
 * playback service is recreated.
 */
@Module
@InstallIn(SingletonComponent::class)
@UnstableApi
object AudioCacheModule {

    private const val AUDIO_CACHE_DIR = "audio_cache"

    /** About 10 hours of the lectures at 64 kbps; the least recently played go first. */
    private const val AUDIO_CACHE_BYTES = 300L * 1024 * 1024

    @Provides
    @Singleton
    fun provideAudioCache(@ApplicationContext context: Context): Cache = SimpleCache(
        File(context.cacheDir, AUDIO_CACHE_DIR),
        LeastRecentlyUsedCacheEvictor(AUDIO_CACHE_BYTES),
        StandaloneDatabaseProvider(context),
    )
}
