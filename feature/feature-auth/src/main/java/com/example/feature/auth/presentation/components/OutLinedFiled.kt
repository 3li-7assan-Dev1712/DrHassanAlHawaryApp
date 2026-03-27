package com.example.feature.auth.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.ui.R
import com.example.core.ui.theme.CairoTypography

@Composable
fun OutlinedField(
    modifier: Modifier,
    @StringRes label: Int,
    icon: ImageVector,
    @DrawableRes trailingIcon: Int? = null,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean = false,
    errorMessage: String? = null,
    isPasswordField: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

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
                contentDescription = null,
            )
        }, label = {
            Text(
                text = stringResource(label),
                style = CairoTypography.bodyMedium
            )
        },
        trailingIcon = {
            if (isPasswordField) {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            } else if (trailingIcon != null) {
                Icon(
                    painter = painterResource(trailingIcon),
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (isPasswordField && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
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