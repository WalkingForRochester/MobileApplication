package com.walkingforrochester.walkingforrochester.android

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.compose.ui.platform.UriHandler
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.walkingforrochester.walkingforrochester.android.ktx.safeStartActivity
import timber.log.Timber

class CustomTabsManager(
    private val application: Application,
    private val lifecycle: Lifecycle,
) : DefaultLifecycleObserver {

    private var customTabsAvailable = false
    private var serviceConnection: TabServiceConnection? = null
    private var client: CustomTabsClient? = null
    private var session: CustomTabsSession? = null

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        bindService()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        unbindService()
    }

    private fun bindService() {
        val packageName = CustomTabsClient.getPackageName(application, defaultPackages)
        if (!packageName.isNullOrBlank()) {
            val newServiceConnection = TabServiceConnection()
            this.serviceConnection = newServiceConnection
            customTabsAvailable =
                CustomTabsClient.bindCustomTabsService(
                    application,
                    packageName,
                    newServiceConnection
                )

            if (!customTabsAvailable) {
                Timber.w("Failed to bind custom tab service")
            }
        } else {
            Timber.w("Failed to find custom tab package")
        }
    }

    private fun unbindService() {
        serviceConnection?.let {
            application.unbindService(it)
        }
        customTabsAvailable = false
        serviceConnection = null
        session = null
        client = null
    }

    private fun rebind() {
        unbindService()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            bindService()
        }
    }

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise falls back to opening in a external browser.
     *
     * @param context     The context to use to start the custom tab.
     * @param uriString   the uri to display via custom tabs
     * @param isDarkMode  Whether or not the app is in dark mode
     * @param toolbarColor provide the color for the toolbar
     * @param navigationBarColor the color for the navigation bar, or will be same as toolbar
     * @param navigationBarDividerColor the divider color between content and navigation bar
     * @return success     true if chrome tab or browser shown, false if unable to show content
     */
    private fun openCustomTab(
        context: Context,
        uriString: String,
        isDarkMode: Boolean,
        @ColorInt toolbarColor: Int,
        @ColorInt navigationBarColor: Int = toolbarColor,
        @ColorInt navigationBarDividerColor: Int = Color.TRANSPARENT
    ) {

        Timber.d("Viewing: %s", uriString)

        // Attempt to build session if not already built
        if (session == null) {
            session = client?.newSession(null)
        }

        val session = session
        val referrer = Uri.parse("android-app://${context.packageName}")

        val uri = Uri.parse(uriString)

        // If able to bind to a custom tab service, then indicate the url to load
        if (customTabsAvailable && session != null) {

            val colorScheme =
                if (isDarkMode) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT
            val schemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(toolbarColor)
                .setNavigationBarColor(navigationBarColor)
                .setNavigationBarDividerColor(navigationBarDividerColor)
                .build()
            val customTabsIntent = CustomTabsIntent.Builder(session)
                .setShowTitle(true)
                .setColorScheme(colorScheme)
                .setDefaultColorSchemeParams(schemeParams)
                .build()

            customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, referrer)

            try {
                customTabsIntent.launchUrl(context, uri)
                return
            } catch (e: Exception) {
                Timber.w(e, "Failed to start custom tabs intent. Falling back to browser")
            }
        }

        // Send to browser if not available
        Timber.i("openCustomTab: using browser. Custom tabs not available")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            putExtra(Intent.EXTRA_REFERRER, referrer)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.safeStartActivity(intent)
    }

    fun createUriHandler(
        context: Context,
        isDarkMode: Boolean,
        toolbarColor: Int,
        navigationBarColor: Int = toolbarColor,
        navigationBarDividerColor: Int = Color.TRANSPARENT
    ): UriHandler {
        return CustomTabUriHandler(
            customTabsManager = this,
            context = context,
            isDarkMode = isDarkMode,
            toolbarColor = toolbarColor,
            navigationBarColor = navigationBarColor,
            navigationBarDividerColor = navigationBarDividerColor
        )
    }

    internal inner class TabServiceConnection : CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(
            componentName: ComponentName,
            customTabsClient: CustomTabsClient
        ) {
            try {
                customTabsClient.warmup(0)
                client = customTabsClient
            } catch (e: Throwable) {
                Timber.e(e)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            session = null
            client = null
            Timber.w("Custom tab service disconnected: %s", name)
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
            customTabsAvailable = false
            Timber.w("Custom tab service binding died for: %s", name)
            rebind()
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            customTabsAvailable = false
            Timber.w("Custom tab service: null binding for: %s", name)
            unbindService()
        }
    }

    companion object {

        private const val STABLE_PACKAGE = "com.android.chrome"
        private const val BETA_PACKAGE = "com.chrome.beta"
        private const val DEV_PACKAGE = "com.chrome.dev"
        private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"

        private val defaultPackages =
            arrayListOf(STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE, LOCAL_PACKAGE)
    }

    private class CustomTabUriHandler(
        private val context: Context,
        private val customTabsManager: CustomTabsManager,
        private val isDarkMode: Boolean,
        private val toolbarColor: Int,
        private val navigationBarColor: Int,
        private val navigationBarDividerColor: Int
    ) : UriHandler {
        override fun openUri(uri: String) {
            customTabsManager.openCustomTab(
                context = context,
                uriString = uri,
                isDarkMode = isDarkMode,
                toolbarColor = toolbarColor,
                navigationBarColor = navigationBarColor,
                navigationBarDividerColor = navigationBarDividerColor
            )
        }
    }
}