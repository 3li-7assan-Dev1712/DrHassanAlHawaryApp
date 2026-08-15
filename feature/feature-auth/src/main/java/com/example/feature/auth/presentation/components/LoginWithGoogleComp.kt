package com.example.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.R
import com.example.core.ui.theme.CairoTypography

@Composable
fun LoginWithGoogleComp(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onElementClick: () -> Unit
) {

    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = onElementClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google Icon",
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = stringResource(R.string.continue_with_google),
                textAlign = TextAlign.Center,
                style = CairoTypography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun LoginWithGoogleCompPreview() {
    LoginWithGoogleComp {}
}

@Preview(showBackground = true, widthDp = 320, name = "Loading")
@Composable
fun LoginWithGoogleCompLoadingPreview() {
    LoginWithGoogleComp(isLoading = true) {}
}
