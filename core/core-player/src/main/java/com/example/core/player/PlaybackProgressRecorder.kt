package com.example.core.player

/** The resumable audio in the player as last read from it: [positionMs] of [durationMs] (0: unknown). */
internal data class AudioPosition(
    val audioUrl: String,
    val title: String,
    val positionMs: Long,
    val durationMs: Long,
)

/**
 * Which positions to [save], told what the player does ([PlaybackProgressTracker]). Only an
 * audio that has played since it was loaded, or since it last ended, is saved: opening a
 * lesson without playing it (a finished one starts again at 0) leaves what was saved for it.
 * Each event passes the player's item as read then (null: nothing resumable).
 */
internal class PlaybackProgressRecorder(private val save: (AudioPosition) -> Unit) {

    private var current: AudioPosition? = null
    private var listened = false
    private var sinceSaveMs = 0L

    /** Another item is in the player: the one before is saved where it was last read. */
    fun onItemChanged(now: AudioPosition?) {
        if (now?.audioUrl != current?.audioUrl) {
            saveCurrent()
            listened = false
        }
        current = now
    }

    fun onPlaying(now: AudioPosition?) {
        current = now
        listened = now != null
        sinceSaveMs = 0L
    }

    /** Read while it plays, [elapsedMs] after the last read; saved every [SAVE_INTERVAL_MS]. */
    fun onTick(now: AudioPosition?, elapsedMs: Long) {
        current = now
        sinceSaveMs += elapsedMs
        if (sinceSaveMs >= SAVE_INTERVAL_MS) {
            sinceSaveMs = 0L
            saveCurrent()
        }
    }

    /** Paused, stopped or sought, or the service is stopping: saved as it is now. */
    fun onSettled(now: AudioPosition?) {
        current = now
        saveCurrent()
    }

    /** Played to the end: saved (finished), then not again until it plays once more. */
    fun onEnded(now: AudioPosition?) {
        current = now
        saveCurrent()
        listened = false
    }

    private fun saveCurrent() {
        val audio = current ?: return
        if (listened) save(audio)
    }

    companion object {
        /** While it plays, at most this much listening is lost if the app is killed. */
        const val SAVE_INTERVAL_MS = 10_000L
    }
}
