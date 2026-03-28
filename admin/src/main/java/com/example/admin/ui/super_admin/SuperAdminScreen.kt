package com.example.admin.ui.super_admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.admin.R

@Composable
fun SuperAdminScreen(
    isSuperAdmin: Boolean,
    onLogout: () -> Unit
) {
    if (!isSuperAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.access_denied),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.super_admin_restricted_msg),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        }
        return
    }

    val viewModel: SuperAdminViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var emailToAdd by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.admin_management),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.current_admins),
                style = MaterialTheme.typography.titleMedium
            )
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.add_admin))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        state.error?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
        }

        state.successMessage?.let {
            Text(text = it, color = Color.Green, modifier = Modifier.padding(vertical = 8.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.admins) { admin ->
                AdminItem(
                    email = admin["email"] as? String ?: "",
                    onRemove = { viewModel.removeAdmin(admin["email"] as? String ?: "") }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; viewModel.clearMessages() },
            title = { Text(stringResource(R.string.add_new_admin_title)) },
            text = {
                OutlinedTextField(
                    value = emailToAdd,
                    onValueChange = { emailToAdd = it },
                    label = { Text(stringResource(R.string.email_address)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addAdmin(emailToAdd)
                    showAddDialog = false
                    emailToAdd = ""
                }) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AdminItem(email: String, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = email, style = MaterialTheme.typography.bodyLarge)
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.remove_admin),
                    tint = Color.Red
                )
            }
        }
    }
}
