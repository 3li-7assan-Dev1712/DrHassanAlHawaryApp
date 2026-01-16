package com.example.data_firebase.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.data_firebase.AudioFirestoreSource
import com.example.data_firebase.FirebaseArticlesSource
import com.example.data_firebase.GoogleAuthUiClient
import com.example.data_firebase.ImageFirestoreSource
import com.example.data_firebase.VideoFirestoreSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseRealtimeDatabase(): FirebaseDatabase {
        // Use getInstance() for the default database instance
        return FirebaseDatabase.getInstance("https://dr-hassan-al-hawary-default-rtdb.firebaseio.com/")
    }


    @Provides
    @Singleton
    fun provideFirebaseAudioFirestoreSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
    ): AudioFirestoreSource {
        return AudioFirestoreSource(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideFirebaseImageFirestoreSource(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage,
    ): ImageFirestoreSource {
        return ImageFirestoreSource(firestore, storage)
    }


    @Provides
    @Singleton
    fun provideFirebaseVideoFirestoreSource(
        firestore: FirebaseFirestore,
    ): VideoFirestoreSource {
        return VideoFirestoreSource(firestore)
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseArticlesSource(firestore: FirebaseFirestore): FirebaseArticlesSource {
        return FirebaseArticlesSource(firestore)
    }


    @Singleton
    @Provides
    fun provideCredentialManager(
        @ApplicationContext context: Context
    ): CredentialManager = CredentialManager.create(context)

    @Singleton
    @Provides
    fun provideGoogleAuthUiClient(
        @ApplicationContext context: Context,
        credentialManager: CredentialManager,
        firebaseAuth: FirebaseAuth
    ): GoogleAuthUiClient {
        return GoogleAuthUiClient(context, credentialManager, firebaseAuth)
    }
}