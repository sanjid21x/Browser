package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.BraveShieldsManager
import com.example.engine.VideoSnifferBridge
import com.example.model.DownloadStatus
import com.example.model.DownloadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Brave Downloader", appName)
    }

    @Test
    fun `brave shields blocks tracker and ad urls`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shields = BraveShieldsManager(context)

        assertTrue("Shields should be active by default", shields.isShieldsEnabled())

        // Test ad network blocking
        assertTrue(shields.shouldBlock("https://ad.doubleclick.net/pagead/ads.js"))
        assertTrue(shields.shouldBlock("https://www.google-analytics.com/analytics.js"))
        assertTrue(shields.shouldBlock("https://secure.adnxs.com/seg?add=1"))

        // Test non-ad legit url allowed
        assertFalse(shields.shouldBlock("https://en.wikipedia.org/wiki/Main_Page"))
        assertFalse(shields.shouldBlock("https://www.vimeo.com"))
    }

    @Test
    fun `idm video sniffer captures stream and resolves qualities`() {
        var capturedCount = 0
        val sniffer = VideoSnifferBridge { capturedCount++ }

        sniffer.addFromNetwork(
            url = "https://example.com/media/sample_1080p_stream.mp4",
            mimeType = "video/mp4",
            pageTitle = "Trailer HD"
        )

        val videos = sniffer.detectedVideos.value
        assertEquals(1, videos.size)
        assertEquals("1080p FHD", videos.first().qualityLabel)
        assertEquals("video/mp4", videos.first().mimeType)
    }

    @Test
    fun `download task calculates progress percentage correctly`() {
        val task = DownloadTask(
            id = "test-1",
            title = "Sample Video",
            url = "https://example.com/video.mp4",
            mimeType = "video/mp4",
            filePath = "/tmp/video.mp4",
            totalBytes = 100_000_000L,
            downloadedBytes = 45_000_000L,
            status = DownloadStatus.DOWNLOADING
        )

        assertEquals(45, task.progressPercentage)
    }
}
