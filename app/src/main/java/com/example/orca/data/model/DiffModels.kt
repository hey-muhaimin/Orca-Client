package com.example.orca.data.model

enum class DiffLineType {
    CONTEXT,
    ADDED,
    DELETED
}

data class DiffLine(
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val content: String,
    val type: DiffLineType
)

data class LineAnnotation(
    val id: String,
    val lineIndex: Int,
    val author: String,
    val comment: String,
    val timestamp: String,
    val sentToAgent: Boolean = false
)

enum class FileChangeType {
    MODIFIED,
    ADDED,
    DELETED
}

data class DiffFile(
    val id: String,
    val worktreeId: String,
    val filePath: String,
    val changeType: FileChangeType,
    val additions: Int,
    val deletions: Int,
    val lines: List<DiffLine>,
    val annotations: List<LineAnnotation> = emptyList(),
    val isStaged: Boolean = true
)
