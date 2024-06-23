package org.zotero.android.pdfjs

import org.zotero.android.sync.Library

data class PdfjsReaderArgs(
    val path: String,
    val key: String,
    val library: Library,
    val preselectedAnnotationKey: String?
)