package org.zotero.android.pdfjs

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.zotero.android.uicomponents.Strings
import org.zotero.android.uicomponents.Drawables
import org.zotero.android.uicomponents.icon.IconWithPadding
import org.zotero.android.uicomponents.icon.ToggleIconWithPadding
import org.zotero.android.uicomponents.theme.CustomTheme
import org.zotero.android.uicomponents.topbar.NewCustomTopBar
import org.zotero.android.uicomponents.topbar.NewHeadingTextButton

@Composable
internal fun PdfjsTopBar(
    onBack: () -> Unit,
    onShowHideSideBar: () -> Unit,
    toPdfSettings: () -> Unit,
    toggleToolbarButton: () -> Unit,
    isToolbarButtonSelected: Boolean,
    showSideBar: Boolean
)
{
    NewCustomTopBar(
        backgroundColor = CustomTheme.colors.surface,
        leftContainerContent = listOf(
            {
                NewHeadingTextButton(
                    text = stringResource(id = Strings.back),
                    onClick = onBack
                )
            },
            {
                ToggleIconWithPadding(
                    drawableRes = Drawables.view_sidebar_24px,
                    isSelected = showSideBar,
                    onToggle = {
                        onShowHideSideBar()
                    }
                )
            }
        ),
        rightContainerContent = listOf(
            {
                ToggleIconWithPadding(
                    drawableRes = Drawables.draw_24px,
                    isSelected = isToolbarButtonSelected,
                    onToggle = toggleToolbarButton
                )
            },
            {
                IconWithPadding(
                    drawableRes = Drawables.settings_24px,
                    onClick = toPdfSettings
                )
            }
        )
    )
}