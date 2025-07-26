package com.example.hassanalhawary.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.theme.CairoTypography


@Composable
fun LoginWithGoogleComp(
    modifier: Modifier = Modifier,
    isLogin: Boolean,
    onElementClick: () -> Unit
) {


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                onElementClick()
            }
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(size = 8.dp)
            )
            .background(Color(0xFFE5E5E5)) // light gray color works in light/dark theme
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Google Icon",
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isLogin) stringResource(R.string.register_using)
            else stringResource(R.string.login_using),
            textAlign = TextAlign.Center,
            style= CairoTypography.bodyMedium

        )

    }


}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun LoginWithGoogleCompPreview() {
    LoginWithGoogleComp(
        isLogin = true,
    ) {

    }
}