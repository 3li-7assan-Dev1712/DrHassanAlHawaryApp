package com.example.data_firebase

import com.example.domain.module.AppConfig
import com.example.domain.repository.ConfigRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ConfigRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ConfigRepository {

    override fun getAppConfig(): Flow<AppConfig> = callbackFlow {
        try {
            val subscription = firestore.collection("config")
                .document("app_config")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val config = snapshot.toObject(AppConfig::class.java)
                        if (config != null) {
                            trySend(config)
                        }
                    } else {
                        trySend(AppConfig())
                    }
                }
            awaitClose { subscription.remove() }

        } catch (e: Exception) {
            close(e)
        }

    }
}