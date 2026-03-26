package com.example.profile.presentation.support

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.core.ui.R
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
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الدعم والتواصل", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }, windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )

        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.contact_illu),
                contentDescription = null,
                modifier = Modifier.height(250.dp)
            )

            Spacer(Modifier.height(16.dp))

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
