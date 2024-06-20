package org.zotero.android.pdfjs

import android.net.Uri
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import org.zotero.android.R
import org.zotero.android.architecture.ui.CustomLayoutSize

@Composable
fun PdfjsView(
    path: String,
    viewModel: PdfjsViewModel
)
{
    val activity = LocalContext.current as? AppCompatActivity ?: return
    val fragmentManager = activity.supportFragmentManager
    val layoutType = CustomLayoutSize.calculateLayoutType()
    
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
                path = path
            )
            frameLayout
        },
        update = {_ ->}
    )
}