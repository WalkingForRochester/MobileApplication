package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.walkingforrochester.walkingforrochester.android.ui.composable.contact.ContactUsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.forgotpassword.ForgotPasswordScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.leaderboard.LeaderboardScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.logawalk.LogAWalkScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.login.LoginScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.newsfeed.NewsFeedScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.profile.ProfileScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.registration.RegistrationScreen
import com.walkingforrochester.walkingforrochester.android.ui.state.RegistrationScreenState

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    loggedIn: Boolean = false,
    onStartWalking: () -> Unit = {},
    onStopWalking: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val startDestination = if (loggedIn) LogAWalk.route else LoginDestination.route
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = LoginDestination.route) {
            LoginScreen(
                onForgotPassword = { navController.navigate(ForgotPassword.route) },
                onRegister = {
                    navController.navigate(Registration.route)
                },
                onRegisterPrefill = { email, firstName, lastName, facebookId ->
                    navController.navigate(
                        Registration.route +
                            "?email=$email" +
                            "&fname=$firstName" +
                            "&lname=$lastName" +
                            "&fbid=$facebookId"
                    ) {
                        popUpTo(LoginDestination.route)
                    }
                },
                onLoginComplete = {
                    navController.navigateSingleTopTo(LogAWalk.route, clearTop = true)
                },
                contentPadding = contentPadding
            )
        }
        composable(route = ForgotPassword.route) {

            ForgotPasswordScreen(
                onPasswordResetComplete = {
                    navController.navigateSingleTopTo(LoginDestination.route, clearTop = true)
                },
                contentPadding = contentPadding
            )
        }
        composable(
            route = Registration.routeWithArgs,
            arguments = Registration.arguments,
        ) { navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("email")
            val firstName = navBackStackEntry.arguments?.getString("fname")
            val lastName = navBackStackEntry.arguments?.getString("lname")
            val facebookId = navBackStackEntry.arguments?.getString("fbid")

            RegistrationScreen(
                initState = RegistrationScreenState(
                    email = email ?: "",
                    firstName = firstName ?: "",
                    lastName = lastName ?: "",
                    facebookId = facebookId
                ),
                onRegistrationComplete = {
                    navController.navigateSingleTopTo(
                        LogAWalk.route,
                        clearTop = true
                    )
                },
                contentPadding = contentPadding
            )
        }
        composable(
            route = Registration.route,
        ) {
            RegistrationScreen(
                onRegistrationComplete = {
                    navController.navigateSingleTopTo(LogAWalk.route, clearTop = true)
                },
                contentPadding = contentPadding
            )
        }
        composable(route = LogAWalk.route) {
            LogAWalkScreen(
                onStartWalking = onStartWalking,
                onStopWalking = onStopWalking,
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
                    navController.navigate(LoginDestination.route) {
                        // Use popUpTo 0 so the user can't go back to being logged in
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                contentPadding = contentPadding
            )
        }
        composable(route = ContactUs.route) {
            ContactUsScreen(contentPadding = contentPadding)
        }
    }
}

fun NavHostController.navigateSingleTopTo(
    route: String,
    clearTop: Boolean = false
) = this.navigate(route) {
    // Use popUpTo(0) to fully clear the top to prevent backing to last destination
    if (clearTop) popUpTo(0) else popUpTo(LogAWalk.route)
    launchSingleTop = true
}
