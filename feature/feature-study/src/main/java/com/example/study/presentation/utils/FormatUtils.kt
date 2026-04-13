package com.example.study.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.study.R

@Composable
fun formatBatchName(batch: String): String {
    val order = batch.substringAfter("batch_").toIntOrNull()
    return when (order) {
        1 -> stringResource(R.string.batch_1)
        2 -> stringResource(R.string.batch_2)
        3 -> stringResource(R.string.batch_3)
        4 -> stringResource(R.string.batch_4)
        5 -> stringResource(R.string.batch_5)
        null -> batch
        else -> stringResource(R.string.batch_other, order)
    }
}
