package org.zotero.android.pdf.reader.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.zotero.android.architecture.ui.CustomLayoutSize
import org.zotero.android.database.objects.AnnotationType
import org.zotero.android.pdfjs.PdfjsBottomPanel
import org.zotero.android.pdfjs.PdfjsViewModel
import org.zotero.android.pdfjs.PdfjsViewState
import org.zotero.android.pdfjs.sidebar.PdfjsSidebarSearchBar
import org.zotero.android.uicomponents.foundation.safeClickable
import org.zotero.android.uicomponents.theme.CustomTheme

@Composable
internal fun PdfjsReaderSidebar(
    viewState: PdfjsViewState,
    layoutType: CustomLayoutSize.LayoutType,
    viewModel: PdfjsViewModel,
    focusRequester: FocusRequester,
    lazyListState: LazyListState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomTheme.colors.pdfAnnotationsFormBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = layoutType.calculateAllItemsBottomPanelHeight())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            PdfjsSidebarSearchBar(viewState = viewState, viewModel = viewModel)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.Absolute.spacedBy(13.dp),
            ) {
                itemsIndexed(
                    items = viewModel.viewState.sortedKeys
                ) { _, key ->
                    val annotation = viewModel.annotation(key) ?: return@itemsIndexed
                    val isSelected = viewState.isAnnotationSelected(annotation.key)
                    val horizontalPadding = if (isSelected) 13.dp else 16.dp
                    var rowModifier: Modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(CustomTheme.colors.pdfAnnotationsItemBackground)

                    if (isSelected) {
                        rowModifier = rowModifier.border(
                            width = 3.dp,
                            color = CustomTheme.colors.zoteroDefaultBlue,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(
                        modifier = rowModifier
                            .safeClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { /*viewModel.selectAnnotation(key)*/ },
                            )
                    ) {
                        val annotationColor =
                            Color(android.graphics.Color.parseColor(annotation.displayColor))
                        val loadPreview = {
                            val preview =
                                viewModel.annotationPreviewMemoryCache.getBitmap(annotation.key)
                            if (preview == null) {
                                viewModel.loadPreviews(listOf(annotation.key))
                            }
                            preview
                        }

                        PdfjsSidebarHeaderSection(
                            annotation = annotation,
                            annotationColor = annotationColor,
                            viewState = viewState,
                            viewModel = viewModel,
                        )
                        PdfjsSidebarDivider()
//                        Spacer(modifier = Modifier.height(8.dp))

                        when (annotation.type) {
                            AnnotationType.note -> PdfjsSidebarNoteRow(
                                annotation = annotation,
                                viewModel = viewModel,
                                viewState = viewState,
                                focusRequester = focusRequester,
                            )

                            AnnotationType.highlight -> PdfjsSidebarHighlightRow(
                                annotation = annotation,
                                annotationColor = annotationColor,
                                viewModel = viewModel,
                                viewState = viewState,
                                focusRequester = focusRequester,
                            )

                            AnnotationType.ink -> PdfjsSidebarInkRow(
                                viewModel = viewModel,
                                viewState = viewState,
                                annotation = annotation,
                                loadPreview = loadPreview,
                            )

                            AnnotationType.image -> PdfjsSidebarImageRow(
                                viewModel = viewModel,
                                viewState = viewState,
                                annotation = annotation,
                                loadPreview = loadPreview,
                                focusRequester = focusRequester,
                            )
                        }
                    }
                }
            }
        }
        PdfjsBottomPanel(
            layoutType = layoutType,
            viewModel = viewModel,
            viewState = viewState
        )
    }
}
