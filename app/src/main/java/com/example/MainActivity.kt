package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.BraveShieldsSheet
import com.example.ui.components.IdmDownloadModal
import com.example.ui.components.VideoPlayerSheet
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HistoryBookmarksSheet
import com.example.ui.screens.TabSwitcherScreen
import com.example.ui.theme.BraveDarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                BraveApp()
            }
        }
    }
}

@Composable
fun BraveApp(
    viewModel: BrowserViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showShieldsSheet by viewModel.showShieldsSheet.collectAsState()
    val showIdmDialog by viewModel.showIdmDialog.collectAsState()
    val detectedVideos by viewModel.detectedVideos.collectAsState()
    val previewVideoPath by viewModel.previewVideoPath.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(BraveDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = BraveDarkBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    CurrentScreen.BROWSER -> {
                        BrowserScreen(viewModel = viewModel)
                    }
                    CurrentScreen.TAB_SWITCHER -> {
                        TabSwitcherScreen(viewModel = viewModel)
                    }
                    CurrentScreen.DOWNLOADS -> {
                        DownloadsScreen(viewModel = viewModel)
                    }
                    CurrentScreen.HISTORY_BOOKMARKS -> {
                        HistoryBookmarksSheet(viewModel = viewModel)
                    }
                }
            }

            // Brave Shields Sheet
            if (showShieldsSheet) {
                BraveShieldsSheet(
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowShieldsSheet(false) }
                )
            }

            // IDM Video Sniffer Download Modal
            if (showIdmDialog && detectedVideos.isNotEmpty()) {
                IdmDownloadModal(
                    detectedVideos = detectedVideos,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setShowIdmDialog(false) }
                )
            }

            // Offline Video Player Sheet
            previewVideoPath?.let { path ->
                VideoPlayerSheet(
                    videoFilePath = path,
                    onDismiss = { viewModel.setPreviewVideoPath(null) }
                )
            }
        }
    }
}
