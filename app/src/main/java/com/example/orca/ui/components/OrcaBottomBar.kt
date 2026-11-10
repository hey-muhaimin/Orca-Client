package com.example.orca.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.data.model.PairingState
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.ScreenTab

@Composable
fun OrcaBottomBar(
    currentTab: ScreenTab,
    pairing: PairingState,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(OrcaSurface)
            .border(1.dp, OrcaBorder)
            .navigationBarsPadding()
            .testTag("orca_bottom_bar")
    ) {
        // Global Telemetry Status Bar Row: local qwen2.5-3b • 0 B sent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(OrcaTerminalBg)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (pairing.isPaired) OrcaSuccess else OrcaDanger)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "on-device • qwen2.5-3b",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaText
                )
            }

            Text(
                text = "${pairing.latencyMs}ms • 0 B sent",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        NavigationBar(
            containerColor = OrcaSurface,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                Triple(ScreenTab.WORKTREES, Icons.Filled.AccountTree, Icons.Outlined.AccountTree),
                Triple(ScreenTab.AGENT_SESSION, Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
                Triple(ScreenTab.DIFF_REVIEW, Icons.Filled.Difference, Icons.Outlined.Difference),
                Triple(ScreenTab.ISSUES, Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
                Triple(ScreenTab.REMOTE_USAGE, Icons.Filled.Hub, Icons.Outlined.Hub)
            )

            items.forEach { (tab, filledIcon, outlinedIcon) ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) filledIcon else outlinedIcon,
                            contentDescription = tab.title,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = tab.title,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrcaAccent,
                        selectedTextColor = OrcaAccent,
                        unselectedIconColor = OrcaMuted,
                        unselectedTextColor = OrcaMuted,
                        indicatorColor = OrcaSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}
