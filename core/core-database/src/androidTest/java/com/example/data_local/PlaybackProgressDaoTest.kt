package com.example.data_local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data_local.model.AudioEntity
import com.example.data_local.model.PlaybackProgressEntity
import com.example.data_local.model.SeriesLessonProgressRow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackProgressDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PlaybackProgressDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.playbackProgressDao()
    }

    @After
    fun closeDatabase() = database.close()

    private fun progress(url: String, positionMs: Long, updatedAt: Long) =
        PlaybackProgressEntity(url, "title $url", positionMs, durationMs = 1_800_000L, updatedAt = updatedAt)

    private fun audio(url: String, seriesId: String?, isDeleted: Boolean = false) = AudioEntity(
        id = "id-$url",
        title = "title $url",
        audioUrl = url,
        durationInMillis = 1_800_000L,
        publishDate = 0L,
        updatedAt = 0L,
        isDeleted = isDeleted,
        seriesId = seriesId,
    )

    @Test
    fun upsertReplacesTheSavedPosition() = runBlocking {
        dao.upsert(progress("a", positionMs = 10_000, updatedAt = 1))
        dao.upsert(progress("a", positionMs = 20_000, updatedAt = 2))

        assertEquals(progress("a", positionMs = 20_000, updatedAt = 2), dao.get("a"))
        assertNull(dao.get("never played"))
    }

    @Test
    fun observeAllListsTheLatestFirst() = runBlocking {
        dao.upsert(progress("old", positionMs = 10_000, updatedAt = 1))
        dao.upsert(progress("new", positionMs = 10_000, updatedAt = 3))
        dao.upsert(progress("middle", positionMs = 10_000, updatedAt = 2))

        assertEquals(listOf("new", "middle", "old"), dao.observeAll().first().map { it.audioUrl })
    }

    @Test
    fun seriesLessonsAreTheCachedLessonsOfASeries() = runBlocking {
        database.audioDao().upsertAll(
            listOf(audio("lesson", seriesId = "bukhari"), audio("standalone", seriesId = null), audio("deleted", seriesId = "bukhari", isDeleted = true))
        )
        listOf("lesson", "standalone", "deleted", "not cached").forEachIndexed { index, url ->
            dao.upsert(progress(url, positionMs = 10_000, updatedAt = index.toLong()))
        }

        assertEquals(
            listOf(SeriesLessonProgressRow("bukhari", progress("lesson", positionMs = 10_000, updatedAt = 0))),
            dao.observeSeriesLessons().first(),
        )
    }
}
