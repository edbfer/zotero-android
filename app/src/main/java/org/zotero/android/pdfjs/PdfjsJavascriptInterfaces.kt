package org.zotero.android.pdfjs

import kotlinx.serialization.Serializable

@Serializable
data class PageOverviewItem(
    val height: Int,
    val width: Int,
    val rotation: Int
)