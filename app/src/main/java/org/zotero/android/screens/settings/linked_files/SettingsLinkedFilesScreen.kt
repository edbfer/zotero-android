package org.zotero.android.screens.settings.linked_files

import android.media.metrics.Event
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.zotero.android.architecture.EventBusConstants
import org.zotero.android.screens.settings.SettingsDivider
import org.zotero.android.screens.settings.SettingsItem
import org.zotero.android.screens.settings.SettingsSection
import org.zotero.android.screens.settings.SettingsSectionTitle
import org.zotero.android.uicomponents.CustomScaffold
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.theme.CustomPalette
import org.zotero.android.uicomponents.theme.CustomTheme
import org.zotero.android.uicomponents.theme.CustomThemeWithStatusAndNavBars

@Composable
internal fun SettingsLinkedFilesScreen(
    onBack: () -> Unit,
    onPathSelect: () -> Unit,
    navigatePdfjs: () -> Unit,
    viewModel: SettingsLinkedFilesViewModel = hiltViewModel(),
) {
    val backgroundColor = CustomTheme.colors.zoteroItemDetailSectionBackground
    CustomThemeWithStatusAndNavBars(
        navBarBackgroundColor = backgroundColor,
    ) {
        val viewState by viewModel.viewStates.observeAsState(SettingsLinkedFilesViewState())
        val viewEffect by viewModel.viewEffects.observeAsState()
        LaunchedEffect(key1 = viewModel) {
            viewModel.init()
        }

        LaunchedEffect(key1 = viewEffect) {
            when (val consumedEffect = viewEffect?.consume()) {
                null -> Unit
                is SettingsLinkedFilesViewEffect.OnBack -> {
                    onBack()
                }
            }
        }
        CustomScaffold(
            backgroundColor = CustomTheme.colors.popupBackgroundContent,
            topBar = {
                SettingsAccountTopBar(
                    onBack = onBack,
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                SettingsSectionTitle(titleId = Strings.settings_linked_files)
                SettingsSection {
                    SettingsItem(title = viewState.root_path
                        , onItemTapped = {viewModel.onPathSelection(onPathSelect)})
                }
                Spacer(modifier = Modifier.height(30.dp))
                SettingsSectionTitle(titleId = Strings.settings_linked_files_testpdfjs)
                SettingsSection {
                    SettingsItem(title = viewState.navigate_pdfjs,
                        onItemTapped = {viewModel.onNavigatePdfjs(navigatePdfjs)})
                    
                }
                SettingsSectionTitle(titleId = Strings.settings_use_pdfjs)
                SettingsSection {
                    SettingsItem(title = viewState.use_pdfjs.toString(), onItemTapped = viewModel::onUsePdfjs)
                }
            }
        }
    }
}