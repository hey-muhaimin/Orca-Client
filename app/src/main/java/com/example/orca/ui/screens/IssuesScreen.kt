package com.example.orca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.orca.data.model.IssueItem
import com.example.orca.data.model.IssuePriority
import com.example.orca.data.model.IssueSource
import com.example.orca.data.model.IssueStatus
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.OrcaViewModel

@Composable
fun IssuesScreen(
    viewModel: OrcaViewModel,
    modifier: Modifier = Modifier
) {
    val allIssues by viewModel.issues.collectAsState()
    var selectedSource by remember { mutableStateOf<IssueSource?>(null) }

    val displayedIssues = remember(allIssues, selectedSource) {
        if (selectedSource == null) allIssues else allIssues.filter { it.source == selectedSource }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrcaBackground)
            .testTag("issues_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Tasks & Issues",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OrcaText
            )
            Text(
                text = "Linear and GitHub tasks connected directly to agent workspaces",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Source Selector Filter
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedSource == null,
                    onClick = { selectedSource = null },
                    label = { Text("All (${allIssues.size})", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrcaSurfaceElevated,
                        selectedLabelColor = OrcaAccent
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                FilterChip(
                    selected = selectedSource == IssueSource.LINEAR,
                    onClick = { selectedSource = IssueSource.LINEAR },
                    label = { Text("Linear", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrcaSurfaceElevated,
                        selectedLabelColor = OrcaAccent
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                FilterChip(
                    selected = selectedSource == IssueSource.GITHUB,
                    onClick = { selectedSource = IssueSource.GITHUB },
                    label = { Text("GitHub", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrcaSurfaceElevated,
                        selectedLabelColor = OrcaAccent
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }

        HorizontalDivider(color = OrcaBorder)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(displayedIssues, key = { it.id }) { issue ->
                IssueCard(
                    issue = issue,
                    onStartWorkspace = { viewModel.startWorktreeForIssue(issue) }
                )
            }
        }
    }
}

@Composable
private fun IssueCard(
    issue: IssueItem,
    onStartWorkspace: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = OrcaSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("issue_card_${issue.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(OrcaSurfaceElevated)
                            .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = issue.identifier,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (issue.source == IssueSource.LINEAR) OrcaCodeBlue else OrcaAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = issue.source.label,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaMuted
                    )
                }

                // Priority Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OrcaTerminalBg)
                        .border(1.dp, Color(issue.priority.colorHex), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = issue.priority.label.lowercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(issue.priority.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = issue.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = OrcaText
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.description,
                fontSize = 11.sp,
                color = OrcaMuted,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Labels
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                issue.labels.forEach { label ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(OrcaTerminalBg)
                            .border(1.dp, OrcaBorder, RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "#$label",
                            fontSize = 10.sp,
                            color = OrcaMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = OrcaBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrcaMuted, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = issue.assignee,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaMuted
                    )
                }

                if (issue.status == IssueStatus.IN_PROGRESS) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(OrcaAccent.copy(alpha = 0.15f))
                            .border(1.dp, OrcaAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "agent working",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = OrcaAccent
                        )
                    }
                } else {
                    Button(
                        onClick = onStartWorkspace,
                        colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_start_issue_${issue.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Start Worktree",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
