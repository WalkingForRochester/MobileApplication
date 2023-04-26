package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.annotation.DrawableRes
import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import com.walkingforrochester.walkingforrochester.android.R

enum class Destination(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int? = null,
    val showBottomBar: Boolean = true,
    val showTopBar: Boolean = true,
    val showBackButton: Boolean = false,
    val showProfileButton: Boolean = true,
    val showTitleComposable: Boolean = false,
    val arguments: List<NamedNavArgument> = listOf(),
    val routeWithArgs: String = "",
    val enableDrawerGestures: Boolean = false
) {
    Login(
        "login",
        "Login",
        showBottomBar = false,
        showTopBar = false,
        enableDrawerGestures = true
    ),
    ForgotPassword(
        "forgotPassword",
        "Forgot Password",
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        showProfileButton = false
    ),
    Registration(
        "registration",
        "Sign Up",
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        showProfileButton = false,
        arguments = listOf(
            navArgument("email") { nullable = true },
            navArgument("fname") { nullable = true },
            navArgument("lname") { nullable = true },
            navArgument("fbid") { nullable = true }),
        routeWithArgs = "registration?email={email}&fname={fname}&lname={lname}&fbid={fbid}"
    ),
    LogAWalk(
        "logAWalk",
        "Log a Walk",
        icon = R.drawable.ic_walk,
        showTitleComposable = true
    ),
    Leaderboard(
        "leaderboard",
        "Leaderboard",
        icon = R.drawable.ic_leaderboard,
        enableDrawerGestures = true
    ),
    NewsFeed("newsFeed", "News Feed", icon = R.drawable.ic_newspaper),
    Profile(
        "profile",
        "Profile",
        showBottomBar = false,
        showProfileButton = false,
        showBackButton = true,
        enableDrawerGestures = true
    ),
    Calendar(
        "calendar",
        "Calendar",
        icon = R.drawable.ic_calendar,
        showBottomBar = false,
        showBackButton = true
    ),
    FAQ(
        "faq",
        "FAQ",
        icon = R.drawable.ic_faq,
        showBottomBar = false,
        showBackButton = true
    ),
    AboutUs(
        "aboutUs",
        "About Us",
        icon = R.drawable.ic_about_us,
        showBottomBar = false,
        showBackButton = true
    )
}

val bottomBarDestinations = listOf(
    Destination.LogAWalk,
    Destination.Leaderboard,
    Destination.NewsFeed
)

val drawerDestinations = listOf(
    Destination.FAQ,
    Destination.AboutUs
)
