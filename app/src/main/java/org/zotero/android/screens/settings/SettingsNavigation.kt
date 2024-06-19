package org.zotero.android.screens.settings

import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.greenrobot.eventbus.EventBus
import org.zotero.android.architecture.EventBusConstants
import org.zotero.android.architecture.navigation.ZoteroNavigation
import org.zotero.android.screens.settings.account.SettingsAccountScreen
import org.zotero.android.screens.settings.debug.SettingsDebugLogScreen
import org.zotero.android.screens.settings.debug.SettingsDebugScreen
import org.zotero.android.screens.settings.linked_files.SettingsLinkedFilesScreen
import org.zotero.android.uicomponents.navigation.ZoteroNavHost

@Composable
internal fun SettingsNavigation(
    onOpenWebpage: (uri: Uri) -> Unit,
    onPathSelect: () -> Unit,
    navigatePdfjs: () -> Unit
    ) {
    val navController = rememberNavController()
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navigation = remember(navController) {
        ZoteroNavigation(navController, dispatcher)
    }
    ZoteroNavHost(
        navController = navController,
        startDestination = SettingsDestinations.SETTINGS,
        modifier = Modifier.navigationBarsPadding(), // do not draw behind nav bar
    ) {
        settingsNavScreens(navigation = navigation, onOpenWebpage = onOpenWebpage, onPathSelect = onPathSelect, navigatePdfjs = navigatePdfjs)
    }
}

internal fun NavGraphBuilder.settingsNavScreens(
    navigation: ZoteroNavigation,
    onOpenWebpage: (uri: Uri) -> Unit,
    onPathSelect: () -> Unit,
    navigatePdfjs: () -> Unit
) {
    settingsScreen(
        onBack = navigation::onBack,
        onOpenWebpage = onOpenWebpage,
        onPathSelect = onPathSelect,
        toAccountScreen = navigation::toAccountScreen,
        toDebugScreen = navigation::toDebugScreen,
        toLinkedFilesScreen = navigation::toLinkedFilesScreen
    )
    accountScreen(onBack = navigation::onBack, onOpenWebpage = onOpenWebpage)
    linkedFilesScreen(onBack = navigation::onBack, onPathSelect = onPathSelect, navigatePdfjs = navigatePdfjs)
    debugScreen(onBack = navigation::onBack, toDebugLogScreen = navigation::toDebugLogScreen)
    debugLogScreen(onBack = navigation::onBack)
}

fun NavGraphBuilder.settingsScreen(
    onOpenWebpage: (uri: Uri) -> Unit,
    onPathSelect: () -> Unit,
    toAccountScreen: () -> Unit,
    toDebugScreen: () -> Unit,
    toLinkedFilesScreen: () -> Unit,
    onBack: () -> Unit,
) {
    composable(
        route = SettingsDestinations.SETTINGS,
    ) {
        SettingsScreen(
            onBack = onBack,
            toAccountScreen = toAccountScreen,
            onOpenWebpage = onOpenWebpage,
            toDebugScreen = toDebugScreen,
            toLinkedFilesScreen = toLinkedFilesScreen
        )
    }
}

private fun NavGraphBuilder.accountScreen(
    onOpenWebpage: (uri: Uri) -> Unit,
    onBack: () -> Unit,
) {
    composable(
        route = SettingsDestinations.ACCOUNT,
    ) {
        SettingsAccountScreen(onBack = onBack, onOpenWebpage = onOpenWebpage)
    }
}

private fun NavGraphBuilder.debugScreen(
    onBack: () -> Unit,
    toDebugLogScreen: () -> Unit,
) {
    composable(
        route = SettingsDestinations.DEBUG,
    ) {
        SettingsDebugScreen(onBack = onBack, toDebugLogScreen = toDebugLogScreen,)
    }
}

private fun NavGraphBuilder.debugLogScreen(
    onBack: () -> Unit,
) {
    composable(
        route = SettingsDestinations.DEBUG_LOG,
    ) {
        SettingsDebugLogScreen(onBack = onBack)
    }
}

private fun NavGraphBuilder.linkedFilesScreen(
    onBack: () -> Unit,
    onPathSelect: () -> Unit,
    navigatePdfjs: () -> Unit,
)
{
    composable(
        route = SettingsDestinations.LINKED_FILES,
    )   {
        SettingsLinkedFilesScreen(onBack = onBack, onPathSelect = onPathSelect, navigatePdfjs = navigatePdfjs)
    }
}


private object SettingsDestinations {
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
    const val DEBUG = "debug"
    const val LINKED_FILES = "linked_files"
    const val DEBUG_LOG = "debugLog"
}

fun ZoteroNavigation.toSettingsScreen() {
    navController.navigate(SettingsDestinations.SETTINGS)
}

fun ZoteroNavigation.toAccountScreen() {
    navController.navigate(SettingsDestinations.ACCOUNT)
}

fun ZoteroNavigation.toDebugScreen() {
    navController.navigate(SettingsDestinations.DEBUG)
}

fun ZoteroNavigation.toDebugLogScreen() {
    navController.navigate(SettingsDestinations.DEBUG_LOG)
}

fun ZoteroNavigation.toLinkedFilesScreen()
{
    navController.navigate(SettingsDestinations.LINKED_FILES)
}