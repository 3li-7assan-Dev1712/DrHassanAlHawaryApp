package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.LeaderboardDto
import com.example.data_firebase.model.LessonDto
import com.example.data_firebase.model.LevelDto
import com.example.data_firebase.model.PlaylistDto
import com.example.data_firebase.model.QuizDto
import com.example.data_firebase.model.UserDto
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    private val usersCollection = firestore.collection("users")


    suspend fun upsertUser(userDto: UserDto) {
        try {
            usersCollection.document(userDto.uid).set(userDto).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing user data in Firestore", e)
            throw e
        }
    }

    fun observeUser(uid: String): Flow<UserDto?> = callbackFlow {
        val listener = usersCollection.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                trySend(snapshot.toObject<UserDto>())
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getUser(uid: String): UserDto? {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                document.toObject<UserDto>()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user by uid: $uid", e)
            null
        }
    }

    suspend fun updateTelegramDisconnected(uid: String) {
        try {
            val updates = hashMapOf<String, Any?>(
                "isConnectedToTelegram" to false,
                "telegramId" to null,
                "telegramUsername" to null,
                "telegramFirstName" to null,
                "telegramLastName" to null,
                "telegramPhotoUrl" to null,
                "membershipState" to "none"
            )
            usersCollection.document(uid).update(updates).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting telegram for user: $uid", e)
            throw e
        }
    }

    // --- Keep existing playlist/lesson/quiz methods below ---

    suspend fun getUpdatedPlaylists(lastPlaylistSync: Long): List<PlaylistDto> {
        return try {
            val playlistsCollection = firestore.collection("playlists")
                .whereGreaterThan("updatedAt", Date(lastPlaylistSync))

            val snapshot = playlistsCollection.get().await()
            snapshot.mapNotNull { document ->
                PlaylistDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    levelId = document.getString("levelId") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0,
                    thumbnailUrl = document.getString("thumbnailUrl") ?: "",
                    updatedAt = document.getDate("updatedAt") ?: Date()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemotePlaylistForLevel(levelId: String): List<PlaylistDto> {
        return try {
            val playlistsCollection =
                firestore.collection("playlists").whereEqualTo("levelId", levelId)

            val snapshot = playlistsCollection.get().await()
            snapshot.mapNotNull { document ->
                PlaylistDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    levelId = document.getString("levelId") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0,
                    thumbnailUrl = document.getString("thumbnailUrl") ?: "",
                    updatedAt = document.getDate("updatedAt") ?: Date()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<LessonDto> {
        return try {
            val lessonsCollection =
                firestore.collection("lessons").whereEqualTo("playlistId", playlistId)

            val snapshot = lessonsCollection.get().await()
            snapshot.mapNotNull { document ->
                document.toObject<LessonDto>()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemotePlaylistById(playlistId: String): PlaylistDto? {
        return try {
            val doc = firestore.collection("playlists").document(playlistId).get().await()
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
            null
        }
    }

    suspend fun getRemoteLessonById(lessonId: String): LessonDto? {
        return try {
            val doc = firestore.collection("lessons").document(lessonId).get().await()
            if (!doc.exists()) return null
            doc.toObject<LessonDto>()
        } catch (e: Exception) {
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
                val imageRef = storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
                val downloadUrl = imageRef.putFile(uri).await().storage.downloadUrl.await().toString()
                val playlistsRef = firestore.collection("playlists").document()
                val playlistToUpload = playlistDto.copy(id = playlistsRef.id, thumbnailUrl = downloadUrl)
                playlistsRef.set(playlistToUpload).await()
                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()
            } catch (e: Exception) {
                trySend(UploadResult.Error("Failed to upload image: ${e.message}"))
                close()
            }
            awaitClose { }
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
                val audioDownloadUrl = audioRef.putFile(audioUri).await().storage.downloadUrl.await().toString()
                trySend(UploadResult.Progress(45))

                val pdfUri = lessonDto.pdfUrl.toUri()
                val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
                val pdfDownloadUrl = pdfRef.putFile(pdfUri).await().storage.downloadUrl.await().toString()
                trySend(UploadResult.Progress(90))

                val lessonRef = firestore.collection("lessons").document()
                val lessonToUpload = lessonDto.copy(
                    id = lessonRef.id,
                    audioUrl = audioDownloadUrl,
                    pdfUrl = pdfDownloadUrl,
                    playlistId = playlistId
                )
                lessonRef.set(lessonToUpload).await()
                trySend(UploadResult.Progress(100))
                trySend(UploadResult.Success)
                close()
            } catch (e: Exception) {
                trySend(UploadResult.Error("Failed to upload image: ${e.message}"))
                close()
            }
            awaitClose { }
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
            val finalThumbnailUrl = when {
                newThumbnailLocalOrRemote.isNullOrBlank() -> null
                newThumbnailLocalOrRemote.startsWith("http") -> newThumbnailLocalOrRemote
                else -> {
                    val uri = newThumbnailLocalOrRemote.toUri()
                    val imageRef = storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
                    imageRef.putFile(uri).await()
                    imageRef.downloadUrl.await().toString()
                }
            }
            val updates = hashMapOf<String, Any>()
            newTitle?.let { updates["title"] = it }
            newLevelId?.let { updates["levelId"] = it }
            newOrder?.let { updates["order"] = it }
            finalThumbnailUrl?.let { updates["thumbnailUrl"] = it }
            updates["updatedAt"] = FieldValue.serverTimestamp()
            if (updates.isNotEmpty()) playlistDoc.update(updates).await()
            Result.success("Playlist updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun deleteFileByUrl(fileUrl: String): Boolean {
        val ref = storage.getReferenceFromUrl(fileUrl)
        ref.delete().await()
        return true
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
            var uploadedAudioUrl: String? = null
            if (!localAudioUrl.isNullOrBlank()) {
                val audioRef = storage.reference.child("audios/${System.currentTimeMillis()}")
                audioRef.putFile(localAudioUrl.toUri()).await()
                uploadedAudioUrl = audioRef.downloadUrl.await().toString()
                updates["audioUrl"] = uploadedAudioUrl
            }
            if (!uploadedAudioUrl.isNullOrBlank() && !remoteAudioUrl.isNullOrBlank()) {
                runCatching { deleteFileByUrl(remoteAudioUrl) }
            }
            var uploadedPdfUrl: String? = null
            if (!localPdfUrl.isNullOrBlank()) {
                val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
                pdfRef.putFile(localPdfUrl.toUri()).await()
                uploadedPdfUrl = pdfRef.downloadUrl.await().toString()
                updates["pdfUrl"] = uploadedPdfUrl
            }
            if (!uploadedPdfUrl.isNullOrBlank() && !remotePdfUrl.isNullOrBlank()) {
                runCatching { deleteFileByUrl(remotePdfUrl) }
            }
            newTitle?.let { updates["title"] = it }
            newOrder?.let { updates["order"] = it }
            updates["updatedAt"] = FieldValue.serverTimestamp()
            if (updates.isNotEmpty()) lessonDoc.update(updates).await()
            Result.success("lesson updated successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getUpdatedLessons(lastLessonSync: Long): List<LessonDto> {
        return try {
            val lessonsCollection = firestore.collection("lessons").whereGreaterThan("updatedAt", Date(lastLessonSync))
            val snapshot = lessonsCollection.get().await()
            snapshot.mapNotNull { it.toObject<LessonDto>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemoteLevels(): List<LevelDto> {
        return try {
            val snapshot = firestore.collection("levels").get().await()
            snapshot.mapNotNull { document ->
                LevelDto(
                    id = document.getString("id") ?: "",
                    title = document.getString("title") ?: "",
                    order = document.getLong("order")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLevelsVersion(): Long {
        val doc = firestore.collection("metadata").document("content_versions").get().await()
        return doc.getLong("levelsVersion") ?: 0
    }

    suspend fun getRemoteMotivationalMessages(): List<String> {
        return try {
            val doc = firestore.collection("motivational_messages").document("daily_messages").get().await()
            doc.get("messages") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLatestQuiz(): QuizDto? {
        return try {
            val snapshot = firestore.collection("weekly_quiz").orderBy("createdAt", Query.Direction.DESCENDING).limit(1).get().await()
            snapshot.documents.firstOrNull()?.toObject<QuizDto>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitLeaderboardEntry(entry: LeaderboardDto) {
        try {
            val docRef = firestore.collection("leaderboard").document(entry.telegramId.toString())
            val existing = docRef.get().await()
            if (existing.exists()) {
                val existingScore = existing.getLong("score") ?: 0
                if (entry.score > existingScore) docRef.set(entry).await()
            } else {
                docRef.set(entry).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun getLeaderboardFlow(): Flow<List<LeaderboardDto>> = callbackFlow {
        val listener = firestore.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .orderBy("answerTimestamp", Query.Direction.ASCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) trySend(snapshot.toObjects(LeaderboardDto::class.java))
            }
        awaitClose { listener.remove() }
    }
}
