package com.example.orca.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.orca.data.model.*
import com.example.orca.data.repository.OrcaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class ScreenTab(val title: String) {
    WORKTREES("Workspaces"),
    AGENT_SESSION("Agent"),
    DIFF_REVIEW("Diffs"),
    ISSUES("Tasks"),
    REMOTE_USAGE("Remote & Limits")
}

enum class SessionMode {
    CHAT,
    TERMINAL
}

class OrcaViewModel(
    private val repository: OrcaRepository = OrcaRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow(ScreenTab.WORKTREES)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _sessionMode = MutableStateFlow(SessionMode.CHAT)
    val sessionMode: StateFlow<SessionMode> = _sessionMode.asStateFlow()

    val worktrees: StateFlow<List<Worktree>> = repository.worktrees
    val selectedWorktreeId: StateFlow<String> = repository.selectedWorktreeId
    val agents: StateFlow<List<AgentInfo>> = repository.agents
    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
    val terminalLines: StateFlow<List<TerminalLine>> = repository.terminalLines
    val diffFiles: StateFlow<List<DiffFile>> = repository.diffFiles
    val issues: StateFlow<List<IssueItem>> = repository.issues
    val usageMetrics: StateFlow<List<ModelUsage>> = repository.usageMetrics
    val pairing: StateFlow<PairingState> = repository.pairing
    val availableHosts: List<HostEnvironment> = repository.availableHosts

    val selectedWorktree: StateFlow<Worktree?> = combine(worktrees, selectedWorktreeId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun setSessionMode(mode: SessionMode) {
        _sessionMode.value = mode
    }

    fun selectWorktree(id: String) {
        repository.selectWorktree(id)
    }

    fun createWorktree(repoName: String, branchName: String, hostId: String, initialAgent: AgentType) {
        repository.createWorktree(repoName, branchName, hostId, initialAgent)
    }

    fun sendPrompt(prompt: String) {
        repository.sendUserMessage(prompt)
    }

    fun approveToolCall(toolId: String) {
        repository.approveToolCall(toolId)
    }

    fun rejectToolCall(toolId: String) {
        repository.rejectToolCall(toolId)
    }

    fun runTerminalCommand(command: String) {
        repository.executeTerminalCommand(command)
    }

    fun addAnnotation(fileId: String, lineIndex: Int, text: String) {
        repository.addLineAnnotation(fileId, lineIndex, text)
    }

    fun pairWithPayload(rawInput: String): Boolean {
        return repository.pairWithPayload(rawInput)
    }

    fun unpair() {
        repository.unpair()
    }

    fun startWorktreeForIssue(issue: IssueItem) {
        repository.startWorktreeForIssue(issue)
        _currentTab.value = ScreenTab.AGENT_SESSION
    }
}
