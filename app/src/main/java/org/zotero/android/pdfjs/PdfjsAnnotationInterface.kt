package org.zotero.android.pdfjs

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import android.provider.ContactsContract.Data
import androidx.annotation.ColorInt
import org.zotero.android.pdf.data.DatabaseAnnotation
import org.zotero.android.pdf.data.DocumentAnnotation
import org.zotero.android.pdfjs.data.PdfjsAnnotation
//import org.zotero.android.pdfjs.data.PdfjsAnnotation
import javax.inject.Inject

class PdfjsDocument @Inject constructor(
    private val pageSizeArray: List<RectF>,
    private val pageLabelsArray: List<String>,
    private val numPages: Int,
)
{
    companion object
    {
        fun loadDocument(context: Context, requested_uri: String) : PdfjsDocument
        {
            return PdfjsDocument(
                pageSizeArray = emptyList(),
                pageLabelsArray = emptyList(),
                numPages = 0
            )
        }

        suspend fun build(fragment: PdfjsFragment) : PdfjsDocument
        {
            return PdfjsDocument(
                numPages = fragment.getPageNumber(),
                pageSizeArray = fragment.getPageSizeMap(),
                pageLabelsArray = fragment.getPageLabelsMap()
            )
        }
    }

    //lateinit var annotationProvider: PdfjsAnnotationProvider

   /* fun annotation(pageIndex: Int, key: String): PdfjsAnnotation {
        return null
    }*/

    fun getPageSize(pageIndex: Int) : RectF
    {
        return pageSizeArray[pageIndex]
    }
}

class PdfjsAnnotationProvider(
    private var fragmentGetAllAnnotations: () -> Unit,
)
{
    fun getAllAnnotations() : MutableList<PdfjsAnnotation>
    {
        return emptyList<PdfjsAnnotation>().toMutableList()
    }

    fun addAnnotationToPage(annotation: PdfjsAnnotation)
    {

    }
}