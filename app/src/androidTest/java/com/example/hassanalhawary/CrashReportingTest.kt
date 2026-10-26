package app.netlify.devalihassan

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreException.Code
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.CancellationException

/** On a device: Firebase's exception classes call Android APIs when they are created. */
@RunWith(AndroidJUnit4::class)
class CrashReportingTest {

    @Test
    fun offlineAndCancellation_areNotReported() {
        assertTrue(isExpectedError(UnknownHostException("firestore.googleapis.com"), signedIn = true))
        assertTrue(isExpectedError(SocketTimeoutException(), signedIn = true))
        assertTrue(isExpectedError(FirebaseNetworkException("offline"), signedIn = true))
        assertTrue(isExpectedError(FirebaseFirestoreException("offline", Code.UNAVAILABLE), signedIn = true))
        assertTrue(isExpectedError(CancellationException(), signedIn = true))
    }

    @Test
    fun anExpectedCauseAnywhereInTheChain_isNotReported() {
        val wrapped = IllegalStateException("sync failed", IOException("read", SocketTimeoutException()))
        assertTrue(isExpectedError(wrapped, signedIn = true))
    }

    @Test
    fun rulesRejection_isReportedOnlyWhileSignedIn() {
        val denied = FirebaseFirestoreException("denied", Code.PERMISSION_DENIED)
        assertFalse(isExpectedError(denied, signedIn = true))
        assertTrue(isExpectedError(denied, signedIn = false))
    }

    @Test
    fun bugs_areReported() {
        assertFalse(isExpectedError(IllegalArgumentException("bad field"), signedIn = true))
        assertFalse(isExpectedError(IOException("HTTP 404"), signedIn = true))
        assertFalse(isExpectedError(FirebaseFirestoreException("index", Code.FAILED_PRECONDITION), signedIn = true))
    }
}
