package com.example.hassanalhawary.ui.screens.ask_question_screen






data class AskQuestionUiState(
    val questionText: String = "",
    val selectedCategory: String? = null, // Can be null if category is optional or not yet selected

    val isSubmitting: Boolean = false, // True when the submission process is active (e.g., network call)
    val submissionStatus: SubmissionStatus = SubmissionStatus.IDLE, // More detailed status of the last submission attempt
    val formError: FormError? = null, // To display specific errors related to form validation

    val availableCategories: List<String> = emptyList(), // Categories fetched from a source


    val showSubmissionSuccessDialog: Boolean = false, // Trigger to show a success dialog
    val navigateToBrowseQuestions: Boolean = false // Trigger to navigate to browse screen
)

// Enum to represent the status of the question submission attempt
enum class SubmissionStatus {
    IDLE,       // No submission attempt yet, or last attempt was reset
    SUCCESS,    // Question submitted successfully
    ERROR_NETWORK, // Network error during submission
    ERROR_VALIDATION, // Validation error (though specific field errors are in formError)
    ERROR_UNKNOWN // Other unexpected error
}

// Data class to hold specific form validation errors
data class FormError(
    val questionTextError: String? = null, // E.g., "Question cannot be empty", "Question is too short"
    val categoryError: String? = null      // E.g., "Please select a category" (if mandatory)
)