package com.example.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
