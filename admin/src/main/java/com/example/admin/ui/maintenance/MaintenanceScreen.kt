package app.netlify.devalihassan.admin.ui.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R

/**
 * Super admins switch the user app's maintenance mode on and off and set its message.
 * Switching on asks first: every user is locked out until it is switched off.
 */
@Composable
fun MaintenanceScreen(
    isSuperAdmin: Boolean,
    onLogout: () -> Unit,
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

    val viewModel: MaintenanceViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsState()
    MaintenanceContent(
        state = state,
        onSetMaintenance = viewModel::setMaintenance,
        onDraftMessageChange = viewModel::onDraftMessageChange,
        onSaveMessage = viewModel::saveMessage,
    )
}

@Composable
private fun MaintenanceContent(
    state: MaintenanceUiState,
    onSetMaintenance: (Boolean) -> Unit,
    onDraftMessageChange: (String) -> Unit,
    onSaveMessage: () -> Unit,
) {
    var confirmTurnOn by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (state.isOn) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.maintenance),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (!state.isLoading) {
                        Text(
                            text = stringResource(
                                if (state.isOn) R.string.maintenance_on_status else R.string.maintenance_off_status
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.isOn) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Switch(
                        checked = state.isOn,
                        enabled = !state.isSaving,
                        onCheckedChange = { on -> if (on) confirmTurnOn = true else onSetMaintenance(false) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.draftMessage,
            onValueChange = onDraftMessageChange,
            enabled = !state.isLoading,
            label = { Text(stringResource(R.string.maintenance_message_label)) },
            supportingText = { Text(stringResource(R.string.maintenance_message_hint)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = onSaveMessage,
            enabled = state.canSaveMessage,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.maintenance_save_message))
        }

        if (state.isSaving) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        val feedback = state.errorText ?: state.messageRes?.let { stringResource(it) }
        if (feedback != null) {
            Text(
                text = feedback,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            text = stringResource(R.string.maintenance_console_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    if (confirmTurnOn) {
        AlertDialog(
            onDismissRequest = { confirmTurnOn = false },
            title = { Text(stringResource(R.string.maintenance_confirm_title)) },
            text = {
                Text(
                    text = stringResource(R.string.maintenance_confirm_msg),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmTurnOn = false
                        onSetMaintenance(true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.maintenance_turn_on))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmTurnOn = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
