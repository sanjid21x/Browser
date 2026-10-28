package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WebTab
import com.example.ui.theme.BraveDarkBackground
import com.example.ui.theme.BraveDarkBorder
import com.example.ui.theme.BraveDarkCard
import com.example.ui.theme.BraveDarkSurface
import com.example.ui.theme.BraveDarkSurfaceVariant
import com.example.ui.theme.BraveOrange
import com.example.ui.theme.BraveShieldPurple
import com.example.ui.theme.BraveTextPrimary
import com.example.ui.theme.BraveTextSecondary
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.setScreen(CurrentScreen.BROWSER)
    }

    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open Tabs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BraveTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = BraveOrange,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${tabs.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setScreen(CurrentScreen.BROWSER) },
                        modifier = Modifier.testTag("tabs_done_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = BraveOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BraveDarkSurface)
            )
        },
        bottomBar = {
            Surface(
                color = BraveDarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.createNewTab("brave://home", isIncognito = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = BraveDarkCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = BraveShieldPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Private Tab",
                            fontSize = 12.sp,
                            color = BraveTextPrimary
                        )
                    }

                    Button(
                        onClick = { viewModel.createNewTab("brave://home", isIncognito = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = BraveOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("new_tab_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Tab",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = BraveDarkBackground,
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(tabs, key = { it.id }) { tab ->
                val isActive = tab.id == activeTabId
                TabCard(
                    tab = tab,
                    isActive = isActive,
                    onSelect = { viewModel.switchTab(tab.id) },
                    onClose = { viewModel.closeTab(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: WebTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) BraveDarkSurfaceVariant else BraveDarkCard
        ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            if (isActive) 2.dp else 1.dp,
            if (isActive) BraveOrange else BraveDarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Tab Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BraveDarkSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (tab.isIncognito) Icons.Default.VisibilityOff else Icons.Default.Language,
                        contentDescription = null,
                        tint = if (tab.isIncognito) BraveShieldPurple else BraveOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BraveTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close tab",
                        tint = BraveTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Tab Preview Representation
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(BraveDarkBackground)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BraveDarkSurface)
                    ) {
                        Icon(
                            imageVector = if (tab.url.startsWith("brave://")) Icons.Default.Shield else Icons.Default.Language,
                            contentDescription = null,
                            tint = if (tab.url.startsWith("brave://")) BraveOrange else BraveTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tab.url.removePrefix("https://").removePrefix("http://"),
                        fontSize = 10.sp,
                        color = BraveTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
