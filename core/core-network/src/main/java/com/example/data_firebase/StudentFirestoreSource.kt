package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.LessonDto
import com.example.data_firebase.model.LevelDto
import com.example.data_firebase.model.PlaylistDto
import com.example.data_firebase.model.StudentDto
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class StudentFirestoreSource @Inject constructor(
    val firestore: FirebaseFirestore, private val storage: FirebaseStorage
) {

    private val TAG = "StudentFirestoreSource"
    private val studentsCollection = firestore.collection("students")
    private val adminCollection = firestore.collection("admins")


    suspend fun storeStudent(studentDto: StudentDto) {
        try {
            // Using the telegramId as the document ID for easy lookups and to prevent duplicates
            studentsCollection.document(studentDto.id.toString()).set(studentDto).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing student data in Firestore", e)
            throw e // Re-throw the exception to be handled by the repository
        }
    }


    suspend fun getStudentByTelegramId(telegramId: Long): StudentDto? {
        return try {
            val document = studentsCollection.document(telegramId.toString()).get().await()
            StudentDto(
                id = document.getLong("id") ?: 0,
                firstName = document.getString("firstName") ?: "",
                lastName = document.getString("lastName") ?: "",
                username = document.getString("username") ?: "",
                photoUrl = document.getString("photoUrl") ?: "",
                isChannelMember = document.getBoolean("isChannelMember") ?: false,
                membershipState = document.getString("membershipState") ?: "",
                isConnectedToTelegram = document.getBoolean("isConnectedToTelegram") ?: false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student by telegramId: $telegramId", e)
            null // Return null on error so the app doesn't crash
        }
    }

    suspend fun getAdminDataByTelegramId(telegramId: Long): StudentDto? {
        return try {
            val document = adminCollection.document(telegramId.toString()).get().await()
            val dto = StudentDto(
                id = document.getLong("id") ?: 0,
                firstName = document.getString("firstName") ?: "",
                lastName = document.getString("lastName") ?: "",
                username = document.getString("username") ?: "",
                photoUrl = document.getString("photoUrl") ?: "",
                isChannelMember = document.getBoolean("isChannelMember") ?: false,
                membershipState = document.getString("membershipState") ?: "",
                isConnectedToTelegram = document.getBoolean("isConnectedToTelegram") ?: false
            )
            Log.d(
                TAG, "getAdminDataByTelegramId: name: ${dto.firstName} -- ${dto.membershipState} "
            )
            dto
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student by telegramId: $telegramId", e)
            null // Return null on error so the app doesn't crash
        }
    }

    suspend fun updateStudentConnectionStatus(telegramId: Long, isConnected: Boolean) {
        try {
            studentsCollection.document(telegramId.toString())
                .update("isConnectedToTelegram", isConnected).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student connection status for id: $telegramId", e)
            throw e // Re-throw to be handled by the repository
        }
    }

    suspend fun getUpdatedPlaylists(lastPlaylistSync: Long): List<PlaylistDto> {
        return try {
            Log.d(TAG, "getPlaylists: last time: $lastPlaylistSync ${Date(lastPlaylistSync)}")
            val playlistsCollection = firestore.collection("playlists")
                .whereGreaterThan("updatedAt", Date(lastPlaylistSync))

            val snapshot = playlistsCollection.get().await()
            snapshot.mapNotNull { document ->

                val dto = PlaylistDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    levelId = document.getString("levelId") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0,
                    thumbnailUrl = document.getString("thumbnailUrl") ?: "",
                    updatedAt = document.getDate("updatedAt") ?: Date()
                )
                Log.d(TAG, "getPlaylistForLevel: ${dto.title}")
                dto
            }
        } catch (e: Exception) {
            emptyList() // Return an empty list on error
        }
    }

    suspend fun getRemotePlaylistForLevel(levelId: String): List<PlaylistDto> {
        return try {
            val playlistsCollection =
                firestore.collection("playlists").whereEqualTo("levelId", levelId)

            val snapshot = playlistsCollection.get().await()
            snapshot.mapNotNull { document ->

                val dto = PlaylistDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    levelId = document.getString("levelId") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0,
                    thumbnailUrl = document.getString("thumbnailUrl") ?: "",
                    updatedAt = document.getDate("updatedAt") ?: Date()
                )
                Log.d(TAG, "getPlaylistForLevel: ${dto.title}")
                dto
            }
        } catch (e: Exception) {
            emptyList() // Return an empty list on error
        }
    }

    suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<LessonDto> {
        return try {
            val lessonsCollection =
                firestore.collection("lessons").whereEqualTo("playlistId", playlistId)

            val snapshot = lessonsCollection.get().await()
            snapshot.mapNotNull { document ->

                val dto = document.toObject<LessonDto>()
                Log.d(TAG, "getPlaylistForLevel: ${dto.title}")
                dto
                /* val dto = LessonDto(
                     id = document.getString("id") ?: "",
                     title = document.getString("title") ?: "",
                     playlistId = document.getString("playlistId") ?: "",
                     order = document.getLong("order")?.toInt() ?: 0,
                     audioUrl = document.getString("audioRemoteUrl") ?: "",
                     duration = ,
                     pdfUrl = document.getString("audioFilePath") ?: "",
                     updatedAt = document.getDate("updatedAt") ?: Date()
                 )*/

            }
        } catch (e: Exception) {
            emptyList() // Return an empty list on error
        }
    }

    suspend fun getRemotePlaylistById(playlistId: String): PlaylistDto? {
        return try {
            val doc = firestore.collection("playlists")
                .document(playlistId)
                .get()
                .await()

            if (!doc.exists()) return null

            PlaylistDto(
                id = doc.getString("id") ?: doc.id,
                title = doc.getString("title") ?: "",
                levelId = doc.getString("levelId") ?: "",
                order = doc.getLong("order")?.toInt() ?: 0,
                thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                updatedAt = doc.getDate("updatedAt") ?: Date()
            )
        } catch (e: Exception) {
            Log.d(TAG, "getRemotePlaylistById error: ${e.message}", e)
            null
        }
    }

    suspend fun getRemoteLessonById(lessonId: String): LessonDto? {
        return try {
            val doc = firestore.collection("lessons")
                .document(lessonId)
                .get()
                .await()

            if (!doc.exists()) return null

            doc.toObject<LessonDto>()
        } catch (e: Exception) {
            Log.d(TAG, "getRemotePlaylistById error: ${e.message}", e)
            null
        }
    }


    fun uploadPlaylist(playlistDto: PlaylistDto): Flow<UploadResult> {

        return callbackFlow {
            if (playlistDto.thumbnailUrl.isEmpty()) {
                trySend(UploadResult.Error("No images to upload."))
                close(); return@callbackFlow
            }
            trySend(UploadResult.Progress(0))

            try {

                val uri = playlistDto.thumbnailUrl.toUri()
                val imageRef =
                    storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
                val downloadUrl =
                    imageRef.putFile(uri).await().storage.downloadUrl.await().toString()


                val playlistsRef = firestore.collection("playlists").document()
                val newDocumentId = playlistsRef.id
                val playlistToUpload = playlistDto.copy(
                    id = newDocumentId, thumbnailUrl = downloadUrl
                )

                playlistsRef.set(playlistToUpload).await()

                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()

                awaitClose { }


                trySend(UploadResult.Progress(100))

            } catch (e: Exception) {
                trySend(UploadResult.Error("Failed to upload image: ${e.message}"))
                close(); return@callbackFlow
            }


        }

    }

    fun addLesson(lessonDto: LessonDto, playlistId: String): Flow<UploadResult> {

        return callbackFlow {
            if (lessonDto.audioUrl.isEmpty() || lessonDto.pdfUrl.isEmpty()) {
                trySend(UploadResult.Error("No files to upload."))
                close(); return@callbackFlow
            }
            trySend(UploadResult.Progress(0))

            try {

                val audioUri = lessonDto.audioUrl.toUri()
                val audioRef = storage.reference.child("audios/${System.currentTimeMillis()}")
                val audioDownloadUrl =
                    audioRef.putFile(audioUri).await().storage.downloadUrl.await().toString()
                trySend(UploadResult.Progress(45))

                val pdfUri = lessonDto.pdfUrl.toUri()
                val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
                val pdfDownloadUrl =
                    pdfRef.putFile(pdfUri).await().storage.downloadUrl.await().toString()
                trySend(UploadResult.Progress(90))

                val lessonRef = firestore.collection("lessons").document()
                val newDocumentId = lessonRef.id
                val lessonToUpload = lessonDto.copy(
                    id = newDocumentId,
                    audioUrl = audioDownloadUrl,
                    pdfUrl = pdfDownloadUrl,
                    playlistId = playlistId
                )
                lessonRef.set(lessonToUpload).await()

                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()

                awaitClose { }

            } catch (e: Exception) {
                trySend(UploadResult.Error("Failed to upload image: ${e.message}"))
                close(); return@callbackFlow
            }


        }

    }


    suspend fun updatePlaylist(
        playlistId: String,
        newTitle: String? = null,
        newLevelId: String? = null,
        newOrder: Int? = null,
        newThumbnailLocalOrRemote: String? = null
    ): Result<String> {
        return try {

            val playlistDoc = firestore.collection("playlists").document(playlistId)

            // Upload new thumbnail if user picked a local uri, otherwise keep remote url as-is
            val finalThumbnailUrl = when {
                newThumbnailLocalOrRemote.isNullOrBlank() -> null
                newThumbnailLocalOrRemote.startsWith("http") -> newThumbnailLocalOrRemote
                else -> {
                    val uri = newThumbnailLocalOrRemote.toUri()
                    val imageRef =
                        storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
                    imageRef.putFile(uri).await()
                    imageRef.downloadUrl.await().toString()
                }
            }

            Log.d(TAG, "updatePlaylist: $newThumbnailLocalOrRemote")
            //  Build a partial update map (only update provided fields)
            val updates = hashMapOf<String, Any>()
            newTitle?.let { updates["title"] = it }
            newLevelId?.let { updates["levelId"] = it }
            newOrder?.let { updates["order"] = it }
            finalThumbnailUrl?.let { updates["thumbnailUrl"] = it }
            updates["updatedAt"] = FieldValue.serverTimestamp()

            //  Update
            if (updates.isNotEmpty()) {
                playlistDoc.update(updates).await()
            }
            Result.success("Playlist updated successfully")
        } catch (e: Exception) {
            Log.d(TAG, "updatePlaylist: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun deleteFileByUrl(fileUrl: String): Boolean {
        val storage = FirebaseStorage.getInstance()
        val ref = storage.getReferenceFromUrl(fileUrl)

        ref.delete().await().also {
            Log.d(TAG, "deleteFileByUrl: success")
            return true
        }

    }


    suspend fun updateLesson(
        lessonId: String,
        newTitle: String? = null,
        newOrder: Int? = null,
        localAudioUrl: String? = null,
        remoteAudioUrl: String? = null,
        localPdfUrl: String? = null,
        remotePdfUrl: String? = null
    ): Result<String> {
        return try {
            val lessonDoc = firestore.collection("lessons").document(lessonId)

            val updates = hashMapOf<String, Any>()

            // ---- AUDIO ----
            var uploadedAudioUrl: String? = null
            if (!localAudioUrl.isNullOrBlank()) {
                val audioRef = storage.reference.child("audios/${System.currentTimeMillis()}")
                val localUri = localAudioUrl.toUri()

                audioRef.putFile(localUri).await()
                uploadedAudioUrl = audioRef.downloadUrl.await().toString()
                updates["audioUrl"] = uploadedAudioUrl
            }

            // delete old audio AFTER successful upload
            if (!uploadedAudioUrl.isNullOrBlank() && !remoteAudioUrl.isNullOrBlank()) {
                runCatching { deleteFileByUrl(remoteAudioUrl) } // ignore not-found
            }

            // ---- PDF ----
            var uploadedPdfUrl: String? = null
            if (!localPdfUrl.isNullOrBlank()) {
                val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
                val localUri = localPdfUrl.toUri()

                pdfRef.putFile(localUri).await()
                uploadedPdfUrl = pdfRef.downloadUrl.await().toString()
                updates["pdfUrl"] = uploadedPdfUrl
            }

            // delete old pdf AFTER successful upload
            if (!uploadedPdfUrl.isNullOrBlank() && !remotePdfUrl.isNullOrBlank()) {
                runCatching { deleteFileByUrl(remotePdfUrl) }
            }

            // ---- OTHER FIELDS ----
            newTitle?.let { updates["title"] = it }
            newOrder?.let { updates["order"] = it }
            updates["updatedAt"] = FieldValue.serverTimestamp()

            if (updates.isNotEmpty()) lessonDoc.update(updates).await()

            Result.success("lesson updated successfully")
        } catch (e: Exception) {
            Log.d(TAG, "update lesson: ${e.message}", e)
            Result.failure(e)
        }
    }


    suspend fun getUpdatedLessons(lastLessonSync: Long): List<LessonDto> {
        return try {
            Log.d(TAG, "getPlaylists: last time: $lastLessonSync ${Date(lastLessonSync)}")
            val lessonsCollection =
                firestore.collection("lessons").whereGreaterThan("updatedAt", Date(lastLessonSync))

            val snapshot = lessonsCollection.get().await()
            snapshot.mapNotNull { document ->
                document.toObject<LessonDto>()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Return an empty list on error
        }
    }

    suspend fun getRemoteLevels(): List<LevelDto> {
        val levelsCollection = firestore.collection("levels")
        val snapshot = levelsCollection.get().await()
        return try {
            snapshot.mapNotNull { document ->
                Log.d(TAG, "getRemoteLevels: ${document.getString("title")}")
//            document.toObject<LevelDto>()
                val dto = LevelDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0
                )
                dto
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting remote levels", e)
            emptyList()
        }
    }

    suspend fun getLevelsVersion(): Long {
        val versionCollection =
            firestore.collection("metadata").document("content_versions").get().await()
        return versionCollection.getLong("levelsVersion") ?: 0
    }

    suspend fun getRemoteMotivationalMessages(): List<String> {
        return try {
            val document = firestore.collection("motivational_messages")
                .document("daily_messages")
                .get()
                .await()

            // Firestore returns arrays as List<*>, so we cast to List<String>
            // We use 'get' and a safe cast to avoid crashing if the field is missing or has a wrong type
            document.get("messages") as? List<String> ?: emptyList()

        } catch (e: Exception) {
            Log.d(TAG, "getRemoteMotivationalMessages: ${e.message}")
            emptyList()
        }

    }

}
