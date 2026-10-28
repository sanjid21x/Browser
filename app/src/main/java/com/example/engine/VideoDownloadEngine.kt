package com.example.engine

import android.content.Context
import com.example.data.BrowserRepository
import com.example.data.entities.DownloadEntity
import com.example.model.DownloadStatus
import com.example.model.DownloadTask
import com.example.model.VideoResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class VideoDownloadEngine(
    private val context: Context,
    private val repository: BrowserRepository,
    private val scope: CoroutineScope
) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _tasks = MutableStateFlow<Map<String, DownloadTask>>(emptyMap())
    val tasks: StateFlow<Map<String, DownloadTask>> = _tasks.asStateFlow()

    private val runningJobs = ConcurrentHashMap<String, Job>()

    init {
        // Load existing completed/paused downloads from repository
        scope.launch(Dispatchers.IO) {
            val entities = repository.allDownloads.first()
            val initialMap = entities.associate { entity ->
                entity.id to DownloadTask(
                    id = entity.id,
                    title = entity.title,
                    url = entity.url,
                    mimeType = entity.mimeType,
                    filePath = entity.filePath,
                    totalBytes = entity.totalBytes,
                    downloadedBytes = entity.downloadedBytes,
                    status = try {
                        DownloadStatus.valueOf(entity.status)
                    } catch (_: Exception) {
                        DownloadStatus.COMPLETED
                    },
                    createdAt = entity.createdAt
                )
            }
            _tasks.value = initialMap
        }
    }

    fun startDownload(video: VideoResource, customTitle: String? = null): String {
        val taskId = UUID.randomUUID().toString()
        val title = (customTitle ?: video.title).trim().ifBlank { "Video_${taskId.take(6)}" }
        val extension = when {
            video.mimeType.contains("mp4") -> ".mp4"
            video.mimeType.contains("webm") -> ".webm"
            video.mimeType.contains("m3u8") -> ".m3u8"
            video.mimeType.contains("mp3") -> ".mp3"
            else -> ".mp4"
        }
        val safeFileName = title.replace(Regex("[^a-zA-Z0-9._-]"), "_") + extension
        val targetDir = context.getExternalFilesDir("Videos") ?: context.filesDir
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, safeFileName)

        val task = DownloadTask(
            id = taskId,
            title = title,
            url = video.url,
            mimeType = video.mimeType,
            filePath = targetFile.absolutePath,
            totalBytes = video.sizeEstimateBytes,
            downloadedBytes = 0L,
            status = DownloadStatus.DOWNLOADING
        )

        updateTaskState(task)
        launchDownloadJob(task, targetFile)
        return taskId
    }

    fun resumeDownload(taskId: String) {
        val task = _tasks.value[taskId] ?: return
        if (task.status == DownloadStatus.DOWNLOADING) return

        val targetFile = File(task.filePath)
        val currentBytes = if (targetFile.exists()) targetFile.length() else 0L

        val updated = task.copy(
            downloadedBytes = currentBytes,
            status = DownloadStatus.DOWNLOADING,
            errorMessage = null
        )
        updateTaskState(updated)
        launchDownloadJob(updated, targetFile)
    }

    fun pauseDownload(taskId: String) {
        runningJobs[taskId]?.cancel()
        runningJobs.remove(taskId)

        val task = _tasks.value[taskId] ?: return
        val updated = task.copy(status = DownloadStatus.PAUSED, speedBps = 0L)
        updateTaskState(updated)
        persistTask(updated)
    }

    fun cancelDownload(taskId: String) {
        runningJobs[taskId]?.cancel()
        runningJobs.remove(taskId)

        val task = _tasks.value[taskId]
        if (task != null) {
            val file = File(task.filePath)
            if (file.exists()) file.delete()
        }

        val updatedMap = _tasks.value.toMutableMap()
        updatedMap.remove(taskId)
        _tasks.value = updatedMap

        scope.launch(Dispatchers.IO) {
            repository.deleteDownload(taskId)
        }
    }

    private fun launchDownloadJob(task: DownloadTask, targetFile: File) {
        val job = scope.launch(Dispatchers.IO) {
            var downloaded = targetFile.length()
            val requestBuilder = Request.Builder().url(task.url)
            if (downloaded > 0) {
                requestBuilder.addHeader("Range", "bytes=$downloaded-")
            }

            var lastCalcTime = System.currentTimeMillis()
            var bytesSinceLastCalc = 0L

            try {
                val response = okHttpClient.newCall(requestBuilder.build()).execute()
                if (!response.isSuccessful && response.code != 206) {
                    throw Exception("Server returned HTTP ${response.code}")
                }

                val body = response.body ?: throw Exception("Empty response body")
                val contentLength = body.contentLength()
                val totalBytes = if (contentLength > 0) {
                    if (downloaded > 0) downloaded + contentLength else contentLength
                } else {
                    task.totalBytes
                }

                val inputStream: InputStream = body.byteStream()
                val outputStream = FileOutputStream(targetFile, downloaded > 0)
                val buffer = ByteArray(8192)
                var read: Int

                outputStream.use { out ->
                    inputStream.use { inp ->
                        while (inp.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            downloaded += read
                            bytesSinceLastCalc += read

                            val now = System.currentTimeMillis()
                            val elapsed = now - lastCalcTime
                            if (elapsed >= 500) {
                                val speed = (bytesSinceLastCalc * 1000) / elapsed
                                lastCalcTime = now
                                bytesSinceLastCalc = 0L

                                updateTaskState(
                                    task.copy(
                                        downloadedBytes = downloaded,
                                        totalBytes = totalBytes,
                                        speedBps = speed,
                                        status = DownloadStatus.DOWNLOADING
                                    )
                                )
                            }
                        }
                    }
                }

                // Completed
                val completedTask = task.copy(
                    downloadedBytes = downloaded,
                    totalBytes = downloaded,
                    speedBps = 0L,
                    status = DownloadStatus.COMPLETED
                )
                updateTaskState(completedTask)
                persistTask(completedTask)
            } catch (ce: CancellationException) {
                // Paused or cancelled by user
            } catch (e: Exception) {
                val failedTask = task.copy(
                    status = DownloadStatus.FAILED,
                    speedBps = 0L,
                    errorMessage = e.localizedMessage ?: "Download failed"
                )
                updateTaskState(failedTask)
                persistTask(failedTask)
            } finally {
                runningJobs.remove(task.id)
            }
        }
        runningJobs[task.id] = job
    }

    private fun updateTaskState(task: DownloadTask) {
        val map = _tasks.value.toMutableMap()
        map[task.id] = task
        _tasks.value = map
    }

    private fun persistTask(task: DownloadTask) {
        scope.launch(Dispatchers.IO) {
            repository.saveDownload(
                DownloadEntity(
                    id = task.id,
                    title = task.title,
                    url = task.url,
                    mimeType = task.mimeType,
                    filePath = task.filePath,
                    totalBytes = task.totalBytes,
                    downloadedBytes = task.downloadedBytes,
                    status = task.status.name,
                    createdAt = task.createdAt
                )
            )
        }
    }
}
