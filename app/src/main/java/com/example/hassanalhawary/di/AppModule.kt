package com.example.hassanalhawary.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.hassanalhawary.core.util.GoogleAuthUiClient
import com.example.hassanalhawary.domain.repository.AuthRepository
import com.example.hassanalhawary.domain.repository.AuthRepositoryImpl
import com.example.hassanalhawary.domain.use_cases.LoginWithEmailAndPasswordUseCase
import com.example.hassanalhawary.domain.use_cases.LoginWithGoogleUseCase
import com.example.hassanalhawary.domain.use_cases.RegisterNewUserWithEmailPasswordUseCase
import com.google.firebase.auth.FirebaseAuth
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

    @Singleton
    @Provides
    fun provideRegisterNewUserWithEmailPasswordUseCase(
        authRepository: AuthRepository
    ): RegisterNewUserWithEmailPasswordUseCase {
        return RegisterNewUserWithEmailPasswordUseCase(authRepository)
    }
}
