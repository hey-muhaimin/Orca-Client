package com.example.orca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.example.orca.data.model.PairingState
import com.example.orca.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingDialog(
    pairing: PairingState,
    onDismiss: () -> Unit,
    onPairSuccess: (hostName: String) -> Unit
) {
    var manualCode by remember { mutableStateOf("orca-pair-m3-studio-9481") }
    var isScanning by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = OrcaAccent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Pair Desktop Orca",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OrcaText
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Pair with your desktop workspace to monitor background agents, approve tools, and send prompts remotely.",
                    fontSize = 12.sp,
                    color = OrcaMuted
                )

                // Simulated QR Camera Scanner Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OrcaTerminalBg)
                        .border(1.dp, if (isScanning) OrcaSuccess else OrcaBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.CheckCircle else Icons.Default.QrCode,
                            contentDescription = null,
                            tint = if (isScanning) OrcaSuccess else OrcaAccent,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isScanning) "Camera Viewfinder Active • Align QR" else "Scan QR in Orca Desktop",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isScanning) OrcaSuccess else OrcaMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = { isScanning = !isScanning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isScanning) OrcaSuccess else OrcaSurfaceElevated
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_toggle_camera_scan")
                ) {
                    Icon(
                        imageVector = if (isScanning) Icons.Default.CameraAlt else Icons.Default.CropFree,
                        contentDescription = null,
                        tint = if (isScanning) OrcaBackground else OrcaText,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Scanning QR..." else "Open QR Camera Scanner",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isScanning) OrcaBackground else OrcaText
                    )
                }

                HorizontalDivider(color = OrcaBorder)

                Text(
                    text = "Or enter manual pairing token:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrcaText
                )

                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    label = { Text("Pairing Token or Relay URL", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrcaAccent,
                        unfocusedBorderColor = OrcaBorder,
                        focusedTextColor = OrcaText,
                        unfocusedTextColor = OrcaText
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_manual_pairing_code")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPairSuccess("MacBook Pro (M3 Max) • Studio 1")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("btn_confirm_pairing")
            ) {
                Text("Connect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OrcaMuted, fontSize = 13.sp)
            }
        },
        containerColor = OrcaSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
