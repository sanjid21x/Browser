package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BraveDarkBackground
import com.example.ui.theme.BraveDarkBorder
import com.example.ui.theme.BraveDarkCard
import com.example.ui.theme.BraveDarkSurface
import com.example.ui.theme.BraveDarkSurfaceVariant
import com.example.ui.theme.BraveOrange
import com.example.ui.theme.BraveOrangeLight
import com.example.ui.theme.BraveShieldGreen
import com.example.ui.theme.BraveShieldPurple
import com.example.ui.theme.BraveTextPrimary
import com.example.ui.theme.BraveTextSecondary
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

data class SpeedDialItem(
    val title: String,
    val url: String,
    val iconVector: ImageVector,
    val accentColor: Color
)

@Composable
fun HomeScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.shieldsStats.collectAsState()
    var searchInput by remember { mutableStateOf("") }

    val speedDials = remember {
        listOf(
            SpeedDialItem("YouTube", "https://m.youtube.com", Icons.Default.VideoLibrary, Color(0xFFFF0000)),
            SpeedDialItem("Vimeo", "https://vimeo.com/watch", Icons.Default.VideoLibrary, Color(0xFF1AB7EA)),
            SpeedDialItem("Reddit", "https://www.reddit.com", Icons.Default.Search, Color(0xFFFF4500)),
            SpeedDialItem("Wikipedia", "https://en.m.wikipedia.org", Icons.Default.Bookmark, Color(0xFF6B7280)),
            SpeedDialItem("Twitch", "https://m.twitch.tv", Icons.Default.VideoLibrary, Color(0xFF9146FF)),
            SpeedDialItem("Archive", "https://archive.org", Icons.Default.History, Color(0xFF3B82F6))
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BraveDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Brave Branding Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(BraveOrange, BraveDarkSurface)
                            )
                        )
                        .border(2.dp, BraveOrange, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Brave Lion Shield",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BRAVE",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = BraveTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = BraveOrange,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "IDM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Fast, Private Browser with Video Downloader",
                    fontSize = 12.sp,
                    color = BraveTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // 2. Brave Shields Live Stats Dashboard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BraveDarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clickable { viewModel.setShowShieldsSheet(true) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = BraveShieldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Brave Shields Protection",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BraveTextPrimary
                            )
                        }

                        Surface(
                            color = if (stats.shieldsEnabled) Color(0x2610B981) else Color(0x26EF4444),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (stats.shieldsEnabled) "SHIELDS UP" else "OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stats.shieldsEnabled) BraveShieldGreen else Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMetric(
                            label = "Trackers Blocked",
                            value = stats.trackersAndAdsBlocked.toString(),
                            accentColor = BraveOrangeLight
                        )
                        StatMetric(
                            label = "Data Saved",
                            value = stats.dataSavedDisplay,
                            accentColor = BraveShieldGreen
                        )
                        StatMetric(
                            label = "Time Saved",
                            value = stats.timeSavedDisplay,
                            accentColor = BraveShieldPurple
                        )
                    }
                }
            }
        }

        // 3. Omnibox Search Bar
        item {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = {
                    Text(
                        text = "Search or type web address",
                        fontSize = 14.sp,
                        color = BraveTextSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = BraveOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (searchInput.isNotBlank()) {
                            viewModel.openUrl(searchInput)
                        }
                    }
                ),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BraveDarkSurface,
                    unfocusedContainerColor = BraveDarkSurface,
                    focusedBorderColor = BraveOrange,
                    unfocusedBorderColor = BraveDarkBorder,
                    focusedTextColor = BraveTextPrimary,
                    unfocusedTextColor = BraveTextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input")
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. Quick Speed Dials Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Favorites & Media",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BraveTextPrimary
                )
                Text(
                    text = "IDM Sniffer Ready",
                    fontSize = 11.sp,
                    color = BraveOrangeLight,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            // Speed dial grid
            val chunked = speedDials.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                SpeedDialCard(
                                    item = item,
                                    onClick = { viewModel.openUrl(item.url) }
                                )
                            }
                        }
                        // Fill extra slot if row has fewer than 3
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. Quick Access Shortcuts (Downloads & History)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShortcutCard(
                    title = "IDM Downloads",
                    subtitle = "Manage & Play",
                    icon = Icons.Default.Download,
                    accentColor = BraveOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setScreen(CurrentScreen.DOWNLOADS) }
                )

                ShortcutCard(
                    title = "History & Bookmarks",
                    subtitle = "Saved Sites",
                    icon = Icons.Default.Bookmark,
                    accentColor = BraveShieldPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setScreen(CurrentScreen.HISTORY_BOOKMARKS) }
                )
            }
        }
    }
}

@Composable
private fun StatMetric(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = BraveTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpeedDialCard(
    item: SpeedDialItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BraveDarkCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BraveDarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = item.iconVector,
                    contentDescription = item.title,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = BraveTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BraveDarkCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BraveTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = BraveTextSecondary
                )
            }
        }
    }
}
