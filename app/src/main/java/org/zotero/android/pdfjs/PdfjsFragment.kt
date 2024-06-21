package org.zotero.android.pdfjs

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import androidx.webkit.WebViewClientCompat
import org.zotero.android.R
import org.zotero.android.screens.root.RootViewModel
import timber.log.Timber
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class PdfjsFragment @Inject constructor(
    private val path : String
) : Fragment(R.layout.pdfjs_fragment)
{
    private lateinit var rootView: View
    private lateinit var webView: WebView
    private var isPdfLoaded: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.rootView = inflater.inflate(R.layout.pdfjs_fragment, container, false)
        return this.rootView
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun callbackOnPageLoaded()
    {
        if(!isPdfLoaded) {
            Timber.e("Page Loaded!")

            val docFile = DocumentFile.fromTreeUri(requireContext(), Uri.parse(path))
            val stream = requireContext().contentResolver.openInputStream(Uri.parse(path))
            val encodedData = Base64.encode(stream!!.readBytes())

            //HIDE TOOLBAR
            this.webView.evaluateJavascript("document.getElementById('toolbarContainer').hidden = true;", null)
            this.webView.evaluateJavascript(
                "d = atob('${encodedData}'); PDFViewerApplication.open({data: d});")
                {result ->
                    if(result != null)
                        this.isPdfLoaded = true
                }
        }
    }

    override fun onStart() {
        super.onStart()

        this.webView = rootView.findViewById<WebView>(R.id.pdfjs_webview)
        this.webView.settings.javaScriptEnabled = true

        //load from storage
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(requireContext()))
            .addPathHandler("/res/", ResourcesPathHandler(requireContext()))
            .build()

        this.webView.webViewClient = LocalContentWebViewClient(assetLoader, this::callbackOnPageLoaded)
        this.webView.loadUrl("https://appassets.androidplatform.net/assets/web/viewer.html?file=")

        this.isPdfLoaded = false
    }
}

private class PdfjsJSInterface(
    private val context: Context
)
{
}

private class LocalContentWebViewClient
(
    private val assetLoader : WebViewAssetLoader,
    private val callbackPageLoaded: () -> Unit
) : WebViewClientCompat()
{
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(request.url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        callbackPageLoaded()
    }

}