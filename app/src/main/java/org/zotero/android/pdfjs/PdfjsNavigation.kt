package org.zotero.android.pdfjs

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import org.zotero.android.architecture.navigation.ZoteroNavigation

internal fun NavGraphBuilder.pdfjsScreenAndNavigationTablet(
    navigation: ZoteroNavigation
)
{
    pdfjsScreen(onBack = navigation::onBack)
}

internal fun NavGraphBuilder.pdfjsScreenAndNavigationPhone(
    navigation: ZoteroNavigation
)
{
    pdfjsScreen(onBack = navigation::onBack)
}

private object PdfjsDestinations {
    const val PDFJS_SCREEN = "pdfjsScreen"
}

fun ZoteroNavigation.toPdfjsScreen(
    context: Context
)
{
    navController.navigate(PdfjsDestinations.PDFJS_SCREEN)
}

private fun NavGraphBuilder.pdfjsScreen(
    onBack: () -> Unit
)
{
    composable(
        route = PdfjsDestinations.PDFJS_SCREEN
    )
    {
        PdfjsScreen(onBack = onBack)
    }
}