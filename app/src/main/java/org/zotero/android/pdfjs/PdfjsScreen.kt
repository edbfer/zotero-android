package org.zotero.android.pdfjs

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.architecture.ui.ObserveLifecycleEvent
import org.zotero.android.uicomponents.CustomScaffold
import org.zotero.android.uicomponents.theme.CustomTheme
import org.zotero.android.uicomponents.theme.CustomThemeWithStatusAndNavBars

@Composable
internal fun PdfjsScreen(
    onBack: () -> Unit,
    viewModel: PdfjsViewModel = hiltViewModel()
)
{
    //theme
    //viewModel.setOsTheme(isDark = isSystemInDarkTheme())

    val viewState by viewModel.viewStates.observeAsState(PdfjsViewState())
    val viewEffect by viewModel.viewEffects.observeAsState()
    val activity = LocalContext.current as? AppCompatActivity ?: return
    ObserveLifecycleEvent { event ->
        when(event)
        {
            Lifecycle.Event.ON_STOP -> {viewModel.onStop(activity.isChangingConfigurations)}
            else -> {}
        }
    }

    CustomThemeWithStatusAndNavBars(isDarkTheme = viewState.isDark) {
        //val params = viewModel.screenArgs
        val lazyListState = rememberLazyListState()
        val layoutType = CustomLayoutSize.calculateLayoutType()
        val focusRequester: FocusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        LaunchedEffect(key1 = viewEffect) {
            when (val consumedEffect = viewEffect?.consume()) {
                is PdfjsViewEffect.NavigateBack -> {
                    onBack()
                }

                else -> {}
            }
        }

        CustomScaffold(
            backgroundColor = CustomTheme.colors.pdfAnnotationsTopbarBackground,
            topBar = {
                AnimatedContent(targetState = viewState.isTopBarVisible, label = "")
                {isTopBarVisible ->
                    if (isTopBarVisible)
                    {
                        PdfjsTopBar(
                            onBack = onBack,
                            onShowHideSideBar = { /*TODO*/ },
                            toPdfSettings = { /*TODO*/ },
                            toggleToolbarButton = { /*TODO*/ },
                            isToolbarButtonSelected = viewState.showCreationToolbar,
                            showSideBar = viewState.showSideBar
                        )
                    }
                }
            }
        )
        {
            if (layoutType.isTablet())
            {
                PdfjsTabletMode(
                    viewState = viewState,
                    viewModel = viewModel,
                    lazyListState = lazyListState,
                    layoutType = layoutType,
                    focusRequester = focusRequester
                )
            }
            else
            {
                //TODO: Phone layout
                PdfjsTabletMode(
                    viewState = viewState,
                    viewModel = viewModel,
                    lazyListState = lazyListState,
                    layoutType = layoutType,
                    focusRequester = focusRequester
                )
            }
        }
    }
}

@Composable
private fun PdfjsTabletMode(
    viewState: PdfjsViewState,
    viewModel: PdfjsViewModel,
    lazyListState: LazyListState,
    layoutType: CustomLayoutSize.LayoutType,
    focusRequester: FocusRequester
)
{
    PdfjsBox(
        viewModel = viewModel,
        viewState = viewState
    )
}