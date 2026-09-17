package com.beardydev.lookhere.ui.navigation

sealed class Screen {
    data object Search : Screen()
    data object Camera : Screen()
}
