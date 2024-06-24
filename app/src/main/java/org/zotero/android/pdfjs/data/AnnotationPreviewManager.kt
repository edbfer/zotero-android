package org.zotero.android.pdfjs.data

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.zotero.android.architecture.coroutines.Dispatchers
import org.zotero.android.database.objects.AnnotationType
import org.zotero.android.files.FileStore
import org.zotero.android.pdf.cache.AnnotationPreviewMemoryCache
import org.zotero.android.pdfjs.PdfjsDocument
import org.zotero.android.sync.LibraryIdentifier
import java.io.FileOutputStream
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

/*@Singleton
class PdfjsAnnotationPreviewManager @Inject constructor(
    dispatchers: Dispatchers,
    private val fileStore: FileStore,
    private val memoryCache: AnnotationPreviewMemoryCache,
    private val context: Context
)
{
    private val currentlyProcessingAnnotations = Collections.synchronizedSet(mutableSetOf<String>())
    private val coroutineScope = CoroutineScope(dispatchers.default)

    fun store(
        rawDocument: PdfjsDocument,
        annotation: PdfjsAnnotation,
        parentKey: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean,
        annotationMaxSideSize: Int
    )
    {
        if (!annotation.shouldRenderPreview || !annotation.isZoteroAnnotation)
        {
            return
        }

        enqueue(
            annotation = annotation,
            key = annotation.previewId,
            parentKey = parentKey,
            libraryId = libraryId,
            isDark = isDark,
            bitmapSize = annotationMaxSideSize,
            rawDocument = rawDocument
        )
    }

    fun delete(
        annotation: PdfjsAnnotation,
        parentKey: String,
        libraryId: LibraryIdentifier
    )
    {
        if (!annotation.shouldRenderPreview || !annotation.isZoteroAnnotation) {
            return
        }

        val key = annotation.previewId
        fileStore.annotationPreview(
            annotationKey = key,
            pdfKey = parentKey,
            libraryId = libraryId,
            isDark = true
        ).delete()
        fileStore.annotationPreview(
            annotationKey = key,
            pdfKey = parentKey,
            libraryId = libraryId,
            isDark = false
        ).delete()
    }

    fun deleteAll(
        parentKey: String,
        libraryId: LibraryIdentifier
    )
    {
        fileStore.annotationPreviews(pdfKey = parentKey, libraryId = libraryId).deleteRecursively()
    }

    fun hasPreview(
        key: String,
        parentKey: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean
    ) : Boolean
    {
        return fileStore.annotationPreview(
            annotationKey = key,
            pdfKey = parentKey,
            libraryId = libraryId,
            isDark = isDark
        ).exists()
    }

    private fun enqueue(
        rawDocument: PdfjsDocument,
        annotation: PdfjsAnnotation,
        key: String,
        parentKey: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean = false,
        bitmapSize: Int
    ) = coroutineScope.launch {
        if (currentlyProcessingAnnotations.contains(key))
        {
            return@launch
        }
        currentlyProcessingAnnotations.add(key)

        val resultBitmap: Bitmap = generateRawDocumentBitmap(
            annotation = annotation,
            rawDocument = rawDocument,
            maxSide = bitmapSize
        )

        val shouldDrawAnnotation = annotation.type == AnnotationType.ink
        if (shouldDrawAnnotation)
        {
            drawAnnotationOnBitmap(resultBitmap, annotation)
        }
        completeRequest(
            bitmap = resultBitmap,
            key = key,
            parentKey = parentKey,
            libraryId = libraryId,
            isDark = isDark
        )
        currentlyProcessingAnnotations.remove(key)
    }

    private fun completeRequest(
        bitmap: Bitmap,
        key: String,
        parentKey: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean
    )
    {
        cache(
            bitmap = bitmap,
            key = key,
            pdfKey = parentKey,
            libraryId = libraryId,
            isDark = isDark
        )
    }

    private fun cache(
        bitmap: Bitmap,
        key: String,
        pdfKey: String,
        libraryId: LibraryIdentifier,
        isDark: Boolean
    )
    {
        val tempFile = fileStore.generateTempFile()
        val fileStream = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileStream)
        fileStream.close()

        val finalFile = fileStore.annotationPreview(
            annotationKey = key,
            pdfKey = pdfKey,
            libraryId = libraryId,
            isDark = isDark
        )
        tempFile.renameTo(finalFile)
    }

    private fun generateRawDocumentBitmap(
        annotation: PdfjsAnnotation,
        rawDocument: PdfjsDocument,
        maxSide: Int
    ) : Bitmap
    {
        val annotationRect = annotation.boundingBox
        val width = (annotationRect.right - annotationRect.left).toInt()
        val height = (annotationRect.top - annotationRect.bottom).toInt()

        val documentSize = rawDocument.getPageSize(annotation.pageIndex)
        val rawDocumentBitmap: Bitmap = rawDocument.renderPageToBitmap(
            context,
            annotation.pageIndex,
            width,
            height,
            annotationRect
        )

        val scaleX = width / maxSide.toDouble()
        val scaleY = height / maxSide.toDouble()
        val resultScale = scaleX.coerceAtLeast(scaleY)
        val resultVideoViewWidth = (width / resultScale).toInt()
        val resultVideoViewHeight = (height / resultScale).toInt()
        return rawDocumentBitmap.scale(resultVideoViewWidth, resultVideoViewHeight, true)
    }
}*/

