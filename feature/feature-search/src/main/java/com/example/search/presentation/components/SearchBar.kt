package com.example.search.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.CairoTypography

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    hint: String,
    onSearchClicked: () -> Unit = {}
) {
    Row(
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon", // Important for accessibility
            modifier = Modifier.padding(start = 8.dp, end = 8.dp)
        )

        // Using BasicTextField for more control over styling and no default Material decorations
        BasicTextField(
            value = searchQuery,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .weight(1f) // Takes remaining space
                .padding(end = 8.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = CairoTypography.bodyLarge.fontSize,
                fontFamily = CairoTypography.bodyLarge.fontFamily
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // Hint color
                        )
                    }
                    innerTextField() // This is where the actual text field input is rendered
                }
            }
        )

        // let user clear the search query
        if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onQueryChanged("") }) { // Clear the query
                Icon(
                    imageVector = Icons.Default.Close, // Or Icons.Filled.Clear
                    contentDescription = "Clear Search"
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 120)
@Composable
fun SearchBarPreview() {
    SearchBar(
        searchQuery = "Search Query",
        onQueryChanged = { /* Handle query changes */ },
        hint = "Search"
    )

}