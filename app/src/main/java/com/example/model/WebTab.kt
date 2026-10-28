package com.example.model

import java.util.UUID

data class WebTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Brave Start",
    val url: String = "brave://home",
    val isIncognito: Boolean = false,
    val faviconUrl: String? = null,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val blockedAdsCount: Int = 0
)
