package com.example.feature.share.presentation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.ExportException
import com.example.core.ui.R
import com.example.domain.module.Audio
import com.example.domain.use_cases.audios.DownloadAudioUseCase
import com.example.domain.use_cases.audios.DownloadResult
import com.example.domain.use_cases.audios.GetAudioByUrlUseCase
import com.example.feature.share.domain.ShareBackgroundSource
import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.domain.ShareClip
import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.engine.AudioClipExtractor
import com.example.feature.share.engine.ExportProgress
import com.example.feature.share.engine.ShareCardBitmapRenderer
import com.example.feature.share.engine.ShareCardSpec
import com.example.feature.share.engine.ShareFileStore
import com.example.feature.share.engine.ShareVideoExporter
import com.example.feature.share.engine.WaveformAnalyzer
import com.example.feature.share.engine.WaveformOverlay
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class SharePreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val shareFileStore: ShareFileStore,
    private val audioClipExtractor: AudioClipExtractor,
    private val waveformAnalyzer: WaveformAnalyzer,
    private val bitmapRenderer: ShareCardBitmapRenderer,
    private val videoExporter: ShareVideoExporter,
    private val getAudioByUrlUseCase: GetAudioByUrlUseCase,
    private val downloadAudioUseCase: DownloadAudioUseCase,
) : ViewModel() {

    /** Identifies this share session's temp files - stable across rotation since the ViewModel survives it. */
    private val shareId = UUID.randomUUID().toString()
    private val spec = ShareCardSpec.default()

    private val audioUrl = savedStateHandle.get<String>(ARG_AUDIO_URL).orEmpty()
    private val navArgLocalFilePath = savedStateHandle.get<String>(ARG_LOCAL_FILE_PATH)?.takeIf { it.isNotBlank() }

    /** Starts as whatever the detail screen already knew (often nothing yet); gets
     * upgraded in place - without the user ever noticing a reload - the moment
     * [observeLocalAudioFile] sees a locally cached copy appear in Room, whether
     * that's from this screen's own fallback download or one the detail screen
     * kicked off silently before the user ever tapped Share. */
    private var resolvedLocalFilePath: String? = navArgLocalFilePath
    private var hasStartedFallbackDownload = false

    private val rawTitle = savedStateHandle.get<String>(ARG_TITLE).orEmpty()
    private val category = savedStateHandle.get<String>(ARG_CATEGORY)?.takeIf { it.isNotBlank() }
    private val totalTrackDurationMs = (savedStateHandle.get<Long>(ARG_TOTAL_DURATION_MS) ?: 0L).coerceAtLeast(0L)
    private val requestedStartMs = savedStateHandle.get<Long>(ARG_START_MS) ?: 0L

    // The app's own display name/brand, not the institute's formal name - pairs
    // with the Sheikh's photo in the circle logo, matching how the rest of the
    // app brands itself (AudioDetailScreen etc.), rather than an "official
    // institute seal" look.
    private val instituteName = context.getString(R.string.app_name)

    // §8: empty/null title falls back to the category, then to the institute name.
    private val effectiveTitle = rawTitle.takeIf { it.isNotBlank() }
        ?: category
        ?: instituteName

    // Plays the ORIGINAL source directly (never the extracted clip) so the user
    // can freely scrub/preview any part of the track before generation ever runs.
    private val exoPlayer: ExoPlayer by lazy { ExoPlayer.Builder(context).build() }

    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState = _uiState.asStateFlow()

    /** One-shot: the screen collects this and fires the actual share/chooser intent. */
    private val _shareIntentEvent = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val shareIntentEvent = _shareIntentEvent.asSharedFlow()

    private var extractionJob: Job? = null
    private var positionJob: Job? = null

    /** Set once the user taps Share; the instant the export lands, we fire the intent. */
    private var shareWhenReady = false

    /** §7: once true, never delete the export file ourselves - the receiving app may
     * still be reading the content URI asynchronously. This does NOT gate re-sharing:
     * cancelling the chooser and tapping Share again must reopen it, same as every
     * other Android share button. */
    private var hasBeenShared = false

    /** §8: rapid-double-tap guard for [fireShareIntent] - short-lived, unlike [hasBeenShared]. */
    private var lastShareIntentFiredAtMs = 0L

    init {
        shareFileStore.sweepStale()
        loadOverviewEnvelope()
        preparePlayer()
        listenToPlayer()
        observeLocalAudioFile()
    }

    /** Reactively picks up a local copy of this audio the moment Room has one -
     * whether it was already downloaded, finished downloading in the background
     * while the user was still on the detail screen, or has to be started here as
     * a fallback (e.g. the detail screen's silent prefetch hadn't started yet). */
    private fun observeLocalAudioFile() {
        viewModelScope.launch {
            getAudioByUrlUseCase(audioUrl).collect { audio ->
                val local = audio?.localFilePath?.takeIf { audio.isDownloaded && File(it).exists() }
                if (local != null && local != resolvedLocalFilePath) {
                    resolvedLocalFilePath = local
                    onLocalFileReady(local)
                } else if (audio != null && !audio.isDownloaded && !hasStartedFallbackDownload) {
                    hasStartedFallbackDownload = true
                    startFallbackDownload(audio)
                }
            }
        }
    }

    private fun startFallbackDownload(audio: Audio) {
        viewModelScope.launch {
            downloadAudioUseCase(audio).collect { result ->
                if (result is DownloadResult.Progress) {
                    _uiState.update { it.copy(downloadProgressPercent = result.percentage) }
                }
                // Success/Error both surface through the same getAudioByUrlUseCase Room
                // flow already being collected in observeLocalAudioFile() - no extra
                // handling needed here beyond clearing the progress affordance.
                if (result is DownloadResult.Success || result is DownloadResult.Error) {
                    _uiState.update { it.copy(downloadProgressPercent = null) }
                }
            }
        }
    }

    /** Upgrades the still-running preview in place - no reload, no flicker: the
     * waveform sharpens from whatever it was showing (synthetic or remote-decoded)
     * and playback seamlessly continues from the same position on the local file. */
    private fun onLocalFileReady(path: String) {
        loadOverviewEnvelope()

        val wasPlaying = exoPlayer.isPlaying
        val position = exoPlayer.currentPosition
        exoPlayer.setMediaItem(MediaItem.fromUri(path))
        exoPlayer.prepare()
        exoPlayer.seekTo(position)
        if (wasPlaying) exoPlayer.play()
    }

    private fun buildInitialState(): SharePreviewUiState {
        // §8: audio shorter than the window -> clip is the whole track, never padded.
        val windowMs = if (totalTrackDurationMs in 1 until DEFAULT_WINDOW_MS) totalTrackDurationMs else DEFAULT_WINDOW_MS
        // §8: startMs + window > duration -> clamp startMs back so the window fits.
        val clampedStart = requestedStartMs.coerceIn(0L, (totalTrackDurationMs - windowMs).coerceAtLeast(0L))

        val content = ShareCardContent(
            title = effectiveTitle,
            category = category,
            instituteName = instituteName,
            background = ShareBackgroundSource.Gradient,
            logoResId = R.drawable.dr_hassan_image,
        )

        // Instant first paint: a natural-looking placeholder waveform rather than an
        // empty bar or a blocking spinner - sharpens into the real decode moments
        // later (see loadOverviewEnvelope/onLocalFileReady), almost never noticeable
        // once the source is already local.
        val placeholder = waveformAnalyzer.placeholderOverview()

        return SharePreviewUiState(
            audioUrl = audioUrl,
            localFilePath = navArgLocalFilePath,
            totalTrackDurationMs = totalTrackDurationMs,
            startMs = clampedStart,
            clipDurationMs = windowMs,
            content = content,
            overviewEnvelope = placeholder,
            clipEnvelope = sliceEnvelope(placeholder, clampedStart, clampedStart + windowMs),
            // §8: track shorter than ~5s -> hide the share action entirely.
            isTooShortToShare = totalTrackDurationMs in 1 until MIN_SHAREABLE_DURATION_MS,
        )
    }

    private fun preparePlayer() {
        val source = resolvedLocalFilePath ?: audioUrl
        exoPlayer.setMediaItem(MediaItem.fromUri(source))
        exoPlayer.prepare()
    }

    private fun loadOverviewEnvelope() {
        viewModelScope.launch {
            val source = resolvedLocalFilePath ?: audioUrl
            val overview = waveformAnalyzer.analyzeTrackOverview(source, totalTrackDurationMs)
            _uiState.update { state ->
                state.copy(
                    overviewEnvelope = overview,
                    clipEnvelope = sliceEnvelope(overview, state.startMs, state.startMs + state.clipDurationMs),
                    isExtracting = false,
                )
            }
        }
    }

    /** A cheap resample of the whole-track overview for the selected range - used only
     * for the live preview's waveform strip. The real per-frame envelope burned into the
     * exported video is always a fresh, precise [WaveformAnalyzer.analyze] of the actual
     * extracted clip, run only once generation starts (see [extractAndAnalyze]). */
    private fun sliceEnvelope(source: FloatArray, startMs: Long, endMs: Long, outputSize: Int = 60): FloatArray {
        if (source.isEmpty() || totalTrackDurationMs <= 0L) return FloatArray(outputSize)
        val startFraction = (startMs.toFloat() / totalTrackDurationMs).coerceIn(0f, 1f)
        val endFraction = (endMs.toFloat() / totalTrackDurationMs).coerceIn(0f, 1f)
        return FloatArray(outputSize) { i ->
            val t = startFraction + (endFraction - startFraction) * (i.toFloat() / (outputSize - 1).coerceAtLeast(1))
            val pos = t.coerceIn(0f, 1f) * (source.size - 1)
            val low = pos.toInt().coerceIn(0, source.size - 1)
            val high = (low + 1).coerceAtMost(source.size - 1)
            val frac = pos - low
            source[low] * (1 - frac) + source[high] * frac
        }
    }

    /** The whole render+encode pipeline. Only ever runs once the user taps Share (see
     * [onShareClicked]) - never eagerly while they're still adjusting the trim range. */
    private fun extractAndAnalyze(startMs: Long, clipDurationMs: Long) {
        extractionJob?.cancel()
        videoExporter.cancel()
        // Set synchronously (not inside the coroutine below) so the generating overlay
        // appears on the very next frame after the tap, with zero dispatch lag.
        _uiState.update { it.copy(exportState = ShareExportState.Preparing, errorMessage = null) }
        extractionJob = viewModelScope.launch {
            // §8: refuse up front rather than failing mid-encode.
            if (shareFileStore.usableSpaceBytes() < MIN_USABLE_SPACE_BYTES) {
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_low_space))
                }
                return@launch
            }

            val content = _uiState.value.content ?: return@launch
            val baseBitmapFile = shareFileStore.baseBitmapFile(shareId)

            // Runs concurrently with the clip extraction + waveform analysis below -
            // the bitmap render depends on neither, so there's no reason to make the
            // user sit through both stages back-to-back while stuck at 0%.
            val bitmapJob = async(Dispatchers.Default) {
                val bitmap = bitmapRenderer.render(context, content, spec)
                baseBitmapFile.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                bitmap.recycle()
            }

            val clipResult = audioClipExtractor.extractClip(
                id = shareId,
                remoteUrl = audioUrl,
                localFilePath = resolvedLocalFilePath,
                startMs = startMs,
                durationMs = clipDurationMs,
            )

            clipResult.onSuccess { clip ->
                val envelope = waveformAnalyzer.analyze(clip.filePath, clip.durationMs)
                bitmapJob.await()
                runExport(
                    baseBitmapFile = baseBitmapFile,
                    clip = clip,
                    envelope = envelope,
                    isRetryAtLowerResolution = false,
                )
            }.onFailure {
                bitmapJob.cancel()
                _uiState.update {
                    it.copy(exportState = ShareExportState.Idle, errorMessage = context.getString(R.string.share_error_generic))
                }
            }
        }
    }

    private suspend fun runExport(
        baseBitmapFile: File,
        clip: ShareClip,
        envelope: FloatArray,
        isRetryAtLowerResolution: Boolean,
    ) {
        val overlay = WaveformOverlay(envelope, spec, frameRate = ShareVideoExporter.VIDEO_FRAME_RATE)
        val outputFile = shareFileStore.exportFile(shareId)

        videoExporter.export(
            context = context,
            baseBitmapFile = baseBitmapFile,
            clipFilePath = clip.filePath,
            clipDurationMs = clip.durationMs,
            waveformOverlay = overlay,
            outputFile = outputFile,
        ).collect { progress ->
            when (progress) {
                is ExportProgress.Encoding -> {
                    _uiState.update { it.copy(exportState = ShareExportState.Encoding(progress.progress)) }
                }

                is ExportProgress.Completed -> {
                    _uiState.update { it.copy(exportState = ShareExportState.Ready(shareFileStore.uriForFile(outputFile))) }
                    if (shareWhenReady) {
                        shareWhenReady = false
                        fireShareIntent(outputFile)
                    }
                }

                is ExportProgress.Failed -> {
                    // §8: some API 24-26 devices can't encode 1080x1920 - retry once at 720x1280.
                    if (!isRetryAtLowerResolution && progress.errorCode == ExportException.ERROR_CODE_ENCODING_FORMAT_UNSUPPORTED) {
                        runExport(baseBitmapFile, clip, envelope, isRetryAtLowerResolution = true)
                    } else {
                        _uiState.update {
                            it.copy(
                                exportState = ShareExportState.Failed(progress.message),
                                errorMessage = context.getString(R.string.share_error_generic),
                            )
                        }
                    }
                }
            }
        }
    }

    fun onPlayPauseToggle() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
            return
        }
        _uiState.update { it.copy(playbackErrorMessage = null) }
        val state = _uiState.value
        val relative = exoPlayer.currentPosition - state.startMs
        if (relative < 0L || relative >= state.clipDurationMs) {
            exoPlayer.seekTo(state.startMs)
        }
        if (exoPlayer.playbackState == Player.STATE_IDLE) {
            exoPlayer.prepare()
        }
        exoPlayer.play()
    }

    fun onSeekWithinClip(positionMs: Long) {
        val state = _uiState.value
        val clamped = positionMs.coerceIn(0L, state.clipDurationMs)
        exoPlayer.seekTo(state.startMs + clamped)
        _uiState.update { it.copy(playbackPositionMs = clamped) }
    }

    /** Freeform trim: either handle, or the whole window, can move to any position -
     * cheap (just a state update + a resample of the already-computed overview), so
     * it runs on every drag delta with no debounce needed for a smooth drag. */
    fun onRangeChanged(newStartMs: Long, newEndMs: Long) {
        val clampedStart = newStartMs.coerceIn(0L, totalTrackDurationMs)
        val minEnd = (clampedStart + MIN_SHAREABLE_DURATION_MS).coerceAtMost(totalTrackDurationMs)
        val clampedEnd = newEndMs.coerceIn(minEnd, totalTrackDurationMs)
        _uiState.update { state ->
            state.copy(
                startMs = clampedStart,
                clipDurationMs = (clampedEnd - clampedStart).coerceAtLeast(0L),
                clipEnvelope = sliceEnvelope(state.overviewEnvelope, clampedStart, clampedEnd),
            )
        }
    }

    /** Tapping Share is what starts generation - nothing runs eagerly before this. */
    fun onShareClicked() {
        when (_uiState.value.exportState) {
            is ShareExportState.Ready -> fireShareIntent(shareFileStore.exportFile(shareId))
            ShareExportState.Preparing, is ShareExportState.Encoding -> Unit // already generating
            ShareExportState.Idle, is ShareExportState.Failed -> {
                shareWhenReady = true
                extractAndAnalyze(_uiState.value.startMs, _uiState.value.clipDurationMs)
            }
        }
    }

    fun onRetry() {
        _uiState.update { it.copy(errorMessage = null) }
        extractAndAnalyze(_uiState.value.startMs, _uiState.value.clipDurationMs)
    }

    private fun fireShareIntent(file: File) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastShareIntentFiredAtMs < SHARE_DEBOUNCE_MS) return
        lastShareIntentFiredAtMs = now
        hasBeenShared = true
        _shareIntentEvent.tryEmit(shareFileStore.uriForFile(file))
    }

    private fun listenToPlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update { it.copy(isBuffering = playbackState == Player.STATE_BUFFERING) }
            }

            override fun onPlayerError(error: PlaybackException) {
                // A remote source can fail before the local file swap lands (§1) -
                // surface it instead of leaving the play button silently dead. Kept
                // separate from `errorMessage` (export failures) since a preview
                // playback hiccup shouldn't hide the Share button.
                _uiState.update { it.copy(isBuffering = false, playbackErrorMessage = context.getString(R.string.share_error_generic)) }
            }
        })

        positionJob = viewModelScope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    val state = _uiState.value
                    val relative = exoPlayer.currentPosition - state.startMs
                    if (relative < 0L || relative >= state.clipDurationMs) {
                        // The trim range moved out from under playback (or we hit the
                        // end of the selection) - stop and reset to the new start.
                        exoPlayer.pause()
                        exoPlayer.seekTo(state.startMs)
                        _uiState.update { it.copy(playbackPositionMs = 0L) }
                    } else {
                        _uiState.update { it.copy(playbackPositionMs = relative) }
                    }
                }
                delay(POSITION_POLL_MS)
            }
        }
    }

    override fun onCleared() {
        positionJob?.cancel()
        extractionJob?.cancel()
        videoExporter.cancel()
        exoPlayer.release()
        // §7: intermediates are never handed to another app - always safe to delete.
        shareFileStore.deleteIntermediates(shareId)
        // §7: the exported mp4 is deleted only if it was never shared; the receiving
        // app may still be reading the content URI asynchronously otherwise.
        if (!hasBeenShared) {
            shareFileStore.deleteExport(shareId)
        }
        super.onCleared()
    }

    companion object {
        const val ARG_AUDIO_URL = "audioUrl"
        const val ARG_TITLE = "title"
        const val ARG_CATEGORY = "category"
        const val ARG_LOCAL_FILE_PATH = "localFilePath"
        const val ARG_START_MS = "startMs"
        const val ARG_TOTAL_DURATION_MS = "totalDurationMs"

        private const val DEFAULT_WINDOW_MS = 60_000L
        private const val MIN_SHAREABLE_DURATION_MS = 5_000L
        private const val POSITION_POLL_MS = 200L
        private const val MIN_USABLE_SPACE_BYTES = 150L * 1024 * 1024
        private const val SHARE_DEBOUNCE_MS = 800L
    }
}
