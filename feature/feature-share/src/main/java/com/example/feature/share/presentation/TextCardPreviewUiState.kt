package com.example.feature.share.presentation

import com.example.feature.share.domain.ShareCardContent
import com.example.feature.share.domain.ShareExportState

data class TextCardPreviewUiState(
    val content: ShareCardContent,
    /** Only ever Idle/Preparing/Ready/Failed for this flow - a bitmap render
     * has no meaningful in-progress percentage, so [ShareExportState.Encoding]
     * is never emitted here. */
    val exportState: ShareExportState = ShareExportState.Idle,
    val errorMessage: String? = null,
)
