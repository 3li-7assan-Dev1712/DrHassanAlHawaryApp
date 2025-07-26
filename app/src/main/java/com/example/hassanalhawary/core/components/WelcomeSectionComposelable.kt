package com.example.hassanalhawary.core.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.theme.CairoTypography

@Composable
fun WelcomeScreen(
    modifier: Modifier,
    @StringRes loginRegister: Int
) {


    Column(
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = CairoTypography.headlineLarge
        )



        Text(
            text = stringResource(loginRegister),
            style = CairoTypography.titleLarge
        )
    }

}

@Preview(name = "Welcome Screen", widthDp = 320, heightDp = 200, showBackground = true)
@Composable
private fun WelcomeScreenPrv() {
    WelcomeScreen(modifier = Modifier, loginRegister = R.string.login)
}