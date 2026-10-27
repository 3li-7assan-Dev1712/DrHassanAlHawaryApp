package com.example.domain.module

/**
 * A series as far as its lessons have been heard: [completed] of them finished, and
 * [resumeIndex], the lesson «متابعة» opens (an index into the lessons; null: the series plays
 * from the start).
 */
data class SeriesProgress(val completed: Int, val resumeIndex: Int?) {

    /** A lesson was finished or is under way: the series shows its progress. */
    val isStarted: Boolean get() = completed > 0 || resumeIndex != null

    companion object {
        val NotStarted = SeriesProgress(completed = 0, resumeIndex = null)

        /**
         * [lessonUrls] in episode order, with the saved [progress] by audio URL. The lesson to
         * resume follows the one heard last: that lesson while it is in progress, the next
         * one once it is finished (none after the last lesson). Lessons only glanced at
         * ([PlaybackProgress.isInProgress] false and not finished) are ignored.
         */
        fun of(lessonUrls: List<String>, progress: Map<String, PlaybackProgress>): SeriesProgress {
            val heard = lessonUrls.mapIndexedNotNull { index, url ->
                progress[url]?.takeIf { it.isInProgress || it.isFinished }?.let { index to it }
            }
            val last = heard.maxByOrNull { (_, lesson) -> lesson.updatedAt } ?: return NotStarted
            val (lastIndex, lastLesson) = last
            val resumeIndex = when {
                lastLesson.isInProgress -> lastIndex
                lastIndex < lessonUrls.lastIndex -> lastIndex + 1
                else -> null
            }
            return SeriesProgress(completed = heard.count { (_, lesson) -> lesson.isFinished }, resumeIndex = resumeIndex)
        }
    }
}

/** A lesson's saved position, with the series it belongs to. */
data class SeriesLessonProgress(val seriesId: String, val progress: PlaybackProgress)
