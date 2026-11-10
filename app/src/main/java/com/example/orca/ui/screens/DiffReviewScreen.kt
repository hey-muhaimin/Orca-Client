package com.example.orca.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
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
import com.example.orca.data.model.DiffFile
import com.example.orca.data.model.DiffLine
import com.example.orca.data.model.DiffLineType
import com.example.orca.ui.theme.*
import com.example.orca.ui.viewmodel.OrcaViewModel
import com.example.orca.ui.viewmodel.ScreenTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffReviewScreen(
    viewModel: OrcaViewModel,
    modifier: Modifier = Modifier
) {
    val diffFiles by viewModel.diffFiles.collectAsState()
    var selectedFileId by remember(diffFiles) { mutableStateOf(diffFiles.firstOrNull()?.id ?: "") }
    val activeFile = diffFiles.find { it.id == selectedFileId } ?: diffFiles.firstOrNull()

    var annotatingLineIndex by remember { mutableStateOf<Int?>(null) }
    var annotationText by remember { mutableStateOf("") }
    var showPrModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OrcaBackground)
            .testTag("diff_review_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "AI Diff Review",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrcaText
                )
                Text(
                    text = "Tap line to send annotation to agent prompt",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaMuted
                )
            }

            Button(
                onClick = { showPrModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("btn_draft_pr")
            ) {
                Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Draft PR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

        // Changed Files Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(diffFiles) { file ->
                val isSelected = file.id == activeFile?.id
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isSelected) OrcaSurfaceElevated else OrcaSurface,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isSelected) OrcaAccent else OrcaBorder
                        )
                    ),
                    modifier = Modifier.clickable { selectedFileId = file.id }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = file.filePath.substringAfterLast('/'),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OrcaAccent else OrcaText,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+${file.additions}",
                            fontSize = 10.sp,
                            color = OrcaSuccess,
                            fontFamily = FontFamily.Monospace
                        )
                        if (file.deletions > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "-${file.deletions}",
                                fontSize = 10.sp,
                                color = OrcaDanger,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = OrcaBorder)

        // Diff Code View
        if (activeFile != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(OrcaTerminalBg)
            ) {
                // File path banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OrcaSurface)
                            .border(1.dp, OrcaBorder)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeFile.filePath,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = OrcaMuted
                        )
                    }
                }

                itemsIndexed(activeFile.lines) { index, line ->
                    val lineNumber = line.newLineNumber ?: line.oldLineNumber ?: (index + 1)
                    val lineAnnotation = activeFile.annotations.find { it.lineIndex == lineNumber }

                    Column {
                        DiffLineRow(
                            line = line,
                            lineIndex = lineNumber,
                            hasAnnotation = lineAnnotation != null,
                            onClick = {
                                annotatingLineIndex = lineNumber
                                annotationText = lineAnnotation?.comment ?: ""
                            }
                        )

                        // Render existing annotation inline
                        if (lineAnnotation != null) {
                            Surface(
                                color = OrcaSurfaceElevated,
                                shape = RoundedCornerShape(4.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OrcaWarning)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null, tint = OrcaWarning, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${lineAnnotation.author}: ${lineAnnotation.comment}",
                                        fontSize = 11.sp,
                                        color = OrcaWarning,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Line Annotation Bottom Sheet Dialog
    if (annotatingLineIndex != null) {
        val targetLine = annotatingLineIndex!!
        AlertDialog(
            onDismissRequest = { annotatingLineIndex = null },
            title = {
                Text(
                    text = "Annotate Line $targetLine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = OrcaText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Attach targeted line feedback directly to the agent's prompt context.",
                        fontSize = 11.sp,
                        color = OrcaMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = annotationText,
                        onValueChange = { annotationText = it },
                        placeholder = { Text("e.g. clamp minimum delay to 50ms...", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier.fillMaxWidth().testTag("input_diff_annotation"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrcaAccent,
                            unfocusedBorderColor = OrcaBorder,
                            focusedTextColor = OrcaText,
                            unfocusedTextColor = OrcaText
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (activeFile != null && annotationText.isNotBlank()) {
                            viewModel.addAnnotation(activeFile.id, targetLine, annotationText)
                        }
                        annotatingLineIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("btn_send_annotation")
                ) {
                    Text("Send to Agent", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { annotatingLineIndex = null }) {
                    Text("Cancel", color = OrcaMuted, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = OrcaSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // PR Draft Dialog
    if (showPrModal) {
        PrDraftDialog(
            activeFileCount = diffFiles.size,
            onDismiss = { showPrModal = false },
            onCreated = {
                showPrModal = false
                viewModel.sendPrompt("Prepare commit and open PR with verified diffs.")
                viewModel.setTab(ScreenTab.AGENT_SESSION)
            }
        )
    }
}

@Composable
private fun DiffLineRow(
    line: DiffLine,
    lineIndex: Int,
    hasAnnotation: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when (line.type) {
        DiffLineType.ADDED -> OrcaSuccess.copy(alpha = 0.12f)
        DiffLineType.DELETED -> OrcaDanger.copy(alpha = 0.12f)
        DiffLineType.CONTEXT -> Color.Transparent
    }
    val textColor = when (line.type) {
        DiffLineType.ADDED -> OrcaSuccess
        DiffLineType.DELETED -> OrcaDanger
        DiffLineType.CONTEXT -> OrcaMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = lineIndex.toString().padStart(4, ' '),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = if (hasAnnotation) OrcaWarning else OrcaMuted,
            modifier = Modifier.width(32.dp)
        )

        val symbol = when (line.type) {
            DiffLineType.ADDED -> "+"
            DiffLineType.DELETED -> "-"
            DiffLineType.CONTEXT -> " "
        }
        Text(
            text = symbol,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(14.dp)
        )

        Text(
            text = line.content.removePrefix("+").removePrefix("-"),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )

        if (hasAnnotation) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = "Annotated",
                tint = OrcaWarning,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

@Composable
private fun PrDraftDialog(
    activeFileCount: Int,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    var title by remember { mutableStateOf("feat(push): add exponential backoff with full jitter to dispatcher") }
    var description by remember {
        mutableStateOf(
            """### Overview
- Clamps retry backoff to 30s ceiling.
- Full Vitest suite passes with 100% boundary assertions.

Closes #108, ORCA-429"""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Draft Pull Request ($activeFileCount files)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                color = OrcaText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("PR Title", fontSize = 11.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrcaAccent,
                        unfocusedBorderColor = OrcaBorder,
                        focusedTextColor = OrcaText,
                        unfocusedTextColor = OrcaText
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_pr_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", fontSize = 11.sp) },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrcaAccent,
                        unfocusedBorderColor = OrcaBorder,
                        focusedTextColor = OrcaText,
                        unfocusedTextColor = OrcaText
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_pr_body")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreated,
                colors = ButtonDefaults.buttonColors(containerColor = OrcaAccent),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.testTag("btn_submit_pr")
            ) {
                Text("Push & Open PR", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
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
