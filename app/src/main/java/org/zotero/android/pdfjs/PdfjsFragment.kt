package org.zotero.android.pdfjs

import android.content.Context
import android.graphics.RectF
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import okhttp3.internal.wait
import org.zotero.android.R
//import org.zotero.android.pdfjs.data.PdfjsAnnotation
//import org.zotero.android.pdfjs.data.PdfjsDocumentAnnotation
import org.zotero.android.screens.root.RootViewModel
import org.zotero.android.uicomponents.reorder.add
import timber.log.Timber
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class PdfjsFragment @Inject constructor(
    private val path : String,
    private val onDocumentLoadedCallback: (PdfjsDocument) -> Unit
) : Fragment(R.layout.pdfjs_fragment)
{
    var pageIndex: Int = 0
    private lateinit var rootView: View
    private lateinit var webView: WebView
    private var isPdfLoaded: Boolean = true

    private var pageNumber: Int = 0
    private var pageSizeMap: ArrayList<RectF> = ArrayList()
    private var pageLabelsMap: ArrayList<String> = ArrayList()

    private var waitSemaphore: Mutex = Mutex()

    //val selectedAnnotations: List<PdfjsAnnotation> = emptyList()

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
                  "PDFViewerApplication.eventBus.on('pagerendered', function a(evt) {ZoteroJsInterface.onDocumentLoaded()});" +
                        "d = atob('${encodedData}');" +
                        " prom = PDFViewerApplication.open({data: d});" +
                        " prom.then()")
                {}

            //get info from the document
            //this.webView.evaluateJavascript("prom = PDFViewerApplication.pdfViewer.getPagesOverview(); prom.then(function(a) {ZoteroJsInterface.getPagesOverview(a)}", null)
        }
    }

    private fun askNumPages() : Int
    {
        if(this.isPdfLoaded)
        {
            this.webView.post {
                this.webView.evaluateJavascript("ZoteroJsInterface.setPageNumber(PDFViewerApplication.pdfDocument.numPages)") {}
            }
            return this.pageNumber
        }
        return 0
    }

    override fun onStart() {
        super.onStart()

        this.webView = rootView.findViewById<WebView>(R.id.pdfjs_webview)
        this.webView.post {
            this.webView.settings.javaScriptEnabled = true
            this.webView.settings.domStorageEnabled = true
            WebView.setWebContentsDebuggingEnabled(true)
        }

        //load from storage
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", AssetsPathHandler(requireContext()))
            .addPathHandler("/res/", ResourcesPathHandler(requireContext()))
            .build()

        //install javascript interface
        this.webView.addJavascriptInterface(this, "ZoteroJsInterface")

        this.webView.webViewClient = LocalContentWebViewClient(assetLoader, this::callbackOnPageLoaded)
        this.webView.post {
            this.webView.loadUrl("https://appassets.androidplatform.net/assets/web/viewer.html?file=")
        }

        this.isPdfLoaded = false
    }

    @JavascriptInterface
    fun onDocumentLoaded()
    {
        this.isPdfLoaded = true
        //num pages
        this.webView.post {
            this.webView.evaluateJavascript("ZoteroJsInterface.setPageNumber(PDFViewerApplication.pdfDocument.numPages)") {} //NumberPages
            this.webView.evaluateJavascript("ZoteroJsInterface.setPageSizeMap(JSON.stringify(PDFViewerApplication.pdfViewer.getPagesOverview()))") {} //PageSizes
            this.webView.evaluateJavascript("PDFViewerApplication.pdfDocument.getPageLabels().then(function (a) {ZoteroJsInterface.setPageLabels(JSON.stringify(a))})") {} //PageLabels
        }

    }

    @JavascriptInterface
    fun setPageSizeMap(jsonArray: String)
    {
        val pageList = Json.decodeFromString<Array<PageOverviewItem>>(jsonArray)
        for (item in pageList)
        {
            this.pageSizeMap.add(RectF(0F, 0F, item.width.toFloat(), item.height.toFloat()))
        }
    }

    @JavascriptInterface
    fun setPageNumber(pageNumber: String)
    {
        this.pageNumber = pageNumber.toInt()
    }

    @JavascriptInterface
    fun setPageLabels(jsonArray: String)
    {
        this.pageLabelsMap = Json.decodeFromString(jsonArray)
    }

    /*fun setSelectedAnnotation(pdfAnnotation: PdfjsAnnotation) {
        //TODO: Unimplemented
    }*/

    fun getZoomScale(pageIndex: Int): Float {
        //TODO: Unimplemented
        return 1.0f
    }

    fun scrollTo(boundingBox: Any, pageIndex: Int, duration: Int, scrollWhenVisible: Boolean) {
        //TODO: Unimplemented
    }

    fun clearSelectedAnnotations() {
        TODO("Not yet implemented")
    }

    fun setPageIndex(pageIndex: Int, animated: Boolean) {
        TODO("Not yet implemented")
    }
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