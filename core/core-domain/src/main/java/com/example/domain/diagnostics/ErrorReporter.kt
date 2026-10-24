package com.example.domain.diagnostics

/**
 * Errors the app catches and recovers from but that still point at a bug: a document that no
 * longer maps, a listener the rules reject, a sync or export that failed.
 *
 * The user app plugs Crashlytics in at startup (CrashReporting). Without a sink (the admin app,
 * unit tests) reports are dropped, so data and network code can call it without knowing which
 * app it runs in.
 */
object ErrorReporter {

    @Volatile
    private var sink: ((where: String, error: Throwable) -> Unit)? = null

    fun install(sink: (where: String, error: Throwable) -> Unit) {
        this.sink = sink
    }

    /** [where] names what failed ("content sync", "audio mapping abc123"); it is logged with the error. */
    fun record(where: String, error: Throwable) {
        sink?.invoke(where, error)
    }
}
