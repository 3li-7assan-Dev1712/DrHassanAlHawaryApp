package com.example.hassanalhawary.ui.screens.home_screen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hassanalhawary.R
import com.example.hassanalhawary.domain.model.Question
import com.example.hassanalhawary.ui.theme.CairoTypography
import com.example.hassanalhawary.ui.theme.HassanAlHawaryTheme

@Composable
fun QuestionOfTheDay(
    question: Question,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (questionId: String) -> Unit
) {
    var isAnswerVisible by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(targetValue = if (isAnswerVisible) 180f else 0f, label = "expand_icon_rotation")

    Card(
        onClick = {
            onNavigateToDetail(question.id)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large, // Or medium
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info, // Choose an icon that fits
                    contentDescription = "Question of the Day Icon",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.question_of_the_day),
                    style = MaterialTheme.typography.labelLarge, // Or titleSmall
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Row to allow tapping to expand/collapse answer, or just to show a hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                // If you want the row itself to toggle the answer visibility, add .clickable here
                // .clickable { isAnswerVisible = !isAnswerVisible }
            ) {
                Text(
                    text = if (isAnswerVisible) "Hide Answer" else "Tap to view details & answer",
                    style = CairoTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                )
                // Optional: Expand/collapse icon if you were toggling locally
                // IconButton(onClick = { isAnswerVisible = !isAnswerVisible }) {
                //     Icon(
                //         imageVector = Icons.Default.ExpandMore,
                //         contentDescription = if (isAnswerVisible) "Collapse Answer" else "Expand Answer",
                //         tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                //         modifier = Modifier.rotate(rotationAngle)
                //     )
                // }
            }

            // Animated visibility for the answer (if you choose to show it on this card)
            // For your use case (navigate to detail for answer), this might be omitted or
            // just show a placeholder like "Answer available in details"
            AnimatedVisibility(
                visible = isAnswerVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column { // Ensure answer text is also in a Column if it's multiline
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Answer: ${question.answer}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

// Preview Composable
@Preview(name = "Question of the Day - Light", showBackground = true)
@Preview(name = "Question of the Day - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewQuestionOfTheDayCard() {
    val sampleQuestion = Question(
        id = "qotd123",
        question = "What is the primary virtue emphasized during the month of Ramadan?",
        answer = "Patience (Sabr) and Taqwa (God-consciousness) are among the primary virtues emphasized."
    )
    HassanAlHawaryTheme { // Make sure to wrap with your app's theme for accurate preview
        Surface(color = MaterialTheme.colorScheme.background) { // Provide a background for contrast
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuestionOfTheDay(
                    question = sampleQuestion,
                    onNavigateToDetail = { questionId ->
                        println("Preview: Navigate to detail for question ID: $questionId")
                    }
                )

                // Example of how it might look if the answer was toggled visible (for design purposes)
                // You can add a button in preview to toggle state if needed, or just show both states.
                val sampleQuestionAnswerVisible = remember { mutableStateOf(true) }
                val sampleQuestion2 = Question(
                    id = "qotd456",
                    question = "Another interesting question for preview?",
                    answer = "This is how the answer might appear when expanded locally."
                )

                // Simulating the card with answer visible for preview
                // In actual use, isAnswerVisible state is internal to QuestionOfTheDayCard
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, "Icon", tint= MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.size(8.dp))
                            Text("Question of the Day", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(sampleQuestion2.question, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.height(8.dp))
                        Text("Hide Answer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Spacer(Modifier.height(10.dp))
                        Text("Answer: ${sampleQuestion2.answer}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }
}
