package com.example.orca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.data.model.HostEnvironment
import com.example.orca.data.model.ModelUsage
import com.example.orca.data.model.PairingState
import com.example.orca.ui.components.PairingDialog
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.OrcaViewModel

@Composable
fun RemoteUsageScreen(
    viewModel: OrcaViewModel,
    modifier: Modifier = Modifier
) {
    val pairing by viewModel.pairing.collectAsState()
    val usageMetrics by viewModel.usageMetrics.collectAsState()
    val hosts = viewModel.availableHosts

    var autoApproveReadOnly by remember { mutableStateOf(true) }
    var pushNotificationsEnabled by remember { mutableStateOf(true) }
    var showPairDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OrcaBackground)
            .padding(14.dp)
            .testTag("remote_usage_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Remote & Model Limits",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OrcaText
            )
            Text(
                text = "Monitor desktop daemon pairing, remote nodes, and token quotas",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        // Desktop Pairing Card
        item {
            PairingCard(
                pairing = pairing,
                onPairClick = { showPairDialog = true }
            )
        }

        // Connected Remote Hosts
        item {
            Text(
                text = "EXECUTION NODES (${hosts.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        items(hosts) { host ->
            HostNodeCard(host = host)
        }

        // Model Usage & Token Limits
        item {
            Text(
                text = "AGENT TOKEN LIMITS & QUOTAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        items(usageMetrics) { usage ->
            UsageCard(usage = usage)
        }

        // Agent Steering Settings
        item {
            Text(
                text = "ORCHESTRATION PREFERENCES",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = OrcaSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Approve Read Tools",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrcaText
                            )
                            Text(
                                text = "Allow agents to read files and run non-mutating checks without prompt",
                                fontSize = 11.sp,
                                color = OrcaMuted
                            )
                        }
                        Switch(
                            checked = autoApproveReadOnly,
                            onCheckedChange = { autoApproveReadOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OrcaAccent,
                                checkedTrackColor = OrcaAccent.copy(alpha = 0.3f)
                            )
                        )
                    }

                    HorizontalDivider(color = OrcaBorder)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Push Alerts When Agent Finishes",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OrcaText
                            )
                            Text(
                                text = "Get notified when background task finishes or awaits tool confirmation",
                                fontSize = 11.sp,
                                color = OrcaMuted
                            )
                        }
                        Switch(
                            checked = pushNotificationsEnabled,
                            onCheckedChange = { pushNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = OrcaAccent,
                                checkedTrackColor = OrcaAccent.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showPairDialog) {
        PairingDialog(
            pairing = pairing,
            onDismiss = { showPairDialog = false },
            onPairSuccess = {
                showPairDialog = false
            }
        )
    }
}

@Composable
private fun PairingCard(
    pairing: PairingState,
    onPairClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = OrcaSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (pairing.isPaired) OrcaSuccess else OrcaDanger)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DESKTOP PAIRING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaText
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OrcaTerminalBg)
                        .border(1.dp, OrcaSuccess, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${pairing.latencyMs}ms latency",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrcaSuccess,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pairing.hostName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = OrcaText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pairing.relayUrl,
                fontSize = 10.sp,
                color = OrcaMuted,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${pairing.activeTunnels} active tunnels",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )

                Button(
                    onClick = onPairClick,
                    colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_re_pair")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pair Desktop", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun HostNodeCard(host: HostEnvironment) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = OrcaSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = OrcaAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = host.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaText
                    )
                    Text(
                        text = host.endpoint,
                        fontSize = 10.sp,
                        color = OrcaMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "CPU: ${host.cpuPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = OrcaText,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = host.memoryUsageGb,
                    fontSize = 10.sp,
                    color = OrcaMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun UsageCard(usage: ModelUsage) {
    val progress = (usage.tokensUsed.toFloat() / usage.tokenLimit.toFloat()).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = OrcaSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = usage.modelName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaText
                    )
                    Text(
                        text = usage.provider,
                        fontSize = 10.sp,
                        color = OrcaMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", usage.costDollars)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrcaAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "reset in ${usage.resetTimeRemaining}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (progress > 0.85f) OrcaDanger else OrcaAccent,
                trackColor = OrcaTerminalBg
            )

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${usage.tokensUsed / 1000}k used",
                    fontSize = 10.sp,
                    color = OrcaMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${usage.tokenLimit / 1000}k limit",
                    fontSize = 10.sp,
                    color = OrcaMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
