package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BraveShieldsSheet(
    viewModel: BrowserViewModel,
    onDismiss: () -> Unit
) {
    val stats by viewModel.shieldsStats.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(BraveOrange, BraveDarkSurfaceVariant)
                                )
                            )
                            .border(1.5.dp, BraveOrange, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Brave Shields",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Brave Shields",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BraveTextPrimary
                        )
                        Text(
                            text = if (stats.shieldsEnabled) "Protection is Active" else "Shields are Paused",
                            fontSize = 12.sp,
                            color = if (stats.shieldsEnabled) BraveShieldGreen else Color(0xFFEF4444)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("shields_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BraveTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Master Shields Switch Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BraveDarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Shields UP for this Browser",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BraveTextPrimary
                        )
                        Text(
                            text = "Blocks cross-site trackers, malware, and intrusive ads",
                            fontSize = 11.sp,
                            color = BraveTextSecondary
                        )
                    }

                    Switch(
                        checked = stats.shieldsEnabled,
                        onCheckedChange = { viewModel.toggleShields(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BraveOrange,
                            uncheckedThumbColor = BraveTextSecondary,
                            uncheckedTrackColor = BraveDarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("shields_toggle_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detailed Protection Indicators
            Card(
                colors = CardDefaults.cardColors(containerColor = BraveDarkCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProtectionFeatureRow(
                        icon = Icons.Default.Security,
                        title = "Trackers & Ads Blocked",
                        value = stats.trackersAndAdsBlocked.toString(),
                        accent = BraveOrangeLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProtectionFeatureRow(
                        icon = Icons.Default.Lock,
                        title = "HTTPS Everywhere",
                        value = "Upgraded",
                        accent = BraveShieldGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProtectionFeatureRow(
                        icon = Icons.Default.Fingerprint,
                        title = "Fingerprint Protection",
                        value = "Blocked",
                        accent = BraveShieldPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset Statistics Option
            OutlinedButton(
                onClick = { viewModel.resetShieldsStats() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BraveTextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BraveDarkBorder),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shields_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset Shields Statistics",
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ProtectionFeatureRow(
    icon: ImageVector,
    title: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = BraveTextPrimary
            )
        }

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = accent
        )
    }
}
