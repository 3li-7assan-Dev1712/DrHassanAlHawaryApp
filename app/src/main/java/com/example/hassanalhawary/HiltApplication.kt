package com.example.hassanalhawary

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.data.di.ApplicationScope
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.hassanalhawary.core.util.LocaleForce
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.GetPlaylistsForLevelUseCase
import com.example.study.domain.use_case.SyncLessonsUseCase
import com.example.study.domain.use_case.SyncPlaylistsUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HiltApplication : Application(), DefaultLifecycleObserver {


    private val TAG = "HiltApplication"

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    @Inject
    lateinit var syncPlaylistsUseCase: SyncPlaylistsUseCase

    @Inject
    lateinit var getLevelsUseCase: GetLevelsUseCase

    @Inject
    lateinit var isUserLoggedInUseCase: IsUserLoggedInUseCase


    @Inject
    lateinit var getStudentDataUseCase: GetStudentDataUseCase

    @Inject
    lateinit var getPlaylistsForLevelUseCase: GetPlaylistsForLevelUseCase


    @Inject
    lateinit var syncLessonsUseCase: SyncLessonsUseCase


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleForce.wrap(newBase))
    }

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart: start app")
        appScope.launch {
            if (isUserLoggedInUseCase()) {
                getLevelsUseCase().collectLatest {
                    Log.d(TAG, "onStart: $it")
                    if (!it.isNullOrEmpty()) {
                        syncPlaylistsUseCase()
                        syncLessons()
                    }
                }
            }
        }

        appScope.launch {
            if (isUserLoggedInUseCase()) {
                getStudentDataUseCase().collectLatest {
                    FirebaseMessaging.getInstance()
                        .subscribeToTopic("all_users")
                    if (it != null) {
                        if (it.isCourseMember) {
                            FirebaseMessaging.getInstance()
                                .subscribeToTopic("student_broadcasts")
                            FirebaseMessaging.getInstance()
                                .subscribeToTopic("${it.batch}")

                        }
                    }
                }
            }
        }

    }

    private suspend fun syncLessons() {
        getPlaylistsForLevelUseCase("level_1").collectLatest {
            if (!it.isNullOrEmpty()) {
                syncLessonsUseCase()
            }
        }
    }
}