package com.example.profile.presentation.support

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.profile.domain.model.SystemActions
import com.example.profile.domain.use_case.ContactSupportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SupportViewModel @Inject constructor(
    private val actions: SystemActions,
    private val contactSupportUseCase: ContactSupportUseCase
) : ViewModel() {

    fun emailSupport() {
        contactSupportUseCase.email(
            to = "support@example.com",
            subject = "الدعم - تطبيق فقه وتأصيل",
            body = "السلام عليكم,\n\n"
        )
    }

    fun openWebsite() = contactSupportUseCase.openUrl("https://example.com")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    viewModel: SupportViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الدعم والتواصل", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                ListItem(
                    headlineContent = { Text("راسلنا عبر البريد") },
                    supportingContent = { Text("للأسئلة والمشاكل والاقتراحات") },
                    leadingContent = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.clickable { viewModel.emailSupport() }
                )
            }

            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                ListItem(
                    headlineContent = { Text("الموقع الرسمي") },
                    supportingContent = { Text("معلومات وروابط إضافية") },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    modifier = Modifier.clickable { viewModel.openWebsite() }
                )
            }
        }
    }
}
