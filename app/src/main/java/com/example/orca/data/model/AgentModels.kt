package com.example.orca.data.model

enum class AgentType(val displayName: String, val badgeColorHex: Long) {
    CLAUDE_CODE("Claude Code", 0xFFD97706),
    CODEX("Codex", 0xFF10B981),
    CURSOR("Cursor", 0xFF00E5FF),
    DEVIN("Devin", 0xFF8B5CF6),
    GROK("Grok", 0xFFEC4899),
    PI("Pi Agent", 0xFF3B82F6),
    DEEPSEEK("DeepSeek", 0xFF06B6D4)
}

enum class AgentRole {
    COORDINATOR,
    WORKER
}

enum class AgentStatus(val label: String) {
    IDLE("Idle"),
    THINKING("Thinking..."),
    EXECUTING("Executing Tool"),
    AWAITING_APPROVAL("Needs Approval"),
    COMPLETED("Completed"),
    ERROR("Error")
}

data class AgentInfo(
    val id: String,
    val name: String,
    val type: AgentType,
    val role: AgentRole,
    val status: AgentStatus,
    val worktreeId: String,
    val currentTask: String,
    val tokensUsed: Long
)

enum class ToolStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class ToolCall(
    val id: String,
    val toolName: String,
    val command: String,
    val path: String = "",
    val rationale: String,
    val status: ToolStatus = ToolStatus.PENDING
)

enum class MessageSender {
    USER,
    AGENT,
    SYSTEM
}

data class ChatMessage(
    val id: String,
    val sender: MessageSender,
    val content: String,
    val timestamp: String,
    val reasoning: String? = null,
    val toolCall: ToolCall? = null,
    val relatedDiffPath: String? = null
)

enum class TerminalLineType {
    INPUT,
    OUTPUT,
    INFO,
    WARN,
    ERROR,
    SUCCESS
}

data class TerminalLine(
    val id: String,
    val text: String,
    val type: TerminalLineType = TerminalLineType.OUTPUT,
    val timestamp: String = ""
)
