package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoResource
import com.example.ui.theme.BraveDarkBackground
import com.example.ui.theme.BraveDarkBorder
import com.example.ui.theme.BraveDarkCard
import com.example.ui.theme.BraveDarkSurface
import com.example.ui.theme.BraveDarkSurfaceVariant
import com.example.ui.theme.BraveOrange
import com.example.ui.theme.BraveOrangeLight
import com.example.ui.theme.BraveShieldGreen
import com.example.ui.theme.BraveTextPrimary
import com.example.ui.theme.BraveTextSecondary
import com.example.viewmodel.BrowserViewModel
import com.example.viewmodel.CurrentScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdmDownloadModal(
    detectedVideos: List<VideoResource>,
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedVideo by remember(detectedVideos) {
        mutableStateOf(detectedVideos.firstOrNull())
    }

    var customTitle by remember(selectedVideo) {
        mutableStateOf(selectedVideo?.title ?: "Web_Video")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BraveDarkSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BraveOrange.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = BraveOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "IDM Video Downloader",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BraveTextPrimary
                        )
                        Text(
                            text = "${detectedVideos.size} stream(s) detected",
                            fontSize = 12.sp,
                            color = BraveShieldGreen
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("idm_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BraveTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // File Name Editor
            Text(
                text = "Save As:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BraveTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        tint = BraveOrange,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = BraveDarkCard,
                    unfocusedContainerColor = BraveDarkCard,
                    focusedBorderColor = BraveOrange,
                    unfocusedBorderColor = BraveDarkBorder,
                    focusedTextColor = BraveTextPrimary,
                    unfocusedTextColor = BraveTextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("idm_file_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Video streams list
            Text(
                text = "Detected Streams & Qualities:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BraveTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(detectedVideos) { video ->
                    val isSelected = video.id == selectedVideo?.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BraveDarkSurfaceVariant else BraveDarkCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) BraveOrange else BraveDarkBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedVideo = video
                                customTitle = video.title
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = if (isSelected) BraveOrange else BraveTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BraveTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = BraveOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = video.qualityLabel,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BraveOrangeLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${video.sizeEstimateBytes / (1024 * 1024)} MB (est.)",
                                        fontSize = 11.sp,
                                        color = BraveTextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = BraveOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = {
                    val videoToDownload = selectedVideo ?: detectedVideos.firstOrNull()
                    if (videoToDownload != null) {
                        viewModel.downloadEngine.startDownload(videoToDownload, customTitle)
                        onDismiss()
                        viewModel.setScreen(CurrentScreen.DOWNLOADS)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BraveOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("idm_start_download_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download with IDM",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
