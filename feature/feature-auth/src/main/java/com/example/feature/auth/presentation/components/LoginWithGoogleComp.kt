package com.example.feature.auth.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
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
    isLogin: Boolean,
    onElementClick: () -> Unit
) {


    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = {
            onElementClick()
        },
        shape = RoundedCornerShape(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Google Icon",
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (isLogin) stringResource(R.string.login_using)
            else stringResource(R.string.register_using),
            textAlign = TextAlign.Center,
            style = CairoTypography.bodyMedium

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