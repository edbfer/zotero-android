package org.zotero.android.screens.settings.linked_files

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.theme.CustomTheme
import org.zotero.android.uicomponents.topbar.NewCustomTopBar
import org.zotero.android.uicomponents.topbar.NewHeadingTextButton

@Composable
internal fun SettingsAccountTopBar(
    onBack: () -> Unit,
) {
    NewCustomTopBar(
        backgroundColor = CustomTheme.colors.surface,
        title = stringResource(id = Strings.settings_linked_files),
        leftContainerContent = listOf {
            NewHeadingTextButton(
                onClick = onBack,
                text = stringResource(Strings.back),
            )
        }
    )
}