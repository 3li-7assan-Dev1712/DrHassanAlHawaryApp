package com.example.admin.ui.add_edit_playlist

import android.net.Uri

/**
 * Represents the different states for the Add/Edit Playlist screen.
 * This sealed interface allows for a clear definition of what the UI should display
 * at any given time, handling initial loading, data entry, saving, and error states.
 */
sealed interface AddEditPlaylistUiState {

    /**
     * The primary state where the user can view and edit the playlist details.
     * This state holds all the data currently displayed in the form.
     *
     * @param playlistId The ID of the playlist being edited, or null if adding.
     * @param title The current value in the title input field.
     * @param order The current value in the order input field.
     * @param existingImageUrl The URL of the cover image for an existing playlist.
     * @param selectedImageUri The URI of a new image selected from the device gallery.
     * @param isSaving Flag to indicate that a save operation is in progress (shows a loading spinner).
     * @param error A message to display if a validation or save error occurs.
     */
    data class Stable(
        val playlistId: String?,
        val levelId: String,
        val title: String = "",
        val order: String = "",
        val existingImageUrl: String? = null,
        val selectedImageUri: Uri? = null,
        val isSaving: Boolean = false,
        val error: String? = null
    ) : AddEditPlaylistUiState

    /**
     * Represents the initial loading state when fetching an existing playlist's
     * data from the server. The UI should show a full-screen loading indicator.
     */
    object Loading : AddEditPlaylistUiState

    /**
     * A terminal state indicating that the playlist was successfully saved to the server.
     * The UI should react to this state by navigating back or showing a success message.
     */
    object SaveSuccess : AddEditPlaylistUiState

    /**
     * A terminal state indicating that a non-recoverable error occurred,
     * such as failing to fetch the initial playlist data.
     * The UI should show a full-screen error message.
     */
    data class Error(val message: String) : AddEditPlaylistUiState
}
