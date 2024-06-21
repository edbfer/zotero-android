package org.zotero.android.pdf.reader.sidebar

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import org.zotero.android.pdf.data.Annotation
import org.zotero.android.pdf.reader.PdfReaderViewModel
import org.zotero.android.pdf.reader.PdfReaderViewState

@Composable
internal fun PdfjsSidebarImageRow(
    viewModel: PdfReaderViewModel,
    viewState: PdfReaderViewState,
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
    viewModel: PdfReaderViewModel,
    viewState: PdfReaderViewState,
    annotation: Annotation,
    loadPreview: () -> Bitmap?,
) {
    PdfjsSidebarImageSection(loadPreview, viewModel)
    PdfjsSidebarTagsSection(viewModel = viewModel, viewState = viewState, annotation = annotation)
}

@Composable
internal fun PdfjsSidebarNoteRow(
    annotation: Annotation,
    viewModel: PdfReaderViewModel,
    viewState: PdfReaderViewState,
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
    viewModel: PdfReaderViewModel,
    viewState: PdfReaderViewState,
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
