package app.netlify.devalihassan.admin.ui.upload_images_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.netlify.devalihassan.admin.R
import app.netlify.devalihassan.admin.ui.components.SeriesPickerField
import app.netlify.devalihassan.admin.ui.content.AdminContentList
import coil.compose.SubcomposeAsyncImage
import com.example.core.ui.components.shimmer
import com.example.domain.module.ImageGroup

/**
 * Every design group: search, filter by series, change many at once. A tap opens the
 * group's series dialog (designs have no edit screen).
 */
@Composable
fun ImageGroupsListScreen(
    viewModel: ImageGroupsListViewModel = hiltViewModel(),
    onAddGroup: () -> Unit
) {
    var groupForSeries by remember { mutableStateOf<ImageGroup?>(null) }

    AdminContentList(
        viewModel = viewModel,
        addLabel = stringResource(R.string.upload_new_design_group),
        onAdd = onAddGroup,
        onOpen = { group ->
            viewModel.editSeries(group)
            groupForSeries = group
        },
        onDelete = viewModel::deleteGroup,
        leading = { group ->
            SubcomposeAsyncImage(
                model = group.previewImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
                loading = { Box(Modifier.fillMaxSize().shimmer(cornerRadius = 10.dp)) },
            )
        },
    )

    groupForSeries?.let { group ->
        val form by viewModel.seriesForm.state.collectAsState()
        AlertDialog(
            onDismissRequest = { groupForSeries = null },
            title = { Text(stringResource(R.string.design_series)) },
            text = { SeriesPickerField(controller = viewModel.seriesForm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveSeries(group.id)
                        groupForSeries = null
                    },
                    enabled = form.isValid,
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { groupForSeries = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
