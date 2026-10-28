package com.example.data

import com.example.data.dao.BrowserDao
import com.example.data.entities.BookmarkEntity
import com.example.data.entities.DownloadEntity
import com.example.data.entities.HistoryEntity
import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {

    val allBookmarks: Flow<List<BookmarkEntity>> = browserDao.getAllBookmarks()
    val recentHistory: Flow<List<HistoryEntity>> = browserDao.getRecentHistory()
    val allDownloads: Flow<List<DownloadEntity>> = browserDao.getAllDownloads()

    suspend fun addBookmark(title: String, url: String, faviconUrl: String? = null) {
        browserDao.insertBookmark(
            BookmarkEntity(
                title = title.ifBlank { url },
                url = url,
                faviconUrl = faviconUrl
            )
        )
    }

    suspend fun removeBookmark(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return browserDao.isBookmarked(url)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            browserDao.insertHistory(
                HistoryEntity(
                    title = title.ifBlank { url },
                    url = url
                )
            )
        }
    }

    suspend fun deleteHistory(id: Long) {
        browserDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        browserDao.clearHistory()
    }

    suspend fun saveDownload(download: DownloadEntity) {
        browserDao.insertOrUpdateDownload(download)
    }

    suspend fun deleteDownload(id: String) {
        browserDao.deleteDownloadById(id)
    }
}
