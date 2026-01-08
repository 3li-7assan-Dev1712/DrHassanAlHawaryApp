package com.example.feature.home.presentation.navigation

/**
 * A centralized object to hold all navigation route constants for the app.
 * Using this object prevents typos and makes route management easier.
 */
object Routes {
    // --- Authentication Routes ---
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN_SCREEN = "login_screen"
    const val REGISTER_SCREEN = "register_screen"

    // --- Main App Routes (Bottom Navigation) ---
    const val MAIN_GRAPH = "main_graph"
    const val HOME_SCREEN = "home_screen"
    const val SEARCH_SCREEN = "search_screen"
    const val STUDY_ZONE_SCREEN = "study_zone_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val CV_SCREEN = "cv_screen"

    // --- Category (Lessons) Routes ---
    const val ARTICLES_SCREEN = "articles_screen"
    const val AUDIO_LIST_SCREEN = "audio_list_screen"
    const val VIDEOS_SCREEN = "videos_screen"
    const val KHOTAB_SCREEN = "khotab_screen"
    const val IMAGES_SCREEN = "images_screen"

    const val IMPORTANT_QUESTIONS_SCREEN = "important_questions_screen"
    // --- Detail Screens ---
     const val ARTICLE_DETAIL_SCREEN = "article_detail_screen"
    const val AUDIO_DETAIL_SCREEN = "audio_detail_screen"
    const val VIDEO_PLAYER_SCREEN = "video_player_screen"
    const val KHOTAB_DETAIL_SCREEN = "khotab_detail_screen"
    const val IMAGE_DETAIL_SCREEN = "image_detail_screen"


}