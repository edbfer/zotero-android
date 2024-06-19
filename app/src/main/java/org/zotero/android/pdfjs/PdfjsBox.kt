package org.zotero.android.pdfjs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density


@Composable
internal fun PdfjsBox(
    viewState: PdfjsViewState,
    viewModel: PdfjsViewModel
)
{
    val density = LocalDensity.current
    val rightTargetAreaXOffset = with(density) {92.dp.toPx()}

    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        PdfjsView(viewModel = viewModel)
    }

}