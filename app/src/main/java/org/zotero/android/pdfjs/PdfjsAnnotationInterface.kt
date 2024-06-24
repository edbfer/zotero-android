package org.zotero.android.pdfjs

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import android.provider.ContactsContract.Data
import androidx.annotation.ColorInt
import org.zotero.android.pdf.data.DatabaseAnnotation
import org.zotero.android.pdf.data.DocumentAnnotation
//import org.zotero.android.pdfjs.data.PdfjsAnnotation
import javax.inject.Inject

class PdfjsDocument @Inject constructor(
    private val pageSizeArray: List<RectF>,
    private val numPages: Int,
)
{
    companion object
    {
        fun loadDocument(context: Context, requested_uri: String) : PdfjsDocument
        {
            return PdfjsDocument(
                pageSizeArray = emptyList(),
                numPages = 0
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

/*class PdfjsAnnotationProvider()
{
    fun getAllAnnotations() : MutableList<PdfjsAnnotation>
    {
        return emptyList<PdfjsAnnotation>().toMutableList()
    }

    fun addAnnotationToPage(annotation: PdfjsAnnotation)
    {

    }
}*/