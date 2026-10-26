package app.netlify.devalihassan

import com.example.domain.diagnostics.ErrorReporter
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.InterruptedIOException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException
import javax.net.ssl.SSLException

/**
 * What Crashlytics knows besides the stack trace: the signed-in user, the screen, whether the
 * user is a course member, and the caught errors that [ErrorReporter] receives.
 *
 * Collection itself is switched per build type in the manifest (firebase_crashlytics_collection_enabled):
 * off in debug, on in release. While it is off nothing is sent.
 */
object CrashReporting {

    private val crashlytics get() = FirebaseCrashlytics.getInstance()

    fun install() {
        // The Firebase uid, so a user's "the app closes" email can be matched to their crashes.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            crashlytics.setUserId(auth.currentUser?.uid.orEmpty())
        }
        ErrorReporter.install { where, error ->
            val signedIn = FirebaseAuth.getInstance().currentUser != null
            if (!isExpectedError(error, signedIn)) {
                crashlytics.log("$where failed")
                crashlytics.recordException(error)
            }
        }
    }

    /** The route pattern ("article/{id}"), never its arguments. The log lines show the path to a crash. */
    fun setScreen(route: String?) {
        crashlytics.setCustomKey("screen", route.orEmpty())
        crashlytics.log("screen $route")
    }

    fun setCourseMember(isCourseMember: Boolean) {
        crashlytics.setCustomKey("course_member", isCourseMember)
    }
}

/**
 * Failures that are not bugs and would only bury the real ones (Crashlytics keeps the last 8
 * caught errors of a session): no network, a coroutine cancelled on purpose, and rules
 * rejecting a listener that was still open when the user signed out.
 */
internal fun isExpectedError(error: Throwable, signedIn: Boolean): Boolean =
    generateSequence(error) { it.cause }.any { cause ->
        when (cause) {
            is CancellationException,
            is FirebaseNetworkException,
            is UnknownHostException,
            is InterruptedIOException,
            is SocketException,
            is SSLException -> true

            is FirebaseFirestoreException -> when (cause.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> true

                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                FirebaseFirestoreException.Code.UNAUTHENTICATED -> !signedIn

                else -> false
            }

            else -> false
        }
    }
