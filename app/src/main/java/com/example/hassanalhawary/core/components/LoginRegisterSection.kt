package com.example.hassanalhawary.core.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hassanalhawary.R


@Composable
fun LoginRegisterSection(
    modifier: Modifier,
    isLogin: Boolean,

    onLoginRegisterClick: () -> Unit

) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (!isLogin) stringResource(R.string.login_question)
            else stringResource(R.string.register_question),
            style = MaterialTheme.typography.bodyMedium
        )



        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (!isLogin) stringResource(R.string.login)
            else stringResource(R.string.register),
            modifier = Modifier.clickable {
                onLoginRegisterClick()
            },
            style = MaterialTheme.typography.bodyLarge

        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 320)
@Composable
fun LoginRegisterSectionPrev() {
    LoginRegisterSection(
        modifier = Modifier,
        isLogin = true,
        onLoginRegisterClick = {

        })
}

