package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.engine.BraveShieldsManager
import com.example.engine.ShieldsStats
import com.example.engine.VideoDownloadEngine
import com.example.engine.VideoSnifferBridge
import com.example.model.VideoResource
import com.example.model.WebTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class CurrentScreen {
    BROWSER,
    TAB_SWITCHER,
    DOWNLOADS,
    HISTORY_BOOKMARKS
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    val repository: BrowserRepository = BrowserRepository(
        BrowserDatabase.getInstance(application).browserDao()
    )

    val shieldsManager = BraveShieldsManager(application)
    val shieldsStats: StateFlow<ShieldsStats> = shieldsManager.stats

    val downloadEngine = VideoDownloadEngine(application, repository, viewModelScope)

    val snifferBridge = VideoSnifferBridge { detected ->
        // Notify or update active tab
    }
    val detectedVideos: StateFlow<List<VideoResource>> = snifferBridge.detectedVideos

    private val _tabs = MutableStateFlow<List<WebTab>>(
        listOf(
            WebTab(
                id = UUID.randomUUID().toString(),
                title = "Brave Start",
                url = "brave://home"
            )
        )
    )
    val tabs: StateFlow<List<WebTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>(_tabs.value.first().id)
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    val activeTab: StateFlow<WebTab?> = combine(_tabs, _activeTabId) { tabs, activeId ->
        tabs.find { it.id == activeId } ?: tabs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, _tabs.value.firstOrNull())

    private val _currentScreen = MutableStateFlow(CurrentScreen.BROWSER)
    val currentScreen: StateFlow<CurrentScreen> = _currentScreen.asStateFlow()

    private val _searchEngine = MutableStateFlow("Brave Search")
    val searchEngine: StateFlow<String> = _searchEngine.asStateFlow()

    // Shields sheet visibility
    private val _showShieldsSheet = MutableStateFlow(false)
    val showShieldsSheet: StateFlow<Boolean> = _showShieldsSheet.asStateFlow()

    // IDM video download bottom sheet
    private val _showIdmDialog = MutableStateFlow(false)
    val showIdmDialog: StateFlow<Boolean> = _showIdmDialog.asStateFlow()

    // Offline video player preview
    private val _previewVideoPath = MutableStateFlow<String?>(null)
    val previewVideoPath: StateFlow<String?> = _previewVideoPath.asStateFlow()

    fun setScreen(screen: CurrentScreen) {
        _currentScreen.value = screen
    }

    fun setShowShieldsSheet(show: Boolean) {
        _showShieldsSheet.value = show
    }

    fun setShowIdmDialog(show: Boolean) {
        _showIdmDialog.value = show
    }

    fun setPreviewVideoPath(path: String?) {
        _previewVideoPath.value = path
    }

    fun setSearchEngine(name: String) {
        _searchEngine.value = name
    }

    fun openUrl(rawQueryOrUrl: String) {
        val trimmed = rawQueryOrUrl.trim()
        if (trimmed.isBlank()) return

        val resolvedUrl = when {
            trimmed.startsWith("brave://") -> trimmed
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> {
                when (_searchEngine.value) {
                    "Google" -> "https://www.google.com/search?q=" + android.net.Uri.encode(trimmed)
                    "DuckDuckGo" -> "https://duckduckgo.com/?q=" + android.net.Uri.encode(trimmed)
                    else -> "https://search.brave.com/search?q=" + android.net.Uri.encode(trimmed)
                }
            }
        }

        updateCurrentTabUrl(resolvedUrl)
        _currentScreen.value = CurrentScreen.BROWSER
    }

    fun updateCurrentTabUrl(url: String) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(url = url)
            } else {
                tab
            }
        }
    }

    fun updateCurrentTabTitle(title: String) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(title = title.ifBlank { "Web Page" })
            } else {
                tab
            }
        }
        val tab = activeTab.value
        if (tab != null && !tab.isIncognito && !tab.url.startsWith("brave://")) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addHistory(title, tab.url)
            }
        }
    }

    fun updateTabLoading(isLoading: Boolean, progress: Int = 0) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(isLoading = isLoading, progress = progress)
            } else {
                tab
            }
        }
    }

    fun updateTabNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        val currentId = _activeTabId.value
        _tabs.value = _tabs.value.map { tab ->
            if (tab.id == currentId) {
                tab.copy(canGoBack = canGoBack, canGoForward = canGoForward)
            } else {
                tab
            }
        }
    }

    fun createNewTab(url: String = "brave://home", isIncognito: Boolean = false) {
        val newTab = WebTab(
            id = UUID.randomUUID().toString(),
            title = if (isIncognito) "Private Tab" else "Brave Start",
            url = url,
            isIncognito = isIncognito
        )
        _tabs.value = _tabs.value + newTab
        _activeTabId.value = newTab.id
        _currentScreen.value = CurrentScreen.BROWSER
        snifferBridge.clearVideos()
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        _currentScreen.value = CurrentScreen.BROWSER
        snifferBridge.clearVideos()
    }

    fun closeTab(tabId: String) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            // Keep at least one tab open
            val replacement = WebTab(
                id = UUID.randomUUID().toString(),
                title = "Brave Start",
                url = "brave://home"
            )
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
            return
        }

        val remaining = currentList.filter { it.id != tabId }
        _tabs.value = remaining

        if (_activeTabId.value == tabId) {
            _activeTabId.value = remaining.last().id
        }
    }

    fun toggleShields(enabled: Boolean) {
        shieldsManager.toggleShields(enabled)
    }

    fun resetShieldsStats() {
        shieldsManager.resetStats()
    }

    fun addBookmarkCurrentPage() {
        val tab = activeTab.value ?: return
        if (tab.url.startsWith("brave://")) return

        viewModelScope.launch(Dispatchers.IO) {
            repository.addBookmark(tab.title, tab.url)
        }
    }
}
