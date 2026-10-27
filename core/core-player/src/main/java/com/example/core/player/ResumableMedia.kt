package com.example.core.player

import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * Marks the media items whose position the playback service keeps, so they open where they
 * were left ([PlaybackProgressTracker]): the audios, by their URL as the media id. Other items
 * (the institute's lessons) are not marked. The mark is in the item's metadata, which reaches
 * the service with it.
 */
object ResumableMedia {

    private const val EXTRA_RESUMABLE = "com.example.core.player.RESUMABLE"

    /** The metadata of a resumable item titled [title]. */
    fun metadata(title: String): MediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setExtras(Bundle().apply { putBoolean(EXTRA_RESUMABLE, true) })
        .build()

    fun isResumable(item: MediaItem): Boolean = item.mediaMetadata.extras?.getBoolean(EXTRA_RESUMABLE) == true
}
