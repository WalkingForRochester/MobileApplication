package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import com.walkingforrochester.walkingforrochester.android.R

data class Destination(
    val route: String,
    @param:StringRes val title: Int,
    @param:DrawableRes val iconResId: Int? = null,
    val showBottomBar: Boolean = true,
    val showTopBar: Boolean = true,
    val showBackButton: Boolean = false,
    val showProfileButton: Boolean = true,
    val arguments: List<NamedNavArgument> = listOf(),
    val routeWithArgs: String = "",
    val enableDrawerGestures: Boolean = false,
    @param:StringRes val uriTarget: Int = 0
)

val LoginDestination = Destination(
    route = "login",
    title = R.string.login,
    showBottomBar = false,
    showTopBar = false,
)

val ForgotPassword = Destination(
    route = "forgotPassword",
    title = R.string.forgot_password,
    showBottomBar = false,
    showTopBar = true,
    showBackButton = true,
    showProfileButton = false
)

val Registration = Destination(
    route = "registration",
    title = R.string.sign_up,
    showBottomBar = false,
    showBackButton = true,
    showProfileButton = false,
    arguments = listOf(
        navArgument("email") { nullable = true },
        navArgument("fname") { nullable = true },
        navArgument("lname") { nullable = true },
        navArgument("fbid") { nullable = true }),
    routeWithArgs = "registration?email={email}&fname={fname}&lname={lname}&fbid={fbid}"
)

val LogAWalk = Destination(
    route = "logAWalk",
    title = R.string.log_a_walk,
    iconResId = R.drawable.ic_directions_walk_24dp,
    enableDrawerGestures = false
)

val Leaderboard = Destination(
    route = "leaderboard",
    title = R.string.leaderboard,
    iconResId = R.drawable.ic_bar_chart_24dp,
    enableDrawerGestures = true
)

val NewsFeed = Destination(
    route = "newsFeed",
    title = R.string.news_feed,
    iconResId = R.drawable.ic_newspaper_24dp
)

val ProfileDestination = Destination(
    route = "profile",
    title = R.string.profile,
    showBottomBar = false,
    showProfileButton = false,
    showBackButton = true,
)

val SubmitWalk = Destination(
    route = "submitWalk",
    title = R.string.submit_walk,
    showBottomBar = false,
    showTopBar = false,
    showProfileButton = false,
)

val TakePicture = Destination(
    route = "takePicture",
    title = R.string.take_picture,
    showBottomBar = false,
    showTopBar = false,
    showProfileButton = false
)

val SafetyGuidelines = Destination(
    route = "safetyGuidelines",
    title = R.string.safety_guidelines,
    iconResId = R.drawable.ic_health_and_safety_24dp,
    uriTarget = R.string.guidelines_url
)

val Waiver = Destination(
    route = "waiver",
    title = R.string.waiver,
    iconResId = R.drawable.ic_contract_24dp,
    uriTarget = R.string.waiver_url
)

val OurStory = Destination(
    route = "ourStory",
    title = R.string.our_story,
    iconResId = R.drawable.ic_article_24dp,
    uriTarget = R.string.our_story_url
)

val ReportLitter = Destination(
    route = "reportLitter",
    title = R.string.report_litter,
    iconResId = R.drawable.ic_delete_24dp,
    uriTarget = R.string.report_litter_url
)

val ContactUs = Destination(
    route = "contactUs",
    title = R.string.contact_us,
    iconResId = R.drawable.ic_help_24dp,
    showBackButton = true,
    showProfileButton = false,
    showBottomBar = false
)

val Destinations = listOf(
    LoginDestination,
    ForgotPassword,
    Registration,
    LogAWalk,
    Leaderboard,
    NewsFeed,
    ProfileDestination,
    SubmitWalk,
    TakePicture,
    SafetyGuidelines,
    Waiver,
    OurStory,
    ReportLitter,
    ContactUs
)

val bottomBarDestinations = listOf(
    LogAWalk,
    Leaderboard,
    NewsFeed
)

val drawerDestinations = listOf(
    SafetyGuidelines,
    Waiver,
    OurStory,
    ReportLitter,
    ContactUs
)
