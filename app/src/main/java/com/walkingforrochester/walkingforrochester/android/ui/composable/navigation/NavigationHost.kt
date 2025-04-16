package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.ui.composable.contact.ContactUsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword.ForgotPasswordScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard.LeaderboardScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.LogAWalkScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.login.LoginScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed.NewsFeedScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.profile.ProfileScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.registration.RegistrationScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.submitwalk.SubmitWalkScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.takepicture.TakePictureScreen

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    loggedIn: Boolean = false,
    contentPadding: PaddingValues = PaddingValues()
) {
    val startDestination = remember {
        when {
            !loggedIn -> LoginDestination.route
            else -> LogAWalk.route
        }
    }

    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = LoginDestination.route) {
            LoginScreen(
                onForgotPassword = {
                    navController.navigate(route = ForgotPassword.route) {
                        launchSingleTop = true
                    }
                },
                onRegister = { email, firstName, lastName, facebookId ->
                    navController.navigate(
                        route = Registration.route +
                            "?email=$email" +
                            "&fname=$firstName" +
                            "&lname=$lastName" +
                            "&fbid=$facebookId"
                    ) {
                        popUpTo(LoginDestination.route)
                        launchSingleTop = true
                    }
                },
                onLoginComplete = {
                    navController.navigateAndClearBackStack(
                        route = LogAWalk.route,
                        clearToRoot = true
                    )
                },
                contentPadding = contentPadding
            )
        }

        composable(route = ForgotPassword.route) {
            ForgotPasswordScreen(
                onPasswordResetComplete = {
                    navController.navigateAndClearBackStack(
                        LoginDestination.route,
                        clearToRoot = true
                    )
                },
                contentPadding = contentPadding
            )
        }

        composable(
            route = Registration.routeWithArgs,
            arguments = Registration.arguments,
        ) { navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("email") ?: ""
            val firstName = navBackStackEntry.arguments?.getString("fname") ?: ""
            val lastName = navBackStackEntry.arguments?.getString("lname") ?: ""
            val facebookId = navBackStackEntry.arguments?.getString("fbid")

            RegistrationScreen(
                profile = AccountProfile.DEFAULT_PROFILE.copy(
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    facebookId = facebookId
                ),
                onRegistrationComplete = {
                    navController.navigateAndClearBackStack(
                        LogAWalk.route,
                        clearToRoot = true
                    )
                },
                contentPadding = contentPadding
            )
        }

        composable(
            route = LogAWalk.route,
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() }
        ) {
            LogAWalkScreen(
                //o//nStartWalking = { },
                //onStopWalking = { },
                onNavigateToSubmitWalk = {
                    navController.navigate(SubmitWalk.route) {
                        launchSingleTop = true
                    }
                },
                contentPadding = contentPadding
            )
        }

        composable(route = Leaderboard.route) {
            LeaderboardScreen(contentPadding = contentPadding)
        }

        composable(route = NewsFeed.route) {
            NewsFeedScreen(contentPadding = contentPadding)
        }

        composable(route = ProfileDestination.route) {
            ProfileScreen(
                onLogoutComplete = {
                    navController.navigateAndClearBackStack(
                        route = LoginDestination.route,
                        clearToRoot = true
                    )
                },
                contentPadding = contentPadding
            )
        }

        composable(
            route = SubmitWalk.route,
            enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { fadeOut() + scaleOut() }
        ) {
            SubmitWalkScreen(
                onCompletion = {
                    navController.popBackStack()
                },
                onTakePicture = {
                    navController.navigate(route = TakePicture.route) {
                        launchSingleTop = true
                    }
                },
                windowSizeClass = windowAdaptiveInfo.windowSizeClass
            )
        }

        composable(
            route = TakePicture.route,
            enterTransition = { fadeIn() }, //+ slideInHorizontally(initialOffsetX = { it }) },
            exitTransition = { fadeOut() + scaleOut() }
        ) {
            /*TakePictureScreen(
                onCompletion = {
                    navController.popBackStack()
                },
            )*/
        }

        composable(route = ContactUs.route) {
            ContactUsScreen(contentPadding = contentPadding)
        }
    }
}

fun NavHostController.navigateAndClearBackStack(
    route: String,
    clearToRoot: Boolean = false
) = this.navigate(route) {
    // Use popUpTo(0) to fully clear the top to prevent backing to last destination
    if (clearToRoot) popUpTo(0) else popUpTo(LogAWalk.route)
    launchSingleTop = true
}
