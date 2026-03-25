package com.rite.pillcounting.core.utils.common

import androidx.navigation.NavController

fun NavController.navigateSafely(route: String) {
    //If you are already on the same destination, do nothing
    if (currentDestination?.route == route) return

    //If the destination is already on top, don’t create another instance
    navigate(route) {
        launchSingleTop = true
    }
}