package org.zotero.android.pdfjs

import android.net.Uri
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import android.content.res.Resources
import org.zotero.android.R
import org.zotero.android.architecture.ui.CustomLayoutSize
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

@Composable
fun PdfjsView(
    path: String,
    viewModel: PdfjsViewModel
)
{
    val activity = LocalContext.current as? AppCompatActivity ?: return
    val fragmentManager = activity.supportFragmentManager
    val layoutType = CustomLayoutSize.calculateLayoutType()
    val annotationMaxSideSize = annotationMaxSideSize()
    
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val frameLayout = FrameLayout(context)

            val containerId = R.id.container
            val fragmentContainerView = FragmentContainerView(context).apply {
                id = containerId
            }
            frameLayout.addView(fragmentContainerView)

            viewModel.init(
                isTablet = layoutType.isTablet(),
                containerId = fragmentContainerView.id,
                fragmentManager = fragmentManager,
                annotationMaxSideSize = annotationMaxSideSize,
                path = path
            )
            frameLayout
        },
        update = {_ ->}
    )
}

@Composable
private fun annotationMaxSideSize(): Int
{
    val layoutType = CustomLayoutSize.calculateLayoutType()
    val context = LocalContext.current
    val outValue = TypedValue()
    context.resources.getValue(R.dimen.pdf_sidebar_width_percent, outValue, true)
    val sidebatWidthPercentage = outValue.float
    val metricsWidthPixels = Resources.getSystem().displayMetrics.widthPixels
    val annotationSize = metricsWidthPixels * sidebatWidthPercentage
    val result = annotationSize.toInt()
    if (result <= 0)
    {
        return if (layoutType.isTablet())
        {
            480
        } else
        {
            1080
        }
    }
    return result
}