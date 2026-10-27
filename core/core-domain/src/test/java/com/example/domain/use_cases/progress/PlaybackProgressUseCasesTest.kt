package com.example.domain.use_cases.progress

import com.example.domain.module.PlaybackProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackProgressUseCasesTest {

    private val minute = 60_000L
    private val repository = FakePlaybackProgressRepository(seriesOf = mapOf("l1" to "bukhari", "l2" to "bukhari", "l3" to "hajj"))
    private val save = SavePlaybackProgressUseCase(repository)

    private suspend fun saveAt(url: String, positionMs: Long, savedAt: Long, durationMs: Long = 30 * minute) =
        save(audioUrl = url, title = "title $url", positionMs = positionMs, durationMs = durationMs, savedAt = savedAt)

    @Test
    fun `save keeps the position within the audio and skips items without a url`() = runBlocking {
        saveAt("a", positionMs = -5, savedAt = 1)
        saveAt("b", positionMs = 40 * minute, savedAt = 2)
        saveAt("c", positionMs = 10 * minute, savedAt = 3, durationMs = -1)
        saveAt(" ", positionMs = 10 * minute, savedAt = 4)

        assertEquals(PlaybackProgress("a", "title a", 0L, 30 * minute, 1), repository.get("a"))
        assertEquals(30 * minute, repository.get("b")?.positionMs)
        assertEquals(PlaybackProgress("c", "title c", 10 * minute, 0L, 3), repository.get("c"))
        assertNull(repository.get(" "))
    }

    @Test
    fun `resume position is where it was left, or the start`() = runBlocking {
        val resume = GetResumePositionUseCase(repository)
        saveAt("left", positionMs = 12 * minute, savedAt = 1)
        saveAt("done", positionMs = 30 * minute, savedAt = 2)

        assertEquals(12 * minute, resume("left"))
        assertEquals(0L, resume("done"))
        assertEquals(0L, resume("never played"))
    }

    @Test
    fun `continue listening lists audios in progress, latest first, up to the limit`() = runBlocking {
        saveAt("old", positionMs = 10 * minute, savedAt = 1)
        saveAt("glanced", positionMs = 2_000, savedAt = 2)
        saveAt("finished", positionMs = 30 * minute, savedAt = 3)
        saveAt("recent", positionMs = 5 * minute, savedAt = 4)
        saveAt("latest", positionMs = 20 * minute, savedAt = 5)

        val shown = ObserveContinueListeningUseCase(repository)(limit = 2).first()

        assertEquals(listOf("latest", "recent"), shown.map { it.audioUrl })
    }

    @Test
    fun `progress by url`() = runBlocking {
        saveAt("a", positionMs = 10 * minute, savedAt = 1)

        val byUrl = ObservePlaybackProgressUseCase(repository)().first()

        assertEquals(setOf("a"), byUrl.keys)
        assertEquals(10 * minute, byUrl.getValue("a").positionMs)
    }

    @Test
    fun `series completion counts finished lessons of started series only`() = runBlocking {
        saveAt("l1", positionMs = 30 * minute, savedAt = 1)
        saveAt("l2", positionMs = 10 * minute, savedAt = 2)
        saveAt("l3", positionMs = 2_000, savedAt = 3)
        saveAt("standalone", positionMs = 30 * minute, savedAt = 4)

        val completion = ObserveSeriesCompletionUseCase(repository)().first()

        assertEquals(mapOf("bukhari" to 1), completion)
    }
}
