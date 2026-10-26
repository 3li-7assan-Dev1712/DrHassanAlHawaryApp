package app.netlify.devalihassan

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.netlify.devalihassan.core.util.LocaleForce
import com.example.data.di.ApplicationScope
import com.example.domain.diagnostics.ErrorReporter
import com.example.domain.use_cases.IsUserLoggedInUseCase
import com.example.domain.use_cases.ObserveAuthStateUseCase
import com.example.domain.use_cases.SyncContentUseCase
import com.example.domain.use_cases.study.GetStudentDataUseCase
import com.example.feature.share.engine.ShareFileStore
import com.example.study.domain.use_case.GetLevelsUseCase
import com.example.study.domain.use_case.GetPlaylistsForLevelUseCase
import com.example.study.domain.use_case.SyncLessonsUseCase
import com.example.study.domain.use_case.SyncPlaylistsUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    lateinit var observeAuthStateUseCase: ObserveAuthStateUseCase


    @Inject
    lateinit var getStudentDataUseCase: GetStudentDataUseCase

    @Inject
    lateinit var getPlaylistsForLevelUseCase: GetPlaylistsForLevelUseCase


    @Inject
    lateinit var syncLessonsUseCase: SyncLessonsUseCase

    @Inject
    lateinit var shareFileStore: ShareFileStore

    @Inject
    lateinit var syncContentUseCase: SyncContentUseCase


    /** The foreground's sync collectors, replaced on the next onStart instead of piling up. */
    private var foregroundJob: Job? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleForce.wrap(newBase))
    }

    override fun onCreate() {
        super<Application>.onCreate()
        CrashReporting.install()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // §7: any share-preview handoff from a previous session is long finished by now.
        appScope.launch(Dispatchers.IO) {
            shareFileStore.sweepAll()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart: start app")
        // onStart runs on every return to the foreground: without the cancel each one added
        // another set of these collectors and the syncs ran once per earlier visit.
        foregroundJob?.cancel()
        foregroundJob = appScope.launch {
            observeAuthStateUseCase().collectLatest { isLoggedIn ->
                if (isLoggedIn) {
                    // Series, and what changed in the cached content (e.g. items moved into a series).
                    launch {
                        runCatching { syncContentUseCase() }
                            .onFailure {
                                Log.e(TAG, "content sync failed", it)
                                ErrorReporter.record("content sync", it)
                            }
                    }

                    launch {
                        getLevelsUseCase().collectLatest {
                            Log.d(TAG, "onStart: $it")
                            if (!it.isNullOrEmpty()) {
                                syncPlaylistsUseCase()
                                syncLessons()
                            }
                        }
                    }

                    launch {
                        getStudentDataUseCase().collectLatest {
                            CrashReporting.setCourseMember(it?.isCourseMember == true)
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