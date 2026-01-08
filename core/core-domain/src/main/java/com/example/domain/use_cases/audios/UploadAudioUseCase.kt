package com.example.domain.use_cases.audios


import com.example.domain.repository.AudiosRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A sealed class to represent the different states of an ongoing file upload.
 * This is a pure domain model.
 */
sealed class UploadResult {
    /** Represents the upload progress from 0 to 100. */
    data class Progress(val percentage: Int) : UploadResult()

    /** Represents a successful upload completion. */
    object Success : UploadResult()

    /** Represents a failure with an error message. */
    data class Error(val message: String) : UploadResult()
}


/**
 * This use case handles the business logic of uploading an audio file.
 * It remains pure by accepting primitive types and returning a Flow of results.
 */
class UploadAudioUseCase @Inject constructor(
    private val audiosRepository: AudiosRepository
) {
    /**
     * @param title The title for the audio metadata.
     * @param uriString The String representation of the file's content URI.
     * @param durationInMillis The duration of the audio.
     * @return A Flow that emits the current state of the upload (Progress, Success, or Error).
     */
    suspend operator fun invoke(
        title: String,
        uriString: String,
        durationInMillis: Long
    ): Flow<UploadResult> {
        // The use case now delegates without needing the file size.
        // This is cleaner and more robust.
        return audiosRepository.uploadAudio(title, uriString, durationInMillis)
    }
}