package com.example.hassanalhawary.ui.navigation

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
    const val STUDY_SCREEN = "study_screen"
    const val PLAYLIST_SCREEN = "playlist_screen"
    const val LESSONS_SCREEN = "lessons_screen"
    const val LESSON_DETAIL_SCREEN = "lesson_detail_screen"

    const val LEVELS_SCREEN = "levels_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val ABOUT_DR_HASSAN_SCREEN = "about_dr_hassan"

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

object StudyRoute {
    const val routeName = Routes.STUDY_SCREEN
    const val deepLinkArg = "data"
    // The route is "study" but can optionally have "?data=..." appended
    val routeDefinition = "$routeName?$deepLinkArg={$deepLinkArg}"
}