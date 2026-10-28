package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.BraveWebChromeClient
import com.example.engine.BraveWebViewClient
import com.example.engine.VideoSnifferBridge
import com.example.ui.components.IdmDownloadBadge
import com.example.ui.theme.BraveDarkBackground
import com.example.ui.theme.BraveDarkBorder
import com.example.ui.theme.BraveDarkCard
import com.example.ui.theme.BraveDarkSurface
import com.example.ui.theme.BraveDarkSurfaceVariant
import com.example.ui.theme.BraveOrange
import com.example.ui.theme.BraveShieldGreen
import com.example.ui.theme.BraveTextPrimary
import com.example.ui.theme.BraveTextSecondary
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val detectedVideos by viewModel.detectedVideos.collectAsState()
    val stats by viewModel.shieldsStats.collectAsState()

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var urlBarText by remember(activeTab?.url) {
        mutableStateOf(if (activeTab?.url?.startsWith("brave://") == true) "" else activeTab?.url ?: "")
    }
    var isEditingUrl by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Intercept back key to navigate web history
    BackHandler(enabled = activeTab?.url?.startsWith("brave://") != true) {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            viewModel.updateCurrentTabUrl("brave://home")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BraveDarkBackground)
    ) {
        // 1. Top URL Bar
        Surface(
            color = BraveDarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brave Shields Button with Blocked Count Badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { viewModel.setShowShieldsSheet(true) }
                        .padding(6.dp)
                        .testTag("brave_shields_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Brave Shields",
                        tint = if (stats.shieldsEnabled) BraveOrange else BraveTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Omnibox Address Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BraveDarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (isEditingUrl) BraveOrange else BraveDarkBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!isEditingUrl && activeTab?.url?.startsWith("https://") == true) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure HTTPS",
                                tint = BraveShieldGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        BasicTextField(
                            value = urlBarText,
                            onValueChange = { urlBarText = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = BraveTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(BraveOrange),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    isEditingUrl = false
                                    viewModel.openUrl(urlBarText)
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (urlBarText.isEmpty()) {
                                    Text(
                                        text = "Search or type URL",
                                        color = BraveTextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("browser_address_bar")
                        )

                        if (urlBarText.isNotEmpty()) {
                            IconButton(
                                onClick = { urlBarText = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = BraveTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Reload / Stop button
                if (activeTab?.isLoading == true) {
                    IconButton(
                        onClick = { webViewRef?.stopLoading() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop loading",
                            tint = BraveTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { webViewRef?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            tint = BraveTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Tabs Counter Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, BraveOrange, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setScreen(CurrentScreen.TAB_SWITCHER) }
                        .testTag("browser_tabs_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${tabs.size}",
                        color = BraveOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // More Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = BraveTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(BraveDarkCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Tab", color = BraveTextPrimary) },
                            onClick = {
                                showMenu = false
                                viewModel.createNewTab("brave://home")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("IDM Downloads", color = BraveOrange) },
                            onClick = {
                                showMenu = false
                                viewModel.setScreen(CurrentScreen.DOWNLOADS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bookmark Page", color = BraveTextPrimary) },
                            onClick = {
                                showMenu = false
                                viewModel.addBookmarkCurrentPage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History & Bookmarks", color = BraveTextPrimary) },
                            onClick = {
                                showMenu = false
                                viewModel.setScreen(CurrentScreen.HISTORY_BOOKMARKS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Brave Shields", color = BraveShieldGreen) },
                            onClick = {
                                showMenu = false
                                viewModel.setShowShieldsSheet(true)
                            }
                        )
                    }
                }
            }
        }

        // Slim Progress Bar
        AnimatedVisibility(visible = activeTab?.isLoading == true) {
            val progressVal = (activeTab?.progress ?: 0) / 100f
            LinearProgressIndicator(
                progress = { progressVal },
                color = BraveOrange,
                trackColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
            )
        }

        // 2. Main Web View or Home Start Page
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val currentUrl = activeTab?.url ?: "brave://home"

            if (currentUrl == "brave://home") {
                HomeScreen(viewModel = viewModel)
            } else {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                mediaPlaybackRequiresUserGesture = false
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                userAgentString = userAgentString.replace("; wv", "") // Desktop/clean mobile UA
                            }

                            addJavascriptInterface(
                                viewModel.snifferBridge,
                                VideoSnifferBridge.JS_INTERFACE_NAME
                            )

                            webViewClient = BraveWebViewClient(
                                shieldsManager = viewModel.shieldsManager,
                                snifferBridge = viewModel.snifferBridge,
                                onPageTitleChanged = { title ->
                                    viewModel.updateCurrentTabTitle(title)
                                },
                                onPageUrlChanged = { url ->
                                    viewModel.updateCurrentTabUrl(url)
                                    viewModel.updateTabNavigationState(canGoBack(), canGoForward())
                                },
                                onLoadingChanged = { loading ->
                                    viewModel.updateTabLoading(loading)
                                    viewModel.updateTabNavigationState(canGoBack(), canGoForward())
                                }
                            )

                            webChromeClient = BraveWebChromeClient(
                                onProgressChanged = { progress ->
                                    viewModel.updateTabLoading(progress < 100, progress)
                                },
                                onTitleReceived = { title ->
                                    viewModel.updateCurrentTabTitle(title)
                                },
                                onFullscreenToggle = { _, _ -> }
                            )

                            loadUrl(currentUrl)
                            webViewRef = this
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                        if (webView.url != currentUrl && !currentUrl.startsWith("brave://")) {
                            webView.loadUrl(currentUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 3. IDM Floating Video Sniffer Download Button
            if (currentUrl != "brave://home" && detectedVideos.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    IdmDownloadBadge(
                        detectedVideos = detectedVideos,
                        onClick = {
                            viewModel.setShowIdmDialog(true)
                        }
                    )
                }
            }
        }

        // 4. Bottom Browser Toolbar (Brave Style)
        Surface(
            color = BraveDarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { webViewRef?.goBack() },
                    enabled = webViewRef?.canGoBack() == true,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (webViewRef?.canGoBack() == true) BraveTextPrimary else BraveTextSecondary.copy(alpha = 0.4f)
                    )
                }

                IconButton(
                    onClick = { webViewRef?.goForward() },
                    enabled = webViewRef?.canGoForward() == true,
                    modifier = Modifier.testTag("nav_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (webViewRef?.canGoForward() == true) BraveTextPrimary else BraveTextSecondary.copy(alpha = 0.4f)
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.updateCurrentTabUrl("brave://home")
                    },
                    modifier = Modifier.testTag("nav_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (activeTab?.url == "brave://home") BraveOrange else BraveTextPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.setScreen(CurrentScreen.DOWNLOADS) },
                    modifier = Modifier.testTag("nav_downloads_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Downloads",
                        tint = BraveOrange
                    )
                }

                IconButton(
                    onClick = { viewModel.setScreen(CurrentScreen.HISTORY_BOOKMARKS) },
                    modifier = Modifier.testTag("nav_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Bookmarks",
                        tint = BraveTextPrimary
                    )
                }
            }
        }
    }
}
