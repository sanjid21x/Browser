package com.example.engine

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream
import java.util.Locale

class BraveWebViewClient(
    private val shieldsManager: BraveShieldsManager,
    private val snifferBridge: VideoSnifferBridge,
    private val onPageTitleChanged: (String) -> Unit,
    private val onPageUrlChanged: (String) -> Unit,
    private val onLoadingChanged: (Boolean) -> Unit
) : WebViewClient() {

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null

        // 1. Brave Shields Check
        if (shieldsManager.shouldBlock(url)) {
            // Return empty 200 response to cleanly drop ad/tracker request
            return WebResourceResponse(
                "text/plain",
                "UTF-8",
                ByteArrayInputStream(ByteArray(0))
            )
        }

        // 2. IDM Video Sniffing over network requests
        val lowerUrl = url.lowercase(Locale.ROOT)
        if (lowerUrl.contains(".mp4") ||
            lowerUrl.contains(".webm") ||
            lowerUrl.contains(".m3u8") ||
            lowerUrl.contains(".mkv") ||
            lowerUrl.contains("/video/") ||
            lowerUrl.contains("googlevideo.com/videoplayback")
        ) {
            val title = view?.title ?: ""
            val mime = when {
                lowerUrl.contains(".m3u8") -> "application/x-mpegURL"
                lowerUrl.contains(".webm") -> "video/webm"
                else -> "video/mp4"
            }
            snifferBridge.addFromNetwork(url, mime, title)
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onLoadingChanged(true)
        url?.let { onPageUrlChanged(it) }
        snifferBridge.clearVideos()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onLoadingChanged(false)
        url?.let { onPageUrlChanged(it) }
        view?.title?.let { onPageTitleChanged(it) }

        // Inject IDM Video Sniffer Script into loaded web page
        view?.evaluateJavascript(VideoSnifferBridge.SNIFFER_JS_CODE, null)

        // Inject cosmetic filter CSS if Shields enabled
        if (shieldsManager.isShieldsEnabled()) {
            val adHideCss = """
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = '.adsbygoogle, [id^="google_ads"], [class*="advertisement"], [class*="ad-banner"], [class*="sponsor"] { display: none !important; height: 0 !important; }';
                    document.head.appendChild(style);
                })();
            """.trimIndent()
            view?.evaluateJavascript(adHideCss, null)
        }
    }
}

class BraveWebChromeClient(
    private val onProgressChanged: (Int) -> Unit,
    private val onTitleReceived: (String) -> Unit,
    private val onFullscreenToggle: (View?, WebChromeClient.CustomViewCallback?) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        title?.let { onTitleReceived(it) }
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        super.onShowCustomView(view, callback)
        onFullscreenToggle(view, callback)
    }

    override fun onHideCustomView() {
        super.onHideCustomView()
        onFullscreenToggle(null, null)
    }
}
