package com.example.orca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.data.model.AgentType
import com.example.orca.data.model.HostType
import com.example.orca.data.model.Worktree
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.OrcaViewModel
import com.example.orca.ui.viewmodel.ScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorktreesScreen(
    viewModel: OrcaViewModel,
    modifier: Modifier = Modifier
) {
    val worktrees by viewModel.worktrees.collectAsState()
    val selectedId by viewModel.selectedWorktreeId.collectAsState()
    val availableHosts = viewModel.availableHosts
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("worktrees_screen"),
        containerColor = OrcaBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = OrcaAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("fab_new_worktree")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Worktree", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "New Worktree",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Parallel Workspaces",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrcaText
                )
                Text(
                    text = "Isolated git worktrees with concurrent coding agents",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
            }

            // Host & Environment Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = OrcaSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
                    modifier = Modifier.fillMaxWidth()
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
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(OrcaSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "orca daemon active",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = OrcaText
                                )
                            }
                            Text(
                                text = "3 hosts ready",
                                fontSize = 11.sp,
                                color = OrcaAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HostMetric(label = "Local CPU", value = "24%")
                            HostMetric(label = "SSH Host", value = "dev-west")
                            HostMetric(label = "Cloud VM", value = "asia-east1")
                        }
                    }
                }
            }

            // Worktrees Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE WORKTREES (${worktrees.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaMuted
                    )
                }
            }

            // Worktree Cards
            items(worktrees, key = { it.id }) { worktree ->
                val isSelected = worktree.id == selectedId
                WorktreeCard(
                    worktree = worktree,
                    isSelected = isSelected,
                    onClick = {
                        viewModel.selectWorktree(worktree.id)
                        viewModel.setTab(ScreenTab.AGENT_SESSION)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateWorktreeDialog(
            hosts = availableHosts,
            onDismiss = { showCreateDialog = false },
            onCreate = { repo, branch, hostId, agentType ->
                viewModel.createWorktree(repo, branch, hostId, agentType)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HostMetric(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = OrcaMuted, fontFamily = FontFamily.Monospace)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = OrcaText,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun WorktreeCard(
    worktree: Worktree,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) OrcaSurfaceElevated else OrcaSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) OrcaAccent else OrcaBorder
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("worktree_card_${worktree.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isSelected) OrcaAccent else OrcaMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = worktree.repoName,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = OrcaMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OrcaTerminalBg)
                        .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = worktree.host.name.take(16),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = OrcaText,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Branch Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.AltRoute,
                    contentDescription = null,
                    tint = OrcaAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = worktree.branchName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = worktree.relativePath,
                fontSize = 10.sp,
                color = OrcaMuted,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = OrcaBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Footer info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = OrcaAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${worktree.activeAgentsCount} agents",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = OrcaMuted
                        )
                    }

                    if (worktree.changesCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Difference,
                                contentDescription = null,
                                tint = OrcaSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${worktree.changesCount} files",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = OrcaSuccess
                            )
                        }
                    }
                }

                Text(
                    text = worktree.lastActiveTime,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateWorktreeDialog(
    hosts: List<com.example.orca.data.model.HostEnvironment>,
    onDismiss: () -> Unit,
    onCreate: (repo: String, branch: String, hostId: String, agent: AgentType) -> Unit
) {
    var repo by remember { mutableStateOf("hey-muhaimin/orca") }
    var branch by remember { mutableStateOf("feat/parallel-runner") }
    var selectedHostId by remember { mutableStateOf(hosts.firstOrNull()?.id ?: "") }
    var selectedAgent by remember { mutableStateOf(AgentType.CLAUDE_CODE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Spawn New Worktree",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = repo,
                    onValueChange = { repo = it },
                    label = { Text("Repository", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrcaAccent,
                        unfocusedBorderColor = OrcaBorder,
                        focusedTextColor = OrcaText,
                        unfocusedTextColor = OrcaText
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_repo")
                )

                OutlinedTextField(
                    value = branch,
                    onValueChange = { branch = it },
                    label = { Text("Branch / Worktree Name", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrcaAccent,
                        unfocusedBorderColor = OrcaBorder,
                        focusedTextColor = OrcaText,
                        unfocusedTextColor = OrcaText
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_branch")
                )

                Text(
                    text = "EXECUTION HOST",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hosts.forEach { host ->
                        val isChosen = host.id == selectedHostId
                        FilterChip(
                            selected = isChosen,
                            onClick = { selectedHostId = host.id },
                            label = { Text(host.type.name, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrcaSurfaceElevated,
                                selectedLabelColor = OrcaAccent
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }

                Text(
                    text = "COORDINATOR AGENT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(AgentType.CLAUDE_CODE, AgentType.CODEX, AgentType.CURSOR).forEach { agent ->
                        val isChosen = agent == selectedAgent
                        FilterChip(
                            selected = isChosen,
                            onClick = { selectedAgent = agent },
                            label = { Text(agent.displayName, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OrcaSurfaceElevated,
                                selectedLabelColor = OrcaAccent
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(repo, branch, selectedHostId, selectedAgent) },
                colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("btn_confirm_worktree")
            ) {
                Text("Spawn", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OrcaMuted, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = OrcaSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
