package com.example.feature.share.presentation

import com.example.feature.share.domain.ShareExportState
import com.example.feature.share.engine.QuotePage

data class TextCardPreviewUiState(
    val isLoading: Boolean = true,
    /** Every image of the share, in order. One for a short excerpt, several for a long one. */
    val pages: List<QuotePage> = emptyList(),
    /** Only ever Idle/Preparing/Ready/Failed for this flow - bitmap renders have no
     * meaningful in-progress percentage, so [ShareExportState.Encoding] is never emitted. */
    val exportState: ShareExportState = ShareExportState.Idle,
    val errorMessage: String? = null,
)
