package com.example.feature.share.domain

/**
 * A local, already-trimmed media file: exactly the window the user chose,
 * never the full source track.
 */
data class ShareClip(
    val filePath: String,
    val startMs: Long,
    val durationMs: Long,
)
