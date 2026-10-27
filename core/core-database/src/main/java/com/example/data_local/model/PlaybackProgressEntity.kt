package com.example.data_local.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Where an audio was left on this device, keyed by its URL (the player's id for it). A table
 * of its own, not columns of `audios`: the playback service writes it every few seconds,
 * which would re-run every query on `audios` (Room invalidates by table), and the server
 * sync rewrites `audios` rows. An audio played from search before it was cached is kept too.
 */
@Entity(tableName = "playback_progress")
data class PlaybackProgressEntity(
    @PrimaryKey
    val audioUrl: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long,
)

/** A saved position joined with the series of its (cached) audio. */
data class SeriesLessonProgressRow(
    val seriesId: String,
    @Embedded
    val progress: PlaybackProgressEntity,
)
