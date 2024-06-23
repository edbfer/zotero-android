package org.zotero.android.pdfjs

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import android.provider.ContactsContract.Data
import androidx.annotation.ColorInt
import org.zotero.android.pdf.data.DatabaseAnnotation
import org.zotero.android.pdf.data.DocumentAnnotation
class PdfjsDocument()
{

    companion object
    {
        fun loadDocument(context: Context, requested_uri: String) : PdfjsDocument
        {
            return PdfjsDocument()
        }
    }

    lateinit var annotationProvider: PdfjsAnnotationProvider

    fun annotation(pageIndex: Int, key: String): DocumentAnnotation {
        return
    }


}

class PdfjsAnnotationProvider()
{
    fun getAllAnnotations() : MutableList<DocumentAnnotation>
    {
        return emptyList<DocumentAnnotation>().toMutableList()
    }

    fun addAnnotationToPage(annotation: DocumentAnnotation)
    {

    }
}