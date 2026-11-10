package com.example.orca.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.R
import com.example.orca.data.model.PairingState
import com.example.orca.data.model.Worktree
import com.example.orca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcaTopBar(
    selectedWorktree: Worktree?,
    pairing: PairingState,
    onSwitchWorktreeClick: () -> Unit,
    onOpenPairingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier
            .background(OrcaBackground)
            .border(1.dp, OrcaBorder)
            .testTag("orca_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OrcaBackground,
            titleContentColor = OrcaText
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Orca Emblem Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OrcaSurface)
                        .border(1.dp, OrcaBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_orca_logo),
                        contentDescription = "Orca",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "orca",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = OrcaText,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(OrcaSurfaceElevated)
                                .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "mobile",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrcaAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (selectedWorktree != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSwitchWorktreeClick() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.AltRoute,
                                contentDescription = null,
                                tint = OrcaMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = selectedWorktree.branchName,
                                fontSize = 11.sp,
                                color = OrcaMuted,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        actions = {
            // Live connection latency indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(OrcaSurface)
                    .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenPairingClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (pairing.isPaired) OrcaSuccess else OrcaDanger)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (pairing.isPaired) "${pairing.latencyMs}ms" else "offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = OrcaText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            IconButton(
                onClick = onOpenPairingClick,
                modifier = Modifier.testTag("btn_top_qr_scanner")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Pairing QR",
                    tint = OrcaText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}
