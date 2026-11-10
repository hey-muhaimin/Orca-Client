package com.example.orca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.ui.theme.*

@Composable
fun TerminalAccessoryBar(
    onKeyClick: (String) -> Unit,
    onSlashCommandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardKeys = listOf("ESC", "TAB", "CTRL+C", "↑", "↓", "CLEAR", "KILL")
    val slashCommands = listOf("/status", "/diff", "/approve", "/test", "/search", "/index", "/scopes")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OrcaSurface)
            .border(1.dp, OrcaBorder)
            .padding(vertical = 6.dp)
            .testTag("terminal_accessory_bar")
    ) {
        // Slash command shortcuts row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(slashCommands) { cmd ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OrcaSurfaceElevated)
                        .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                        .clickable { onSlashCommandClick(cmd) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cmd,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrcaAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Virtual keyboard action keys row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(keyboardKeys) { key ->
                val isDanger = key == "CTRL+C" || key == "KILL"
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .widthIn(min = 44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isDanger) OrcaDanger.copy(alpha = 0.15f) else OrcaBackground)
                        .border(1.dp, if (isDanger) OrcaDanger else OrcaBorder, RoundedCornerShape(4.dp))
                        .clickable { onKeyClick(key) }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDanger) OrcaDanger else OrcaText
                    )
                }
            }
        }
    }
}
