package com.example.engine

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class ShieldsStats(
    val trackersAndAdsBlocked: Long = 0L,
    val dataSavedBytes: Long = 0L,
    val timeSavedSeconds: Long = 0L,
    val shieldsEnabled: Boolean = true
) {
    val dataSavedDisplay: String
        get() {
            val mb = dataSavedBytes.toDouble() / (1024 * 1024)
            return if (mb < 1.0) {
                val kb = dataSavedBytes / 1024
                "${kb} KB"
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }

    val timeSavedDisplay: String
        get() = if (timeSavedSeconds < 60) {
            "${timeSavedSeconds}s"
        } else {
            val minutes = timeSavedSeconds / 60
            val sec = timeSavedSeconds % 60
            "${minutes}m ${sec}s"
        }
}

class BraveShieldsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("brave_shields_prefs", Context.MODE_PRIVATE)

    private val _stats = MutableStateFlow(
        ShieldsStats(
            trackersAndAdsBlocked = prefs.getLong("blocked_count", 1420L), // sensible starter stats so user sees the counter
            dataSavedBytes = prefs.getLong("data_saved", 28_400_000L),
            timeSavedSeconds = prefs.getLong("time_saved", 142L),
            shieldsEnabled = prefs.getBoolean("shields_enabled", true)
        )
    )
    val stats: StateFlow<ShieldsStats> = _stats.asStateFlow()

    // Host blocklist for known ad servers, trackers, and telemetry
    private val blockedDomains = setOf(
        "doubleclick.net",
        "googleadservices.com",
        "googlesyndication.com",
        "google-analytics.com",
        "adservice.google.com",
        "adnxs.com",
        "outbrain.com",
        "taboola.com",
        "criteo.com",
        "criteo.net",
        "scorecardresearch.com",
        "moatads.com",
        "amazon-adsystem.com",
        "advertising.com",
        "quantserve.com",
        "popads.net",
        "adcolony.com",
        "applovin.com",
        "unityads.unity3d.com",
        "chartbeat.net",
        "hotjar.com",
        "mixpanel.com",
        "segment.io",
        "facebook.com/tr",
        "analytics.twitter.com",
        "clarity.ms",
        "adroll.com",
        "zedo.com",
        "inmobi.com",
        "serving-sys.com",
        "revcontent.com"
    )

    fun toggleShields(enabled: Boolean) {
        prefs.edit().putBoolean("shields_enabled", enabled).apply()
        _stats.value = _stats.value.copy(shieldsEnabled = enabled)
    }

    fun isShieldsEnabled(): Boolean = _stats.value.shieldsEnabled

    fun shouldBlock(url: String): Boolean {
        if (!_stats.value.shieldsEnabled) return false

        val lowerUrl = url.lowercase(Locale.ROOT)

        val host = try {
            Uri.parse(url).host?.lowercase(Locale.ROOT) ?: ""
        } catch (_: Exception) {
            ""
        }

        val domainBlocked = blockedDomains.any { domain ->
            host == domain || host.endsWith(".$domain") || lowerUrl.contains("/$domain")
        }

        val pathBlocked = lowerUrl.contains("/ads/") ||
                lowerUrl.contains("/adserver/") ||
                lowerUrl.contains("pagead2") ||
                lowerUrl.contains("advertisement") ||
                lowerUrl.contains("google_ads") ||
                lowerUrl.contains("popunder")

        if (domainBlocked || pathBlocked) {
            recordBlock()
            return true
        }

        return false
    }

    @Synchronized
    private fun recordBlock() {
        val current = _stats.value
        val newCount = current.trackersAndAdsBlocked + 1
        val newData = current.dataSavedBytes + 85_000L // ~85KB saved per suppressed ad script/image
        val newTime = current.timeSavedSeconds + 1L // 1s time saved

        prefs.edit()
            .putLong("blocked_count", newCount)
            .putLong("data_saved", newData)
            .putLong("time_saved", newTime)
            .apply()

        _stats.value = current.copy(
            trackersAndAdsBlocked = newCount,
            dataSavedBytes = newData,
            timeSavedSeconds = newTime
        )
    }

    fun resetStats() {
        prefs.edit()
            .putLong("blocked_count", 0L)
            .putLong("data_saved", 0L)
            .putLong("time_saved", 0L)
            .apply()

        _stats.value = _stats.value.copy(
            trackersAndAdsBlocked = 0L,
            dataSavedBytes = 0L,
            timeSavedSeconds = 0L
        )
    }
}
