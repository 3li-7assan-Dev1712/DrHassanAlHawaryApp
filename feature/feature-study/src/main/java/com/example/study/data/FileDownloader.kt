package com.example.study.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

class FileDownloader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun downloadAudio(url: String, lessonId: String): String =
        download(
            url = url,
            subDir = "audio",
            fileName = "$lessonId.mp3"
        )

    suspend fun downloadPdf(url: String, lessonId: String): String =
        download(
            url = url,
            subDir = "pdf",
            fileName = "$lessonId.pdf"
        )

    private suspend fun download(
        url: String,
        subDir: String,
        fileName: String
    ): String = withContext(Dispatchers.IO) {

        val dir = File(context.filesDir, subDir)
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        if (file.exists()) return@withContext file.absolutePath

        val request = Request.Builder().url(url).build()
        val response = OkHttpClient().newCall(request).execute()

        response.body?.byteStream()?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        file.absolutePath
    }
}
