package org.zotero.android.pdfjs.data

import android.graphics.PointF
import android.graphics.RectF
import org.zotero.android.sync.Tag
import org.zotero.android.database.objects.AnnotationType
import org.zotero.android.ktx.rounded
import org.zotero.android.pdf.data.Annotation
import org.zotero.android.pdf.data.AnnotationBoundingBoxConverter
import org.zotero.android.pdf.data.AnnotationEditability
import org.zotero.android.pdf.reader.AnnotationKey
import org.zotero.android.sync.AnnotationBoundingBoxCalculator
import org.zotero.android.sync.Library

/*interface PdfjsAnnotation
{
    val key: String
    val readerKey: AnnotationKey
    val type: AnnotationType
    val lineWidth: Float?
    val page: Int
    val pageIndex: Int
    val pageLabel: String
    val comment: String
    val color: String
    val text: String?
    val sortIndex: String
    val tags: List<Tag>
    val shouldRenderPreview: Boolean
    val isZoteroAnnotation: Boolean
    val previewId: String
    var boundingBox: RectF

    fun editability(currentUserId: Long, library: Library) : AnnotationEditability
    fun paths(boundingBoxConverter: PdfjsAnnotationBoundingBoxConverter) : List<List<PointF>>
    fun rects(boundingBoxConverter: PdfjsAnnotationBoundingBoxConverter) : List<RectF>
    fun isAuthor(currentUserId: Long) : Boolean
    fun boundingBox(boundingBoxConverter: PdfjsAnnotationBoundingBoxConverter) : RectF
    {
        when(this.type)
        {
            AnnotationType.ink ->
            {
                val paths = paths(boundingBoxConverter = boundingBoxConverter)
                val lineWidth = this.lineWidth ?: 1F
                this.boundingBox = AnnotationBoundingBoxCalculator.boundingBox(
                    paths = paths,
                    lineWidth = lineWidth
                ).rounded(3)
                return this.boundingBox
            }

            AnnotationType.note,
            AnnotationType.highlight,
            AnnotationType.image ->
            {
                val rects = rects(boundingBoxConverter = boundingBoxConverter)
                if (rects.size == 1)
                    return rects[0].rounded(3)
                this.boundingBox = AnnotationBoundingBoxCalculator.boundingBox(rects).rounded(3)
                return this.boundingBox
            }
        }
    }

    fun author(displayName: String, username: String) : String

    val displayColor: String get()
    {
        if(!color.startsWith("#"))
        {
            return "#" + this.color
        }
        return this.color
    }
}*/