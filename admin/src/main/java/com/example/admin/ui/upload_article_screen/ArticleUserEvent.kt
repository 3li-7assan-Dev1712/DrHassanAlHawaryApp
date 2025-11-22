package com.example.admin.ui.upload_article_screen


sealed interface ArticleUserEvent {
    data class OnTitleChanged(val title: String) : ArticleUserEvent
    data class OnContentChanged(val content: String) : ArticleUserEvent
    data class OnPublishDateChanged(val date: Long) : ArticleUserEvent
    object OnUploadClicked : ArticleUserEvent
    object OnUserMessageShown : ArticleUserEvent
}