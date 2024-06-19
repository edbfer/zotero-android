package org.zotero.android.pdfjs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import org.zotero.android.R
import org.zotero.android.screens.root.RootViewModel

class PdfjsFragment : Fragment(R.layout.pdfjs_fragment)
{
    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.rootView = inflater.inflate(R.layout.pdfjs_fragment, container, false)
        return this.rootView
    }

    override fun onStart() {
        super.onStart()

        val webview = rootView.findViewById<WebView>(R.id.pdfjs_webview)
        webview.webViewClient = WebViewClient()
        webview.loadUrl("https://github.com/edbfer/zotero-android")
    }
}