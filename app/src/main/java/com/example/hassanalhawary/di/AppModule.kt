package com.example.hassanalhawary.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.hassanalhawary.core.util.GoogleAuthUiClient
import com.example.hassanalhawary.core.util.NetworkMonitor
import com.example.hassanalhawary.data.local.AppDatabase
import com.example.hassanalhawary.data.local.AudioDao
import com.example.hassanalhawary.data.remote.FirebaseAudioSource
import com.example.hassanalhawary.domain.repository.ArticlesRepository
import com.example.hassanalhawary.domain.repository.ArticlesRepositoryImpl
import com.example.hassanalhawary.domain.repository.AudiosRepository
import com.example.hassanalhawary.domain.repository.AudiosRepositoryImpl
import com.example.hassanalhawary.domain.repository.AuthRepository
import com.example.hassanalhawary.domain.repository.AuthRepositoryImpl
import com.example.hassanalhawary.domain.repository.WisdomRepository
import com.example.hassanalhawary.domain.repository.WisdomRepositoryImpl
import com.example.hassanalhawary.domain.use_cases.GetWisdomOfTheDayUseCase
import com.example.hassanalhawary.domain.use_cases.LoginWithEmailAndPasswordUseCase
import com.example.hassanalhawary.domain.use_cases.LoginWithGoogleUseCase
import com.example.hassanalhawary.domain.use_cases.RegisterNewUserWithEmailPasswordUseCase
import com.google.firebase.auth.FirebaseAuth
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
object AppModule {


    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()


    // provide the network monitor class
    @Singleton
    @Provides
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor = NetworkMonitor(context)

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()


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

    @Singleton
    @Provides
    fun provideAuthRepository(
        googleAuthUiClient: GoogleAuthUiClient,
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(googleAuthUiClient, firebaseAuth)
    }

    @Singleton
    @Provides
    fun provideAudiosRepository(
        firebaseStorage: FirebaseStorage,
        audioDao: AudioDao,
        firebaseAudioSource: FirebaseAudioSource
    ): AudiosRepository {
        return AudiosRepositoryImpl(firebaseStorage, audioDao, firebaseAudioSource)
    }

    @Singleton
    @Provides
    fun provideFirebaseAudioSource(firebaseStorage: FirebaseStorage): FirebaseAudioSource =
        FirebaseAudioSource(firebaseStorage)

    @Singleton
    @Provides
    fun provideArticlesRepository(
        firebaseDb: FirebaseFirestore
    ): ArticlesRepository {
        return ArticlesRepositoryImpl(firebaseDb)
    }

    @Singleton
    @Provides
    fun provideLoginWithGoogleUseCase(
        authRepository: AuthRepository
    ): LoginWithGoogleUseCase {
        return LoginWithGoogleUseCase(authRepository)
    }

    @Singleton
    @Provides
    fun provideLoginWithEmailAndPasswordUseCase(
        authRepository: AuthRepository
    ): LoginWithEmailAndPasswordUseCase {
        return LoginWithEmailAndPasswordUseCase(authRepository)
    }

    @Provides
    fun provideWisdomRepository(): WisdomRepository {
        return WisdomRepositoryImpl()
    }

    @Provides
    fun provideGetWisdomOfTheDayUseCase(
        wisdomRepository: WisdomRepository,
        networkMonitor: NetworkMonitor
    ): GetWisdomOfTheDayUseCase {
        return GetWisdomOfTheDayUseCase(wisdomRepository, networkMonitor)
    }

    @Singleton
    @Provides
    fun provideRegisterNewUserWithEmailPasswordUseCase(
        authRepository: AuthRepository
    ): RegisterNewUserWithEmailPasswordUseCase {
        return RegisterNewUserWithEmailPasswordUseCase(authRepository)
    }


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "hassan_al_hawary_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAudioDao(appDatabase: AppDatabase): AudioDao {
        return appDatabase.audioDao()
    }


}
