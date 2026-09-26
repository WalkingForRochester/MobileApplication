package com.walkingforrochester.walkingforrochester.android.ui.composable.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.walkingforrochester.walkingforrochester.android.model.AccountProfile
import com.walkingforrochester.walkingforrochester.android.ui.composable.login.LoginScreen
import com.walkingforrochester.walkingforrochester.android.ui.composable.registration.RegistrationScreen
import timber.log.Timber

@Composable
fun RootNavDisplay(
    backStack: NavBackStack<RootRoute>,
    modifier: Modifier = Modifier,
    onToggleDarkMode: (Boolean) -> Unit = {}
) {
    val navigateBack: () -> Unit = {

        Timber.d(
            "JSR backstack size: %d  last: %s",
            backStack.size,
            backStack.last()
        )
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { navigateBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginRoute> {
                    LoginScreen(
                        onForgotPassword = { backStack.add(ForgotPasswordRoute) },
                        onRegister = { email, firstName, lastName, facebookId ->
                            backStack.add(
                                RegistrationRoute(
                                    email,
                                    firstName,
                                    lastName,
                                    facebookId
                                )
                            )
                        },
                        onLoginComplete = {
                            backStack.clear()
                            backStack.add(HomeRoute)
                        }
                    )
            }
            entry<ForgotPasswordRoute> {
                Text("This is forgot password")
            }
            entry<RegistrationRoute> { registrationRoute ->
                RegistrationScreen(
                    profile = AccountProfile.DEFAULT_PROFILE.copy(
                        email = registrationRoute.email,
                        firstName = registrationRoute.firstName,
                        lastName = registrationRoute.lastName,
                        facebookId = registrationRoute.facebookId
                    ),
                    onNavigateBack = navigateBack,
                    onRegistrationComplete = {
                        backStack.clear()
                        backStack.add(HomeRoute)
                    }
                )
            }
            entry<HomeRoute> {
                Text("This is home")
            }
            /*entry<Home> {
                HomeScreen(
                    contentPadding = contentPadding,
                    onViewShowDetails = { show ->
                        backStack.add(
                            PlayList(
                                startTime = show.startTime,
                                endTime = show.endTime
                            )
                        )
                    },
                    onViewArchive = { backStack.add(ArchivedShows) },
                )
            }

            entry<PlayList> { key ->
                PlaylistScreen(
                    startTime = key.startTime,
                    endTime = key.endTime,
                    navigateBack = navigateBack,
                    selectPlay = { playId ->
                        backStack.add(PlayDetail(playId))
                    },
                    contentPadding = contentPadding
                )
            }

            entry<PlayDetail> { key ->
                ExpandedPlayScreen(
                    playId = key.playId,
                    navigateBack = navigateBack,
                    contentPadding = contentPadding
                )
            }

            entry<ArchivedShows> {
                ArchivedShowsScreen(
                    navigateBack = navigateBack,
                    contentPadding = contentPadding,
                    onViewShowDetails = { show ->
                        backStack.add(
                            PlayList(
                                startTime = show.startTime,
                                endTime = show.endTime
                            )
                        )
                    }
                )
            }*/
        }
    )
}