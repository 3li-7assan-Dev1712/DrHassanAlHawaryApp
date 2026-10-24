package com.example.data_firebase

import com.example.domain.module.AppConfig
import com.example.domain.repository.ConfigRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConfigRepository {

    private val configDocument get() = firestore.collection("config").document("app_config")

    override fun getAppConfig(): Flow<AppConfig> = callbackFlow {
        try {
            val subscription = configDocument
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // On logout, we might get a permission denied error.
                        // We close the flow gracefully in that case.
                        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            close()
                        } else {
                            close(error)
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        // Field by field, never throwing (appConfigFrom): this runs on the main thread.
                        trySend(appConfigFrom(snapshot.data))
                    } else {
                        trySend(AppConfig())
                    }
                }
            awaitClose { subscription.remove() }

        } catch (e: Exception) {
            close(e)
        }

    }

    override suspend fun fetchAppConfig(): AppConfig? = try {
        appConfigFrom(configDocument.get(Source.SERVER).await().data)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
