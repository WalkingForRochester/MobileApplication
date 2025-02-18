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
import com.walkingforrochester.walkingforrochester.android.di.AppModule
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class LoginFormTest {
    private lateinit var device: UiDevice

    @After
    fun teardown() {
        // Launch the app
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences(
            AppModule.PREFERENCE_FILE,
            Context.MODE_PRIVATE
        )
        prefs.edit().clear().commit()
    }

    // This tests to make sure UI tests can find the resource ids. These are used by
    // play store to auto populate test account per play store policy
    @Test
    fun testLogin() = runTest {
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
            AppModule.PREFERENCE_FILE,
            Context.MODE_PRIVATE
        )
        prefs.edit().clear().commit()

        val intent = context.packageManager.getLaunchIntentForPackage(
            context.packageName
        )?.apply {
            // Clear out any previous instances
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.pkg(context.packageName).depth(0)),
            LAUNCH_TIMEOUT
        )

        val emailField = device.findObject(By.res("login_email"))
        assertThat(emailField, `is`(notNullValue()))
        emailField.text = "test@email.com"

        val passwordField = device.findObject(By.res("login_password"))
        assertThat(passwordField, `is`(notNullValue()))
        passwordField.text = "test"

        val loginButton = device.findObject(By.res("login_button"))
        assertThat("Login Button", loginButton, `is`(notNullValue()))
        loginButton.click()

        device.waitForIdle()

        Thread.sleep(2000)
    }

    companion object {
        private const val LAUNCH_TIMEOUT = 5000L
    }
}
