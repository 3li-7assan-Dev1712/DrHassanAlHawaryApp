package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.LeaderboardDto
import com.example.data_firebase.model.LessonDto
import com.example.data_firebase.model.LevelDto
import com.example.data_firebase.model.PlaylistDto
import com.example.data_firebase.model.QuizDto
import com.example.data_firebase.model.StudentDto
import com.example.domain.use_cases.audios.UploadResult
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class StudentFirestoreSource @Inject constructor(
    val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val functions: FirebaseFunctions
) {

    private val TAG = "StudentFirestoreSource"
    private val studentsCollection = firestore.collection("students")
    private val adminCollection = firestore.collection("admins")
    private val playlistsCollection = firestore.collection("playlists")
    private val lessonsCollection = firestore.collection("lessons")

    private fun DocumentSnapshot.toPlaylistDtoSafe(): PlaylistDto? {
        return try {
            this.toObject<PlaylistDto>()?.copy(id = this.id)
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Standard deserialization failed for playlist ${this.id}, trying manual mapping",
                e
            )
            try {
                val publishDate = try {
                    this.getTimestamp("publishDate")
                } catch (ex: Exception) {
                    this.getLong("publishDate")?.let { Timestamp(Date(it)) }
                }

                val updatedAt = try {
                    this.getTimestamp("updatedAt")
                } catch (ex: Exception) {
                    this.getLong("updatedAt")?.let { Timestamp(Date(it)) }
                }

                PlaylistDto(
                    isDeleted = this.getBoolean("isDeleted") ?: this.getBoolean("deleted") ?: false,
                    id = this.id,
                    title = this.getString("title") ?: "",
                    levelId = this.getString("levelId") ?: "",
                    order = this.getLong("order")?.toInt() ?: 0,
                    thumbnailUrl = this.getString("thumbnailUrl") ?: "",
                    publishDate = publishDate,
                    updatedAt = updatedAt
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Manual mapping failed for playlist ${this.id}", ex)
                null
            }
        }
    }

    private fun DocumentSnapshot.toLessonDtoSafe(): LessonDto? {
        return try {
            this.toObject<LessonDto>()?.copy(id = this.id)
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Standard deserialization failed for lesson ${this.id}, trying manual mapping",
                e
            )
            try {
                LessonDto(
                    id = this.id,
                    playlistId = this.getString("playlistId") ?: "",
                    order = this.getLong("order")?.toInt() ?: 0,
                    title = this.getString("title") ?: "",
                    audioUrl = this.getString("audioUrl") ?: "",
                    duration = this.getLong("duration") ?: 0L,
                    pdfUrl = this.getString("pdfUrl") ?: "",
                    publishDate = this.getLong("publishDate") ?: 0L,
                    updatedAt = this.getLong("updatedAt") ?: 0L,
                    isDeleted = this.getBoolean("isDeleted") ?: this.getBoolean("deleted") ?: false
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Manual mapping failed for lesson ${this.id}", ex)
                null
            }
        }
    }

    suspend fun storeStudent(studentDto: StudentDto) {
        try {
            studentsCollection.document(studentDto.id.toString()).set(studentDto).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing student data in Firestore", e)
            throw e
        }
    }


    suspend fun checkMembership(uid: String, telegramId: Long): Result<Unit> {
        return try {
            val data = hashMapOf(
                "uid" to uid,
                "telegramId" to telegramId
            )
            functions
                .getHttpsCallable("checkMembership")
                .call(data)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "checkMembership error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getStudentDataById(uid: String): StudentDto? {
        return try {
            val document = studentsCollection.document(uid).get().await()
            if (document.data == null) {
                null
            } else {
                StudentDto(
                    id = document.getLong("id") ?: 0,
                    firstName = document.getString("firstName") ?: "",
                    lastName = document.getString("lastName") ?: "",
                    username = document.getString("username") ?: "",
                    photoUrl = document.getString("photoUrl") ?: "",
                    isChannelMember = document.getBoolean("isChannelMember") ?: false,
                    membershipState = document.getString("membershipState") ?: "",
                    isConnectedToTelegram = document.getBoolean("isConnectedToTelegram") ?: false,
                    currentLevelId = document.getString("currentLevelId") ?: "1"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting student by telegramId: $uid", e)
            null
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
            dto
        } catch (e: Exception) {
            Log.e(TAG, "Error getting admin by telegramId: $telegramId", e)
            null
        }
    }

    suspend fun updateStudentConnectionStatus(telegramId: Long, isConnected: Boolean) {
        try {
            studentsCollection.document(telegramId.toString())
                .update("isConnectedToTelegram", isConnected).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating student connection status for id: $telegramId", e)
            throw e
        }
    }

    suspend fun getUpdatedPlaylists(lastSyncTime: Long): List<PlaylistDto> {
        return try {
            val snapshot = playlistsCollection
                .whereGreaterThan("updatedAt", Timestamp(Date(lastSyncTime)))
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toPlaylistDtoSafe() }
        } catch (e: Exception) {
            Log.e(TAG, "getUpdatedPlaylists failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRemotePlaylistForLevel(levelId: String): List<PlaylistDto> {
        return try {
            val snapshot = playlistsCollection
                .whereEqualTo("levelId", levelId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.mapNotNull { it.toPlaylistDtoSafe() }
        } catch (e: Exception) {
            Log.d(TAG, "getRemotePlaylistForLevel: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRemoteLessonsForPlaylist(playlistId: String): List<LessonDto> {
        return try {
            val snapshot = lessonsCollection
                .whereEqualTo("playlistId", playlistId)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.mapNotNull { it.toLessonDtoSafe() }
        } catch (e: Exception) {
            Log.d(TAG, "getRemoteLessonsForPlaylist: ${e.message}")
            emptyList()
        }
    }

    suspend fun getRemotePlaylistById(playlistId: String): PlaylistDto? {
        return try {
            val doc = playlistsCollection.document(playlistId).get().await()
            doc.toPlaylistDtoSafe()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRemoteLessonById(lessonId: String): LessonDto? {
        return try {
            val doc = lessonsCollection.document(lessonId).get().await()
            doc.toLessonDtoSafe()
        } catch (e: Exception) {
            null
        }
    }


    fun uploadPlaylist(playlistDto: PlaylistDto): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))
        try {
            val uri = playlistDto.thumbnailUrl.toUri()
            val imageRef = storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
            val downloadUrl = imageRef.putFile(uri).await().storage.downloadUrl.await().toString()

            val newDocRef = playlistsCollection.document()
            val now = Timestamp.now()
            val finalDto = playlistDto.copy(
                id = newDocRef.id,
                thumbnailUrl = downloadUrl,
                publishDate = now,
                updatedAt = now,
                isDeleted = false
            )

            newDocRef.set(finalDto).await()
            trySend(UploadResult.Success)
            close()
        } catch (e: Exception) {
            trySend(UploadResult.Error("Failed to upload: ${e.message}"))
            close()
        }
        awaitClose { }
    }

    fun addLesson(lessonDto: LessonDto, playlistId: String): Flow<UploadResult> = callbackFlow {
        trySend(UploadResult.Progress(0))
        try {
            val audioUri = lessonDto.audioUrl.toUri()
            val audioRef = storage.reference.child("audios/${System.currentTimeMillis()}")
            val audioDownloadUrl =
                audioRef.putFile(audioUri).await().storage.downloadUrl.await().toString()

            trySend(UploadResult.Progress(50))

            val pdfUri = lessonDto.pdfUrl.toUri()
            val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
            val pdfDownloadUrl =
                pdfRef.putFile(pdfUri).await().storage.downloadUrl.await().toString()

            val newDocRef = lessonsCollection.document()
            val now = Timestamp.now()
            val finalDto = lessonDto.copy(
                id = newDocRef.id,
                playlistId = playlistId,
                audioUrl = audioDownloadUrl,
                pdfUrl = pdfDownloadUrl,
                publishDate = now.toDate().time,
                updatedAt = now.toDate().time,
                isDeleted = false
            )
            newDocRef.set(finalDto).await()
            trySend(UploadResult.Success)
            close()
        } catch (e: Exception) {
            trySend(UploadResult.Error("Failed to upload: ${e.message}"))
            close()
        }
        awaitClose { }
    }


    suspend fun updatePlaylist(
        playlistId: String,
        newTitle: String? = null,
        newLevelId: String? = null,
        newOrder: Int? = null,
        newThumbnailLocalOrRemote: String? = null
    ): Result<String> {
        return try {
            val updates = mutableMapOf<String, Any>()
            newTitle?.let { updates["title"] = it }
            newLevelId?.let { updates["levelId"] = it }
            newOrder?.let { updates["order"] = it }

            if (!newThumbnailLocalOrRemote.isNullOrBlank() && !newThumbnailLocalOrRemote.startsWith(
                    "http"
                )
            ) {
                val uri = newThumbnailLocalOrRemote.toUri()
                val imageRef =
                    storage.reference.child("playlists/${System.currentTimeMillis()}.jpg")
                imageRef.putFile(uri).await()
                updates["thumbnailUrl"] = imageRef.downloadUrl.await().toString()
            } else if (!newThumbnailLocalOrRemote.isNullOrBlank()) {
                updates["thumbnailUrl"] = newThumbnailLocalOrRemote
            }

            updates["updatedAt"] = Timestamp.now()
            playlistsCollection.document(playlistId).update(updates).await()
            Result.success("Playlist updated")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateLesson(
        lessonId: String,
        newTitle: String? = null,
        newOrder: Int? = null,
        localAudioUrl: String? = null,
        localPdfUrl: String? = null
    ): Result<String> {
        return try {
            val updates = mutableMapOf<String, Any>()
            newTitle?.let { updates["title"] = it }
            newOrder?.let { updates["order"] = it }

            if (!localAudioUrl.isNullOrBlank()) {
                val audioRef = storage.reference.child("audios/${System.currentTimeMillis()}")
                audioRef.putFile(localAudioUrl.toUri()).await()
                updates["audioUrl"] = audioRef.downloadUrl.await().toString()
            }

            if (!localPdfUrl.isNullOrBlank()) {
                val pdfRef = storage.reference.child("pdf/${System.currentTimeMillis()}")
                pdfRef.putFile(localPdfUrl.toUri()).await()
                updates["pdfUrl"] = pdfRef.downloadUrl.await().toString()
            }

            updates["updatedAt"] = System.currentTimeMillis()
            lessonsCollection.document(lessonId).update(updates).await()
            Result.success("Lesson updated")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUpdatedLessons(lastSyncTime: Long): List<LessonDto> {
        return try {
            val snapshot = lessonsCollection
                .whereGreaterThan("updatedAt", lastSyncTime)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toLessonDtoSafe() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRemoteLevels(): List<LevelDto> {
        return try {
            val snapshot = firestore.collection("levels")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.mapNotNull { it.toObject<LevelDto>() }
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
            val doc = firestore.collection("motivational_messages").document("daily_messages").get()
                .await()
            doc.get("messages") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLatestQuiz(): QuizDto? {
        return try {
            val snapshot = firestore.collection("weekly_quiz")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            snapshot.documents.firstOrNull()?.toObject<QuizDto>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun submitLeaderboardEntry(entry: LeaderboardDto) {
        try {
            val docRef = firestore.collection("leaderboard").document(entry.telegramId.toString())
            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                val existingScore = existingDoc.getLong("score") ?: 0
                if (entry.score > existingScore) {
                    docRef.set(entry).await()
                }
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
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(LeaderboardDto::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitQuizAndPromote(answers: List<Any>): String {
        val data = hashMapOf("answers" to answers)
        val result = functions.getHttpsCallable("submitWeeklyQuizAndPromote").call(data).await()
        val response = result.data as Map<*, *>
        if (response["success"] != true || response["passed"] != true) {
            throw Exception("Quiz not passed")
        }
        return response["newLevelId"] as String
    }

    suspend fun getAdmins(): Result<List<Map<String, Any>>> {
        return try {
            val result = functions.getHttpsCallable("getAdmins").call().await()
            val data = result.data as? Map<*, *>
            val admins = data?.get("admins") as? List<Map<String, Any>> ?: emptyList()
            Result.success(admins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAdmin(email: String, role: String): Result<Unit> {
        return try {
            functions.getHttpsCallable("setUserRole")
                .call(mapOf("email" to email, "role" to role))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeAdmin(uid: String): Result<Unit> {
        return try {
            functions.getHttpsCallable("removeAdmin").call(mapOf("uid" to uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
