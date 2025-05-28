package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

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
                    navController.navigate(route = ForgotPassword.route)
                },
                onRegister = { email, firstName, lastName, facebookId ->
                    navController.navigate(
                        route = Registration.route +
                            "?email=$email" +
                            "&fname=$firstName" +
                            "&lname=$lastName" +
                            "&fbid=$facebookId"
                    ) {
                        popUpTo(LoginDestination.route) {
                            inclusive = false
                        }
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

        composable(route = LogAWalk.route) {
            LogAWalkScreen(
                onNavigateToSubmitWalk = {
                    navController.navigate(SubmitWalk.route)
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

        composable(route = SubmitWalk.route) {
            SubmitWalkScreen(
                onNavigateBack = {
                    navController.popBackStack(route = LogAWalk.route, inclusive = false)
                },
                onTakePicture = {
                    navController.navigate(route = TakePicture.route)
                },
                windowSizeClass = windowAdaptiveInfo.windowSizeClass
            )
        }

        composable(route = TakePicture.route) {
            TakePictureScreen(
                windowSizeClass = windowAdaptiveInfo.windowSizeClass,
                onNavigateBack = {
                    navController.popBackStack(route = SubmitWalk.route, inclusive = false)
                },
            )
        }

        composable(route = ContactUs.route) {
            ContactUsScreen(contentPadding = contentPadding)
        }
    }
}

fun NavHostController.navigateAndClearBackStack(
    route: String,
    clearToRoot: Boolean = false
) {
    when {
        clearToRoot -> this.navigate(route) {
            popUpTo(0)
            launchSingleTop = true
        }

        route == LogAWalk.route -> popBackStack(route, inclusive = false)

        else -> navigate(route) {
            popUpTo(LogAWalk.route) {
                inclusive = false
            }
        }
    }
}
