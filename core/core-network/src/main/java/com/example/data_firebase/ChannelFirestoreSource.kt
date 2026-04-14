package com.example.data_firebase

import com.example.domain.module.Channel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChannelFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) {
    private val channelsCollection = firestore.collection("channels")

    fun getChannels(): Flow<List<Channel>> = callbackFlow {
        val listener = channelsCollection
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val channels = snapshot.documents.mapNotNull { doc ->
                        try {
                            Channel(
                                channelId = doc.getString("channelId") ?: "",
                                batch = doc.getString("batch") ?: "",
                                order = doc.getLong("order")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(channels)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addChannel(channel: Channel): Result<Unit> {
        return try {
            val data = hashMapOf(
                "channelId" to channel.channelId,
                "batch" to channel.batch,
                "order" to channel.order
            )
            functions.getHttpsCallable("addChannel").call(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChannel(channelId: String): Result<Unit> {
        return try {
            val data = hashMapOf("channelId" to channelId)
            functions.getHttpsCallable("removeChannel").call(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
