package com.example.data

import android.content.Context
import android.util.Log
import com.example.domain.use_cases.audios.DownloadResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val okHttpClient = OkHttpClient()

    private val TAG = "FileDownloader"

    fun downloadAudioWithProgress(url: String, audioId: String): Flow<DownloadResult> = flow {
        emit(DownloadResult.Progress(0))

        val dir = File(context.filesDir, "audio")
        if (!dir.exists()) dir.mkdirs()

        val urlHash = url.hashCode().toString(32)
        val fileName = "${audioId}_$urlHash.mp3"
        val file = File(dir, fileName)
        if (file.exists()) {
            emit(DownloadResult.Progress(100))
            emit(DownloadResult.Success(file.absolutePath))
            return@flow
        }

        if (file.exists()) {
            emit(DownloadResult.Progress(100))
            emit(DownloadResult.Success(file.absolutePath))
            return@flow
        }

        // Cleanup old versions
        dir.listFiles { _, name -> 
            name.startsWith("${audioId}_") || name == "$audioId.mp3" 
        }?.forEach { it.delete() }

        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                emit(DownloadResult.Error("Failed to download: ${response.code}"))
                return@flow
            }

            val body = response.body
            if (body == null) {
                emit(DownloadResult.Error("Response body is null"))
                return@flow
            }

            val totalBytes = body.contentLength()
            var bytesDownloaded = 0L

            body.byteStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var lastProgress = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead
                        
                        if (totalBytes > 0) {
                            val progress = (bytesDownloaded * 100 / totalBytes).toInt()
                            if (progress > lastProgress) {
                                lastProgress = progress
                                emit(DownloadResult.Progress(progress))
                            }
                        }
                    }
                }
            }

            emit(DownloadResult.Success(file.absolutePath))
        } catch (e: Exception) {
            if (file.exists()) file.delete()
            emit(DownloadResult.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun downloadAudio(url: String, lessonId: String): String =
        download(
            url = url,
            subDir = "audio",
            lessonId = lessonId,
            extension = "mp3"
        )

    suspend fun downloadPdf(url: String, lessonId: String): String =
        download(
            url = url,
            subDir = "pdf",
            lessonId = lessonId,
            extension = "pdf"
        )

    private suspend fun download(
        url: String,
        subDir: String,
        lessonId: String,
        extension: String
    ): String = withContext(Dispatchers.IO) {

        val dir = File(context.filesDir, subDir)
        if (!dir.exists()) dir.mkdirs()

        // Use a hash of the URL to detect if the content version has changed
        val urlHash = url.hashCode().toString(32)
        val fileName = "${lessonId}_$urlHash.$extension"
        val file = File(dir, fileName)

        // If this specific version already exists, just return it
        if (file.exists()) return@withContext file.absolutePath

        Log.d("FileDownloader", "Downloading new version of $subDir for lesson $lessonId")

        // Cleanup: remove any previous versions for this lesson to save space
        dir.listFiles { _, name -> 
            name.startsWith("${lessonId}_") || name == "$lessonId.$extension" 
        }?.forEach { 
            try {
                it.delete()
                Log.d(TAG, "download: successfully deleted file ${it.name}")
            } catch (e: Exception) { Log.e("FileDownloader", "Failed to delete old file", e) }
        }

        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw Exception("Failed to download file from $url: ${response.code}")
            }

            response.body?.byteStream()?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "download: donwloaded new file ${file.name}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e("FileDownloader", "Download failed for $url", e)
            if (file.exists()) file.delete()
            throw e
        }
    }
}
