package com.example.engine

import android.webkit.JavascriptInterface
import com.example.model.VideoResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VideoSnifferBridge(
    private val onVideoDetected: (VideoResource) -> Unit
) {

    private val _detectedVideos = MutableStateFlow<List<VideoResource>>(emptyList())
    val detectedVideos: StateFlow<List<VideoResource>> = _detectedVideos.asStateFlow()

    fun clearVideos() {
        _detectedVideos.value = emptyList()
    }

    @JavascriptInterface
    fun onMediaFound(rawUrl: String?, title: String?, mimeType: String?, durationEstimate: String?) {
        if (rawUrl.isNullOrBlank()) return
        val cleanUrl = rawUrl.trim()
        if (cleanUrl.startsWith("blob:") || cleanUrl.startsWith("data:")) {
            // Blob streams can't be fetched directly without media stream recorder
            return
        }

        val cleanTitle = if (!title.isNullOrBlank() && title != "undefined") {
            title.trim()
        } else {
            extractFilenameFromUrl(cleanUrl)
        }

        val resolvedMime = if (!mimeType.isNullOrBlank() && mimeType != "undefined") {
            mimeType
        } else {
            inferMimeType(cleanUrl)
        }

        val quality = inferQualityFromUrl(cleanUrl)

        val video = VideoResource(
            url = cleanUrl,
            title = cleanTitle,
            mimeType = resolvedMime,
            qualityLabel = quality,
            sizeEstimateBytes = estimateSize(cleanUrl),
            pageUrl = ""
        )

        addVideoIfNew(video)
    }

    fun addFromNetwork(url: String, mimeType: String, pageTitle: String) {
        if (url.isBlank()) return
        val lower = url.lowercase(Locale.ROOT)
        // Skip tiny ads, tracker pings, or manifest chunks if not main stream
        if (lower.contains("ad") && lower.contains("pixel")) return

        val quality = inferQualityFromUrl(url)
        val title = if (pageTitle.isNotBlank()) pageTitle else extractFilenameFromUrl(url)

        val video = VideoResource(
            url = url,
            title = title,
            mimeType = mimeType,
            qualityLabel = quality,
            sizeEstimateBytes = estimateSize(url),
            pageUrl = ""
        )

        addVideoIfNew(video)
    }

    private fun addVideoIfNew(video: VideoResource) {
        val current = _detectedVideos.value
        // Check for duplicates
        if (current.any { it.url == video.url }) return

        val updated = current + video
        _detectedVideos.value = updated
        onVideoDetected(video)
    }

    private fun extractFilenameFromUrl(url: String): String {
        return try {
            val path = android.net.Uri.parse(url).path ?: ""
            val name = path.substringAfterLast("/")
            if (name.contains(".")) name else "Web_Video_${System.currentTimeMillis() % 10000}.mp4"
        } catch (_: Exception) {
            "Web_Video_${System.currentTimeMillis() % 10000}.mp4"
        }
    }

    private fun inferMimeType(url: String): String {
        val lower = url.lowercase(Locale.ROOT)
        return when {
            lower.contains(".mp4") -> "video/mp4"
            lower.contains(".webm") -> "video/webm"
            lower.contains(".m3u8") -> "application/x-mpegURL"
            lower.contains(".mkv") -> "video/x-matroska"
            lower.contains(".mp3") -> "audio/mpeg"
            lower.contains(".aac") -> "audio/aac"
            else -> "video/mp4"
        }
    }

    private fun inferQualityFromUrl(url: String): String {
        val lower = url.lowercase(Locale.ROOT)
        return when {
            lower.contains("1080") -> "1080p FHD"
            lower.contains("720") -> "720p HD"
            lower.contains("480") -> "480p SD"
            lower.contains("360") -> "360p"
            lower.contains("m3u8") -> "HLS Stream"
            else -> "HD Video (MP4)"
        }
    }

    private fun estimateSize(url: String): Long {
        val lower = url.lowercase(Locale.ROOT)
        return when {
            lower.contains("1080") -> 45_000_000L // ~45MB
            lower.contains("720") -> 24_000_000L  // ~24MB
            lower.contains("480") -> 12_000_000L  // ~12MB
            else -> 18_500_000L                   // ~18MB
        }
    }

    companion object {
        const val JS_INTERFACE_NAME = "IdmSniffer"

        val SNIFFER_JS_CODE = """
            (function() {
                if (window.__idmSnifferInjected) return;
                window.__idmSnifferInjected = true;

                function reportVideo(src, title) {
                    if (!src || src.startsWith('blob:') || src.startsWith('data:')) return;
                    try {
                        var pageTitle = title || document.title || 'Web Video';
                        window.IdmSniffer.onMediaFound(src, pageTitle, 'video/mp4', '0');
                    } catch(e) {}
                }

                function scanMedia() {
                    var videos = document.querySelectorAll('video');
                    videos.forEach(function(v) {
                        if (v.src) reportVideo(v.src);
                        if (v.currentSrc) reportVideo(v.currentSrc);
                        var sources = v.querySelectorAll('source');
                        sources.forEach(function(s) {
                            if (s.src) reportVideo(s.src);
                        });

                        v.addEventListener('play', function() {
                            if (v.src) reportVideo(v.src);
                            if (v.currentSrc) reportVideo(v.currentSrc);
                        });
                    });
                }

                scanMedia();
                setInterval(scanMedia, 3000);

                // Listen to dynamically inserted video tags
                var observer = new MutationObserver(function(mutations) {
                    scanMedia();
                });
                observer.observe(document.body || document.documentElement, {
                    childList: true,
                    subtree: true
                });
            })();
        """.trimIndent()
    }
}
