package org.zotero.android.pdfjs

import kotlinx.serialization.Serializable

@Serializable
data class PageOverviewItem(
    val height: Int,
    val width: Int,
    val rotation: Int
)

@Serializable
data class jsBorderStyleObject(
    val dashArray: List<Int>,
    val horizontalCornerRadius: Float,
    val style: Int,
    val verticalCornerRadius: Float,
    val width: Int
)

@Serializable
data class jsContentsObject(
    val str: String,
    val dir: String
)

@Serializable
data class jsPoint(
    val x: Float,
    val y: Float
)

@Serializable
data class jsAnnotationObject(
    val annotationFlags: Int,
    val annotationType: Int,
    val backgroundColor: Int?,
    val borderColor: Int?,
    val borderStyle: jsBorderStyleObject,
    val color: List<Int>,
    val contentsObject: jsContentsObject,
    val creationDate: String?,
    val hasAppearance: Boolean,
    val hasOwnCanvas: Boolean,
    val id: String,
    val modificationDate: String,
    val noHTML: Boolean,
    val noRotate: Boolean,
    val popupRef: String,
    val quadPoints: List<List<jsPoint>>?,
    val inkLists: List<List<jsPoint>>?,
    val rect: List<Float>,
    val rotation: Float,
    val subtype: String,
    val titleObj: jsContentsObject
)