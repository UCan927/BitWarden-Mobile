package com.x8bit.bitwarden.ui.platform.feature.rootnav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.x8bit.bitwarden.ui.auth.feature.auth.AUTH_ROUTE
import com.x8bit.bitwarden.ui.auth.feature.auth.authDestinations
import com.x8bit.bitwarden.ui.auth.feature.auth.navigateToAuth
import com.x8bit.bitwarden.ui.platform.components.PlaceholderComposable
import com.x8bit.bitwarden.ui.platform.feature.vaultunlocked.VAULT_UNLOCKED_ROUTE
import com.x8bit.bitwarden.ui.platform.feature.vaultunlocked.navigateToVaultUnlocked
import com.x8bit.bitwarden.ui.platform.feature.vaultunlocked.vaultUnlockedDestinations

/**
 * Controls root level [NavHost] for the app.
 */
@Composable
fun RootNavScreen(
    viewModel: RootNavViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
    ) {
        splashDestinations()
        authDestinations(navController)
        vaultUnlockedDestinations()
    }

    // When state changes, navigate to different root navigation state
    val rootNavOptions = navOptions {
        // When changing root navigation state, pop everything else off the back stack:
        popUpTo(navController.graph.id) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    // This workaround is for an issue where "launchSingleTop" flag does not work correctly
    // and we "re-navigate" after rotation or process death. To prevent this, we are currently
    // checking the root level route and no-opping the navigation, because we are already there.
    // When upgrading to the latest compose nav version (which we currently aren't doing for
    // other reasons), we can test that this workaround is no longer needed and remove it.
    // To test, remove the (currentRoute == targetRoute) and test that state is saved
    // on process death and rotation (BIT-201).
    val targetRoute = when (state) {
        RootNavState.Auth -> AUTH_ROUTE
        RootNavState.Splash -> SPLASH_ROUTE
        RootNavState.VaultUnlocked -> VAULT_UNLOCKED_ROUTE
    }
    val currentRoute = navController.currentDestination?.routeLevelRoute()

    // Don't navigate if we are already at the correct root:
    if (currentRoute == targetRoute) {
        return
    }

    when (state) {
        RootNavState.Auth -> navController.navigateToAuth(rootNavOptions)
        RootNavState.Splash -> navController.navigateToSplash(rootNavOptions)
        RootNavState.VaultUnlocked -> navController.navigateToVaultUnlocked(rootNavOptions)
    }
}

/**
 * Helper method that returns the highest level route for the given [NavDestination].
 *
 * As noted above, this can be removed after upgrading to latest compose navigation, since
 * the nav args can prevent us from having to do this check.
 */
@Suppress("ReturnCount")
private fun NavDestination?.routeLevelRoute(): String? {
    if (this == null) {
        return null
    }
    if (parent?.route == null) {
        return route
    }
    return parent.routeLevelRoute()
}

/**
 * The functions below should be moved to their respective feature packages once they exist.
 *
 * For an example of how to setup these nav extensions, see NIA project.
 */

/**
 * TODO: move to splash package (BIT-147)
 */
private const val SPLASH_ROUTE = "splash"

/**
 * Add splash destinations to the nav graph.
 *
 * TODO: move to splash package (BIT-147)
 */
private fun NavGraphBuilder.splashDestinations() {
    composable(SPLASH_ROUTE) {
        PlaceholderComposable(text = "Splash")
    }
}

/**
 * Navigate to the splash screen. Note this will only work if splash destination was added
 * via [splashDestinations].
 *
 * TODO: move to splash package (BIT-147)
 *
 */
private fun NavController.navigateToSplash(
    navOptions: NavOptions? = null,
) {
    navigate(SPLASH_ROUTE, navOptions)
}