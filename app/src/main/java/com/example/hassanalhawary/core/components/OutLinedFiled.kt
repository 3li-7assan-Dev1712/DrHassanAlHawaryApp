package com.example.hassanalhawary.core.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.hassanalhawary.R
import com.example.hassanalhawary.ui.theme.CairoTypography

@Composable
fun OutlinedField(
    modifier: Modifier,
    @StringRes label: Int,
    icon: ImageVector,
    @DrawableRes trailingIcon: Int? = null,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean = false,
    errorMessage: String? = null

) {

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "Enter your E-mail to login",
            )
        }, label = {
            Text(
                text = stringResource(label),
                style = CairoTypography.bodyMedium
            )
        },
        trailingIcon = {
            if (trailingIcon != null) {
                Icon(
                    painter = painterResource(trailingIcon),
                    contentDescription = "Enter your password to login"
                )
            }
        },
        isError = showError

    )

}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun OutlinedFieldPrev() {
    OutlinedField(
        modifier = Modifier.fillMaxWidth(),
        label = R.string.enter_email,
        icon = Icons.Default.Lock,
        value = "",
        onValueChange = {

        },
        showError = false,
        errorMessage = null
    )
}