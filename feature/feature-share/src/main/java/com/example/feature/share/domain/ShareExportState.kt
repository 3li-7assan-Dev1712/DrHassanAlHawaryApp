package com.example.feature.share.domain

import android.net.Uri

sealed interface ShareExportState {
    data object Idle : ShareExportState
    data object Preparing : ShareExportState
    data class Encoding(val progress: Float) : ShareExportState
    data class Ready(val uri: Uri) : ShareExportState
    data class Failed(val reason: String) : ShareExportState
}
