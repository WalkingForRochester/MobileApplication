package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.walkingforrochester.walkingforrochester.android.ui.composable.aboutus.AboutUsScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.calendar.GuidelinesScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.faq.FAQScreen
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
    onStartWalking: () -> Unit,
    onStopWalking: () -> Unit
) {
    val startDestination = if (loggedIn) Destination.LogAWalk.route else Destination.Login.route
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        composable(route = Destination.Login.route) {
            LoginScreen(onForgotPassword = { navController.navigate(Destination.ForgotPassword.route) },
                onRegister = {
                    navController.navigate(Destination.Registration.route)
                },
                onRegisterPrefill = { email, firstName, lastName, facebookId ->
                    navController.navigate(
                        Destination.Registration.route +
                                "?email=$email" +
                                "&fname=$firstName" +
                                "&lname=$lastName" +
                                "&fbid=$facebookId"
                    ) {
                        popUpTo(Destination.Login.route)
                    }
                },
                onLoginComplete = { navController.navigateSingleTopTo(Destination.LogAWalk.route) })
        }
        composable(route = Destination.ForgotPassword.route) {

            ForgotPasswordScreen(onPasswordResetComplete = {
                navController.navigate(Destination.Login.route)
            })
        }
        composable(
            route = Destination.Registration.routeWithArgs,
            arguments = Destination.Registration.arguments,
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
                onRegistrationComplete = { navController.navigateSingleTopTo(Destination.LogAWalk.route) })
        }
        composable(
            route = Destination.Registration.route,
        ) {
            RegistrationScreen(onRegistrationComplete = {
                navController.navigateSingleTopTo(
                    Destination.LogAWalk.route
                )
            })
        }
        composable(route = Destination.LogAWalk.route) {
            LogAWalkScreen(onStartWalking = onStartWalking, onStopWalking = onStopWalking)
        }
        composable(route = Destination.Leaderboard.route) {
            LeaderboardScreen()
        }
        composable(route = Destination.NewsFeed.route) {
            NewsFeedScreen()
        }
        composable(route = Destination.Profile.route) {
            ProfileScreen(onLogoutComplete = {
                navController.navigate(Destination.Login.route) {
                    popUpTo(0)
                    launchSingleTop
                }
            })
        }
        composable(route = Destination.Calendar.route) {
            GuidelinesScreen()
        }
        composable(route = Destination.FAQ.route) {
            FAQScreen()
        }
        composable(route = Destination.AboutUs.route) {
            AboutUsScreen()
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) = this.navigate(route) {
    popUpTo(Destination.LogAWalk.route)
    launchSingleTop = true
}
