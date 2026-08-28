package com.example.feature.share.engine

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

sealed interface ExportProgress {
    data class Encoding(val progress: Float) : ExportProgress
    data class Completed(val outputPath: String) : ExportProgress
    data class Failed(val message: String, val errorCode: Int) : ExportProgress
}

/**
 * Wraps Media3 Transformer. All Transformer calls happen on the main thread
 * (its default Looper) - only the extract/analyse/render steps elsewhere in
 * the pipeline hop to Dispatchers.Default.
 */
@UnstableApi
class ShareVideoExporter @Inject constructor() {

    private var activeTransformer: Transformer? = null

    fun export(
        context: Context,
        baseBitmapFile: File,
        clipFilePath: String,
        clipDurationMs: Long,
        waveformOverlay: WaveformOverlay,
        outputFile: File,
    ): Flow<ExportProgress> = callbackFlow {
        val imageItem = MediaItem.Builder()
            .setUri(Uri.fromFile(baseBitmapFile))
            .setImageDurationMs(clipDurationMs)
            .build()

        val videoItem = EditedMediaItem.Builder(imageItem)
            .setFrameRate(VIDEO_FRAME_RATE)
            .setDurationUs(clipDurationMs * 1_000L)
            .setEffects(
                Effects(
                    /* audioProcessors = */ emptyList(),
                    /* videoEffects = */ listOf(
                        Presentation.createForWidthAndHeight(
                            OUTPUT_WIDTH, OUTPUT_HEIGHT, Presentation.LAYOUT_SCALE_TO_FIT
                        ),
                        OverlayEffect(ImmutableList.of<TextureOverlay>(waveformOverlay)),
                    ),
                )
            )
            .build()

        val audioItem = EditedMediaItem.Builder(MediaItem.fromUri(clipFilePath)).build()

        val composition = Composition.Builder(
            EditedMediaItemSequence.Builder(videoItem).build(),
            EditedMediaItemSequence.Builder(audioItem).build(),
        ).build()

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                trySend(ExportProgress.Completed(outputFile.absolutePath))
                close()
            }

            override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                trySend(
                    ExportProgress.Failed(
                        exportException.message ?: "export failed",
                        exportException.errorCode,
                    )
                )
                close()
            }
        }

        val transformer = Transformer.Builder(context)
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .setEncoderFactory(
                DefaultEncoderFactory.Builder(context)
                    .setRequestedVideoEncoderSettings(
                        VideoEncoderSettings.Builder().setBitrate(VIDEO_BITRATE_BPS).build()
                    )
                    .build()
            )
            .addListener(listener)
            .build()

        activeTransformer = transformer
        transformer.start(composition, outputFile.absolutePath)

        val progressHolder = ProgressHolder()
        val handler = Handler(Looper.getMainLooper())
        val progressRunnable = object : Runnable {
            override fun run() {
                val state = transformer.getProgress(progressHolder)
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    trySend(ExportProgress.Encoding(progressHolder.progress / 100f))
                }
                handler.postDelayed(this, PROGRESS_POLL_MS)
            }
        }
        handler.post(progressRunnable)

        awaitClose {
            handler.removeCallbacks(progressRunnable)
            transformer.removeAllListeners()
            if (activeTransformer === transformer) activeTransformer = null
        }
    }.flowOn(Dispatchers.Main)

    /** §8: back pressed / trim changed mid-export - stop, no toast. */
    fun cancel() {
        activeTransformer?.cancel()
        activeTransformer = null
    }

    companion object {
        // 720x1280, not 1080x1920: speed over quality - this is the dominant
        // generation-time cost for this mostly-static (bitmap + waveform-strip
        // overlay) content, ~2.25x fewer pixels to GPU-composite and H.264-encode
        // per frame than the card's native 1080x1920 drawing resolution (which
        // ShareCardBitmapRenderer/WaveformOverlay still draw at - this just
        // downscales it at encode time). Still sharp enough on a phone screen.
        private const val OUTPUT_WIDTH = 720
        private const val OUTPUT_HEIGHT = 1280
        // 20, not 30: a pulsing waveform bar animation reads just as smooth at 20fps,
        // and dropping frame rate cuts the frame count that must be GPU-composited
        // and H.264-encoded by a third - without touching resolution/bitrate. Exposed
        // (not private) so WaveformOverlay's frame windowing always matches this exactly.
        // 60fps was considered and rejected: with a fixed target bitrate, file size is
        // governed by bitrate x duration, not frame count, so 60fps wouldn't meaningfully
        // grow the output - it would just make generation ~3x slower for no visible gain
        // on this content.
        const val VIDEO_FRAME_RATE = 20
        // Paired back down with the 720p resolution above - a higher bitrate mainly
        // buys sharper detail at higher resolutions; at 720p this is already plenty
        // for a mostly-static card, and keeping it here (instead of the 9 Mbps used
        // at 1080x1920) keeps output size and encode time down too.
        private const val VIDEO_BITRATE_BPS = 4_500_000
        private const val PROGRESS_POLL_MS = 400L
    }
}
