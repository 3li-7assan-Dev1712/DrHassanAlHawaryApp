package com.example.hassanalhawary.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hassanalhawary.ui.theme.CairoTypography

@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    navigationIcon: @Composable () -> Unit = {},
    showNavigationIcon: Boolean = false
) {
    Row(modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = CairoTypography.headlineLarge,
        )

        if (showNavigationIcon) navigationIcon()

    }
}


@Preview(showBackground = true, widthDp = 320, heightDp = 120)
@Composable
fun TopBarRev() {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = "Dr Hasan AlHwary",
        navigationIcon = {
            IconButton({}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        showNavigationIcon = true
    )
}