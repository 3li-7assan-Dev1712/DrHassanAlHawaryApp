package com.example.feature.share.engine

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Owns cacheDir/share/ - paths, FileProvider uris, and the cleanup policy in
 * the spec's §7 (never delete the exported mp4 too early; the receiving app
 * reads the content URI asynchronously after our screen is gone).
 */
class ShareFileStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun rootDir(): File = File(context.cacheDir, "share").apply { if (!exists()) mkdirs() }

    /**
     * [extension] depends on the source codec: MediaMuxer's MP4 container only
     * accepts AAC ("m4a") for direct stream-copy; other codecs (e.g. MP3, which
     * is what this app's imported lectures actually are) get copied as raw
     * elementary-stream bytes instead, so the file keeps that codec's own extension.
     */
    fun clipFile(id: String, extension: String): File = File(rootDir(), "clip_$id.$extension")
    fun baseBitmapFile(id: String): File = File(rootDir(), "base_$id.png")
    fun exportFile(id: String): File = File(rootDir(), "share_$id.mp4")
    fun downloadTempFile(id: String): File = File(rootDir(), "download_$id.tmp")

    fun uriForFile(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    /** Called when leaving the preview: these are never handed to another app. */
    fun deleteIntermediates(id: String) {
        CLIP_EXTENSIONS.forEach { extension -> clipFile(id, extension).delete() }
        baseBitmapFile(id).delete()
        downloadTempFile(id).delete()
    }

    /** Called when leaving the preview and the export was never shared. */
    fun deleteExport(id: String) {
        exportFile(id).delete()
    }

    /** Called every time the preview screen opens. */
    fun sweepStale(maxAge: Long = TimeUnit.MINUTES.toMillis(10)) {
        val now = System.currentTimeMillis()
        rootDir().listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAge) file.delete()
        }
    }

    /** Called once from HiltApplication.onCreate - any handoff is long finished by now. */
    fun sweepAll() {
        rootDir().listFiles()?.forEach { it.delete() }
    }

    fun usableSpaceBytes(): Long = context.cacheDir.usableSpace

    companion object {
        private val CLIP_EXTENSIONS = listOf("m4a", "mp3")
    }
}
