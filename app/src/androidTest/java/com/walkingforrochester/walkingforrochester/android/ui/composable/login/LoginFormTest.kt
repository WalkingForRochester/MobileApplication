package com.walkingforrochester.walkingforrochester.android.ui.composable.login

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.walkingforrochester.walkingforrochester.android.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginFormTest {
    private lateinit var device: UiDevice

    @Test
    fun testLogin() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Start from the home screen
        device.pressHome()

        // Wait for launcher
        val launcherPackage: String = device.launcherPackageName
        assertThat(launcherPackage, notNullValue())
        device.wait(
            Until.hasObject(By.pkg(launcherPackage).depth(0)),
            LAUNCH_TIMEOUT
        )

        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences(
            context.getString(R.string.wfr_preferences),
            Context.MODE_PRIVATE
        )
        prefs.edit().clear().commit()

        val intent = context.packageManager.getLaunchIntentForPackage(
            context.packageName)?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.pkg(context.packageName).depth(0)),
            LAUNCH_TIMEOUT
        )

        val emailField = device.findObject(By.res("login:email"))
        assertThat(emailField, `is`(notNullValue()))
        emailField.text = "admin@admin.co"

        val passwordField = device.findObject(By.res("login:password"))
        assertThat(passwordField, `is`(notNullValue()))
        passwordField.text = "admin"

        val loginButton = device.findObject(By.res("login:button"))
        assertThat("Login Button", loginButton, `is`(notNullValue()))
        loginButton.click()

        device.waitForIdle()

        Thread.sleep(5000)
    }
    /*@get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testContentDescriptions() {

        var loginScreenState = LoginScreenState()
        // Start the app
        composeTestRule.setContent {
            LoginForm(
                loginScreenState = loginScreenState,
                onEmailAddressValueChange = {
                    loginScreenState = loginScreenState.copy(emailAddress = it)
                },
                onPasswordValueChange = { loginScreenState = loginScreenState.copy(password = it) },
                onPasswordVisibilityChange = {
                    loginScreenState =
                        loginScreenState.copy(passwordVisible = !loginScreenState.passwordVisible)
                }
            )
        }
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithTag("Email Address").assertExists()
        composeTestRule.onNodeWithText(context.getString(R.string.email_address)).performTextInput("Test")
            assertThat(loginScreenState.emailAddress, `is`("Test"))
        composeTestRule.onNodeWithText(context.getString(R.string.email_address)).printToLog("LoginFormTest")
        assertThat(loginScreenState.emailAddress, `is`("Test"))
        composeTestRule.onNodeWithContentDescription("pass").assertExists()
    }*/

    companion object {
        private const val LAUNCH_TIMEOUT = 5000L
    }
}
