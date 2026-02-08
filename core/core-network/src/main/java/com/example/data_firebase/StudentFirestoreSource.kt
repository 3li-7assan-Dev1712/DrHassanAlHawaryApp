package com.example.data_firebase

import android.util.Log
import androidx.core.net.toUri
import com.example.data_firebase.model.LessonDto
import com.example.data_firebase.model.LevelDto
import com.example.data_firebase.model.PlaylistDto
import com.example.data_firebase.model.StudentDto
import com.example.domain.use_cases.audios.UploadResult
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
    val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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
                TAG,
                "getAdminDataByTelegramId: name: ${dto.firstName} -- ${dto.membershipState} "
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
                .update("isConnectedToTelegram", isConnected)
                .await()
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


                val playlistToUpload = playlistDto.copy(
                    thumbnailUrl = downloadUrl
                )

                // Add the main group doc
                val groupDocRef = firestore.collection("playlists").document()
                groupDocRef.set(playlistToUpload).await()

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


    suspend fun getUpdatedLessons(lastLessonSync: Long): List<LessonDto> {
        return try {
            Log.d(TAG, "getPlaylists: last time: $lastLessonSync ${Date(lastLessonSync)}")
            val lessonsCollection = firestore.collection("lessons")
                .whereGreaterThan("updatedAt", Date(lastLessonSync))

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

}
