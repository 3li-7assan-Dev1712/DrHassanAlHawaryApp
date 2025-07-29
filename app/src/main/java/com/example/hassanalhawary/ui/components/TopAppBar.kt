package com.example.hassanalhawary.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hassanalhawary.ui.theme.CairoTypography

@Composable
fun TopAppBar(
    modifier: Modifier,
    title: String
) {
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = CairoTypography.headlineLarge,
        )
    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 120)
@Composable
fun TopBarRev() {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = "Dr Hasan AlHwary"
    )
}