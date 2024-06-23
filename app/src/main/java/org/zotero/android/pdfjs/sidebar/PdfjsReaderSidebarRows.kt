package org.zotero.android.pdf.reader.sidebar

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import org.zotero.android.pdf.data.Annotation
import org.zotero.android.pdf.reader.PdfReaderViewModel
import org.zotero.android.pdf.reader.PdfReaderViewState
import org.zotero.android.pdfjs.PdfjsViewModel
import org.zotero.android.pdfjs.PdfjsViewState

@Composable
internal fun PdfjsSidebarImageRow(
    viewModel: PdfjsViewModel,
    viewState: PdfjsViewState,
    annotation: Annotation,
    loadPreview: () -> Bitmap?,
    focusRequester: FocusRequester,
) {
    PdfjsSidebarImageSection(
        loadPreview = loadPreview,
        viewModel = viewModel
    )
    PdfjsSidebarDivider()
    PdfjsSidebarTagsAndCommentsSection(
        annotation = annotation,
        viewModel = viewModel,
        viewState = viewState,
        focusRequester = focusRequester,
        shouldAddTopPadding = true,
    )
}

@Composable
internal fun PdfjsSidebarInkRow(
    viewModel: PdfjsViewModel,
    viewState: PdfjsViewState,
    annotation: Annotation,
    loadPreview: () -> Bitmap?,
) {
    PdfjsSidebarImageSection(loadPreview, viewModel)
    PdfjsSidebarTagsSection(viewModel = viewModel, viewState = viewState, annotation = annotation)
}

@Composable
internal fun PdfjsSidebarNoteRow(
    annotation: Annotation,
    viewModel: PdfjsViewModel,
    viewState: PdfjsViewState,
    focusRequester: FocusRequester,
) {
    PdfjsSidebarTagsAndCommentsSection(
        annotation = annotation,
        viewModel = viewModel,
        viewState = viewState,
        focusRequester = focusRequester,
        shouldAddTopPadding = true,
    )
}

@Composable
internal fun PdfjsSidebarHighlightRow(
    annotation: Annotation,
    viewModel: PdfjsViewModel,
    viewState: PdfjsViewState,
    annotationColor: Color,
    focusRequester: FocusRequester,
) {
    PdfjsSidebarHighlightedTextSection(annotationColor = annotationColor, annotation = annotation)

    PdfjsSidebarTagsAndCommentsSection(
        annotation = annotation,
        viewModel = viewModel,
        viewState = viewState,
        focusRequester = focusRequester,
        shouldAddTopPadding = false,
    )
}
