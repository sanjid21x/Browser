package com.example.model

import java.util.UUID

data class VideoResource(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val mimeType: String = "video/mp4",
    val qualityLabel: String = "HD Video",
    val sizeEstimateBytes: Long = 0L,
    val pageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)
