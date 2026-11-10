package com.example.orca.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orca.data.model.*
import com.example.orca.ui.components.TerminalAccessoryBar
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.OrcaViewModel
import com.example.orca.ui.viewmodel.SessionMode

@Composable
fun AgentSessionScreen(
    viewModel: OrcaViewModel,
    modifier: Modifier = Modifier
) {
    val sessionMode by viewModel.sessionMode.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val terminalLines by viewModel.terminalLines.collectAsState()
    val agents by viewModel.agents.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    var isDictating by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrcaBackground)
            .testTag("agent_session_screen")
    ) {
        // Mode Switcher Banner: Chat UI <-> Terminal CLI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(OrcaSurface)
                .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            val isChat = sessionMode == SessionMode.CHAT
            TabButton(
                title = "Chat Mode",
                icon = Icons.Default.ChatBubbleOutline,
                isSelected = isChat,
                onClick = { viewModel.setSessionMode(SessionMode.CHAT) },
                modifier = Modifier.weight(1f).testTag("tab_chat_mode")
            )
            TabButton(
                title = "Terminal Remote",
                icon = Icons.Default.Terminal,
                isSelected = !isChat,
                onClick = { viewModel.setSessionMode(SessionMode.TERMINAL) },
                modifier = Modifier.weight(1f).testTag("tab_terminal_mode")
            )
        }

        // Active Agents Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Text(
                    text = "AGENTS:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
            }
            items(agents) { agent ->
                AgentPill(agent = agent)
            }
        }

        HorizontalDivider(color = OrcaBorder)

        // Main View: Chat or Terminal
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (sessionMode == SessionMode.CHAT) {
                ChatTimeline(
                    messages = chatMessages,
                    onApproveTool = { viewModel.approveToolCall(it) },
                    onRejectTool = { viewModel.rejectToolCall(it) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                TerminalView(
                    lines = terminalLines,
                    onExecuteCommand = { viewModel.runTerminalCommand(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Terminal Remote Accessory Bar (for Terminal mode)
        if (sessionMode == SessionMode.TERMINAL) {
            TerminalAccessoryBar(
                onKeyClick = { key ->
                    when (key) {
                        "CLEAR" -> viewModel.runTerminalCommand("clear")
                        "CTRL+C" -> viewModel.runTerminalCommand("^C (SIGINT sent)")
                        "KILL" -> viewModel.runTerminalCommand("orca agent kill")
                        else -> viewModel.runTerminalCommand("[$key]")
                    }
                },
                onSlashCommandClick = { cmd ->
                    val cleanCmd = cmd.removePrefix("/")
                    viewModel.runTerminalCommand("orca $cleanCmd")
                }
            )
        }

        // Input bar (for Chat mode)
        if (sessionMode == SessionMode.CHAT) {
            Surface(
                color = OrcaSurface,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Helper slash command tag shortcuts arrayed horizontally above prompt block
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(listOf("/search", "/index", "/scopes", "/status", "/diff", "/test")) { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(OrcaSurfaceElevated)
                                    .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                                    .clickable {
                                        inputPrompt = "$tag "
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = OrcaAccent
                                )
                            }
                        }
                    }

                    if (isDictating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(OrcaAccent.copy(alpha = 0.15f))
                                .border(1.dp, OrcaAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = OrcaAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dictating prompt... tap mic to stop",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = OrcaAccent
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputPrompt,
                            onValueChange = { inputPrompt = it },
                            placeholder = {
                                Text(
                                    "Steer agent or input instruction...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = OrcaMuted
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrcaAccent,
                                unfocusedBorderColor = OrcaBorder,
                                focusedTextColor = OrcaText,
                                unfocusedTextColor = OrcaText,
                                focusedContainerColor = OrcaBackground,
                                unfocusedContainerColor = OrcaBackground
                            ),
                            shape = RoundedCornerShape(4.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputPrompt.isNotBlank()) {
                                    viewModel.sendPrompt(inputPrompt)
                                    inputPrompt = ""
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_agent_prompt")
                        )

                        // Voice Dictation Button
                        IconButton(
                            onClick = {
                                isDictating = !isDictating
                                if (isDictating && inputPrompt.isEmpty()) {
                                    inputPrompt = "Refactor the push retry dispatcher with exponential backoff."
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isDictating) OrcaAccent else OrcaSurfaceElevated
                            ),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                                .testTag("btn_dictate")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictation",
                                tint = if (isDictating) Color.White else OrcaText,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Send Prompt Button
                        IconButton(
                            onClick = {
                                if (inputPrompt.isNotBlank()) {
                                    viewModel.sendPrompt(inputPrompt)
                                    inputPrompt = ""
                                    isDictating = false
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = OrcaAccent),
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, OrcaAccent, RoundedCornerShape(4.dp))
                                .testTag("btn_send_prompt")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) OrcaSurfaceElevated else Color.Transparent,
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) OrcaAccent else OrcaMuted,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) OrcaText else OrcaMuted
            )
        }
    }
}

@Composable
private fun AgentPill(agent: AgentInfo) {
    val statusColor = when (agent.status) {
        AgentStatus.AWAITING_APPROVAL -> OrcaWarning
        AgentStatus.EXECUTING -> OrcaAccent
        AgentStatus.THINKING -> OrcaCodeBlue
        AgentStatus.COMPLETED -> OrcaSuccess
        AgentStatus.IDLE -> OrcaMuted
        AgentStatus.ERROR -> OrcaDanger
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(OrcaSurface)
            .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = agent.name.take(12),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = OrcaText
            )
        }
    }
}

@Composable
private fun ChatTimeline(
    messages: List<ChatMessage>,
    onApproveTool: (String) -> Unit,
    onRejectTool: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            when (msg.sender) {
                MessageSender.USER -> UserMessageBubble(msg)
                MessageSender.AGENT -> AgentMessageCard(
                    message = msg,
                    onApproveTool = onApproveTool,
                    onRejectTool = onRejectTool
                )
                MessageSender.SYSTEM -> SystemMessageBubble(msg)
            }
        }
    }
}

@Composable
private fun UserMessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(OrcaSurfaceElevated)
                .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                .padding(10.dp)
        ) {
            Column {
                Text(
                    text = message.content,
                    color = OrcaText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    color = OrcaMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun AgentMessageCard(
    message: ChatMessage,
    onApproveTool: (String) -> Unit,
    onRejectTool: (String) -> Unit
) {
    var expandedReasoning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(OrcaSurface)
            .border(1.dp, OrcaBorder, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = OrcaAccent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "claude-3.7-sonnet",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaText
                )
            }
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message.content,
            fontSize = 13.sp,
            color = OrcaText,
            lineHeight = 18.sp
        )

        // Collapsible Reasoning Block
        if (message.reasoning != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = OrcaTerminalBg,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaBorder)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedReasoning = !expandedReasoning }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "▸ thought",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = OrcaMuted
                        )
                        Icon(
                            imageVector = if (expandedReasoning) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = OrcaMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    if (expandedReasoning) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = message.reasoning,
                            fontSize = 11.sp,
                            color = OrcaMuted,
                            lineHeight = 16.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Tool Execution Tray / Action Prompt
        if (message.toolCall != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ToolExecutionTray(
                toolCall = message.toolCall,
                onApprove = { onApproveTool(message.toolCall.id) },
                onReject = { onRejectTool(message.toolCall.id) }
            )
        }
    }
}

/**
 * Tool Execution Tray matching pocketai specs:
 * Outlined surface cards enclosing tool signatures (e.g. ▸ search_memory, ▸ set_alarm)
 * styled with an end-aligned status pill ('ok 61 ms' or 'awaiting approval').
 * Vertical 1.dp accent line running along left margin next to input queries.
 * Semantic layout split horizontally into [Deny] vs [Allow once].
 */
@Composable
private fun ToolExecutionTray(
    toolCall: ToolCall,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = OrcaTerminalBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                when (toolCall.status) {
                    ToolStatus.PENDING -> OrcaWarning
                    ToolStatus.APPROVED -> OrcaSuccess
                    ToolStatus.REJECTED -> OrcaDanger
                }
            )
        ),
        modifier = Modifier.fillMaxWidth().testTag("tool_execution_tray")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header: Signature + End-aligned Status Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "▸ ${toolCall.toolName.lowercase().replace(' ', '_')}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrcaText
                )

                // End-aligned status pill
                val (pillBg, pillFg, pillText) = when (toolCall.status) {
                    ToolStatus.PENDING -> Triple(OrcaWarning.copy(alpha = 0.2f), OrcaWarning, "awaiting")
                    ToolStatus.APPROVED -> Triple(OrcaSuccess.copy(alpha = 0.2f), OrcaSuccess, "ok 61 ms")
                    ToolStatus.REJECTED -> Triple(OrcaDanger.copy(alpha = 0.2f), OrcaDanger, "denied")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(pillBg)
                        .border(1.dp, pillFg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pillText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = pillFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interior content with vertical 1.dp accent line running along the left margin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Vertical 1.dp accent line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(OrcaAccent)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "$ ${toolCall.command}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = OrcaText,
                        lineHeight = 15.sp
                    )

                    if (toolCall.rationale.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "query: \"${toolCall.rationale}\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = OrcaMuted
                        )
                    }
                }
            }

            // Action Prompt Banners / Approval Sheet
            if (toolCall.status == ToolStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = OrcaBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // [Deny] styled with danger boundaries
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrcaDanger),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(OrcaDanger)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_reject_tool")
                    ) {
                        Text(
                            text = "Deny",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            color = OrcaDanger
                        )
                    }

                    // [Allow once] in solid white text background
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrcaText),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("btn_approve_tool")
                    ) {
                        Text(
                            text = "Allow once",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = OrcaBackground
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemMessageBubble(message: ChatMessage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message.content,
            fontSize = 10.sp,
            color = OrcaMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TerminalView(
    lines: List<TerminalLine>,
    onExecuteCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val terminalState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            terminalState.animateScrollToItem(lines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .background(OrcaTerminalBg)
            .padding(10.dp)
            .testTag("terminal_view")
    ) {
        // Terminal output log lines
        LazyColumn(
            state = terminalState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(lines, key = { it.id }) { line ->
                val textColor = when (line.type) {
                    TerminalLineType.INPUT -> OrcaAccent
                    TerminalLineType.SUCCESS -> OrcaSuccess
                    TerminalLineType.WARN -> OrcaWarning
                    TerminalLineType.ERROR -> OrcaDanger
                    TerminalLineType.INFO -> OrcaCodeBlue
                    TerminalLineType.OUTPUT -> OrcaMuted
                }
                Text(
                    text = line.text,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = textColor,
                    lineHeight = 15.sp
                )
            }
        }

        // Command Prompt Input Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .background(OrcaSurface, RoundedCornerShape(4.dp))
                .border(1.dp, OrcaBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "orca $ ",
                color = OrcaAccent,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            TextField(
                value = commandInput,
                onValueChange = { commandInput = it },
                placeholder = {
                    Text(
                        "enter CLI command...",
                        color = OrcaMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = OrcaText,
                    unfocusedTextColor = OrcaText,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (commandInput.isNotBlank()) {
                        onExecuteCommand(commandInput)
                        commandInput = ""
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_terminal_command")
            )
        }
    }
}
