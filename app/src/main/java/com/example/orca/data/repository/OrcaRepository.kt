package com.example.orca.data.repository

import com.example.orca.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID

class OrcaRepository {

    // --- Hosts ---
    private val localHost = HostEnvironment(
        id = "host-local",
        name = "Local Host (Daemon)",
        type = HostType.LOCAL,
        endpoint = "unix:///var/run/orca.sock",
        status = HostStatus.ONLINE,
        cpuPercent = 24,
        memoryUsageGb = "5.8 / 32 GB"
    )

    private val sshHost = HostEnvironment(
        id = "host-ssh",
        name = "AWS dev-west-2 (EC2)",
        type = HostType.SSH,
        endpoint = "ubuntu@ec2-54-218-12-8.compute-1.amazonaws.com",
        status = HostStatus.ONLINE,
        cpuPercent = 42,
        memoryUsageGb = "8.1 / 16 GB"
    )

    private val cloudVmHost = HostEnvironment(
        id = "host-cloud",
        name = "GCP Cloud Run Worker (asia-east1)",
        type = HostType.CLOUD_VM,
        endpoint = "relay://orca-runner-cell-04.internal",
        status = HostStatus.ONLINE,
        cpuPercent = 12,
        memoryUsageGb = "2.4 / 8 GB"
    )

    val availableHosts = listOf(localHost, sshHost, cloudVmHost)

    // --- Worktrees ---
    private val initialWorktrees = listOf(
        Worktree(
            id = "wt-1",
            repoName = "hey-muhaimin/orca",
            branchName = "feat/orchestrator-core",
            relativePath = "~/dev/orca/.worktrees/feat-orchestrator-core",
            host = localHost,
            activeAgentsCount = 2,
            changesCount = 4,
            lastActiveTime = "Active now",
            isCoordinatorActive = true
        ),
        Worktree(
            id = "wt-2",
            repoName = "hey-muhaimin/orca",
            branchName = "fix/relay-websocket-drain",
            relativePath = "~/dev/orca/.worktrees/fix-relay-websocket-drain",
            host = cloudVmHost,
            activeAgentsCount = 1,
            changesCount = 2,
            lastActiveTime = "3m ago",
            isCoordinatorActive = false
        ),
        Worktree(
            id = "wt-3",
            repoName = "stablyai/cloud-relay",
            branchName = "linear/ORCA-429-auth-stream",
            relativePath = "~/dev/cloud-relay/.worktrees/linear-429",
            host = sshHost,
            activeAgentsCount = 1,
            changesCount = 5,
            lastActiveTime = "12m ago",
            isCoordinatorActive = false
        )
    )

    private val _worktrees = MutableStateFlow(initialWorktrees)
    val worktrees: StateFlow<List<Worktree>> = _worktrees.asStateFlow()

    private val _selectedWorktreeId = MutableStateFlow("wt-1")
    val selectedWorktreeId: StateFlow<String> = _selectedWorktreeId.asStateFlow()

    // --- Agents ---
    private val _agents = MutableStateFlow(
        listOf(
            AgentInfo(
                id = "agent-1",
                name = "Claude 3.7 Sonnet",
                type = AgentType.CLAUDE_CODE,
                role = AgentRole.COORDINATOR,
                status = AgentStatus.AWAITING_APPROVAL,
                worktreeId = "wt-1",
                currentTask = "Implementing push event drain retry policy & test suite",
                tokensUsed = 84_520
            ),
            AgentInfo(
                id = "agent-2",
                name = "OpenAI Codex Worker",
                type = AgentType.CODEX,
                role = AgentRole.WORKER,
                status = AgentStatus.EXECUTING,
                worktreeId = "wt-1",
                currentTask = "Writing Vitest unit cases for push-background-database.ts",
                tokensUsed = 42_190
            ),
            AgentInfo(
                id = "agent-3",
                name = "Cursor Agent",
                type = AgentType.CURSOR,
                role = AgentRole.COORDINATOR,
                status = AgentStatus.IDLE,
                worktreeId = "wt-2",
                currentTask = "Audit memory leaks in websocket splice forwarder",
                tokensUsed = 21_800
            ),
            AgentInfo(
                id = "agent-4",
                name = "DeepSeek Coder",
                type = AgentType.DEEPSEEK,
                role = AgentRole.COORDINATOR,
                status = AgentStatus.THINKING,
                worktreeId = "wt-3",
                currentTask = "Drafting Linear authentication token validation layer",
                tokensUsed = 59_300
            )
        )
    )
    val agents: StateFlow<List<AgentInfo>> = _agents.asStateFlow()

    // --- Chat Messages ---
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "msg-1",
                sender = MessageSender.USER,
                content = "Add exponential backoff with jitter to the push retry loop, and verify with tests.",
                timestamp = "10:14 AM"
            ),
            ChatMessage(
                id = "msg-2",
                sender = MessageSender.AGENT,
                content = "I have inspected `cloud/apps/push/src/push-dispatcher.ts` and identified the fixed 500ms delay. I will replace it with full jitter exponential backoff capped at 30 seconds.",
                timestamp = "10:14 AM",
                reasoning = "Analyzing existing retry mechanics: currently retry count is checked against MAX_RETRIES=5, but backoff uses Math.min(initialDelay * 2, maxDelay) without random entropy, creating thundering herds under APNS blips."
            ),
            ChatMessage(
                id = "msg-3",
                sender = MessageSender.AGENT,
                content = "I modified `push-dispatcher.ts` and added unit test cases. I need permission to execute the test suite in the local worktree.",
                timestamp = "10:15 AM",
                toolCall = ToolCall(
                    id = "tool-1",
                    toolName = "Bash Execution",
                    command = "pnpm --filter @orca-cloud/push test",
                    path = "cloud/apps/push",
                    rationale = "Verify that the jitter backoff calculation produces delays strictly between 0 and 30,000 ms over 1,000 iterations.",
                    status = ToolStatus.PENDING
                ),
                relatedDiffPath = "cloud/apps/push/src/push-dispatcher.ts"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // --- Terminal Logs ---
    private val _terminalLines = MutableStateFlow(
        listOf(
            TerminalLine("t-1", "=== Orca Orchestrator v1.0.0 (worktree: feat/orchestrator-core) ===", TerminalLineType.INFO),
            TerminalLine("t-2", "[daemon] Connected to local unix socket /var/run/orca.sock", TerminalLineType.SUCCESS),
            TerminalLine("t-3", "[coordinator] Claude Code initialized in isolated worktree directory", TerminalLineType.INFO),
            TerminalLine("t-4", "$ orca worker spawn --agent codex --task 'unit tests'", TerminalLineType.INPUT),
            TerminalLine("t-5", "[worker-1] Spawned Codex worker on PID 94812", TerminalLineType.SUCCESS),
            TerminalLine("t-6", "[coordinator] Awaiting human approval for tool execution (Tool ID: tool-1)", TerminalLineType.WARN)
        )
    )
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    // --- Diffs ---
    private val _diffFiles = MutableStateFlow(
        listOf(
            DiffFile(
                id = "diff-1",
                worktreeId = "wt-1",
                filePath = "cloud/apps/push/src/push-dispatcher.ts",
                changeType = FileChangeType.MODIFIED,
                additions = 18,
                deletions = 4,
                lines = listOf(
                    DiffLine(38, 38, " export function computeRetryDelay(attempt: number): number {", DiffLineType.CONTEXT),
                    DiffLine(39, null, "-  return Math.min(1000 * Math.pow(2, attempt), 30000)", DiffLineType.DELETED),
                    DiffLine(null, 39, "+  // Apply full jitter exponential backoff to avoid synchronized thundering herd", DiffLineType.ADDED),
                    DiffLine(null, 40, "+  const baseDelay = Math.min(1000 * Math.pow(2, attempt), 30_000)", DiffLineType.ADDED),
                    DiffLine(null, 41, "+  const jitter = Math.random() * baseDelay", DiffLineType.ADDED),
                    DiffLine(null, 42, "+  return Math.floor(jitter)", DiffLineType.ADDED),
                    DiffLine(40, 43, " }", DiffLineType.CONTEXT),
                    DiffLine(41, 44, "", DiffLineType.CONTEXT),
                    DiffLine(42, 45, " export async function dispatchPushWithRetry(payload: PushMessage) {", DiffLineType.CONTEXT),
                    DiffLine(null, 46, "+  const attemptLimit = payload.maxRetries ?? 5", DiffLineType.ADDED),
                    DiffLine(43, 47, "   for (let i = 0; i < attemptLimit; i++) {", DiffLineType.CONTEXT)
                ),
                annotations = listOf(
                    LineAnnotation(
                        id = "ann-1",
                        lineIndex = 41,
                        author = "You",
                        comment = "Add minimum delay clamp so jitter doesn't produce 0ms immediately.",
                        timestamp = "10:16 AM",
                        sentToAgent = true
                    )
                ),
                isStaged = true
            ),
            DiffFile(
                id = "diff-2",
                worktreeId = "wt-1",
                filePath = "cloud/apps/push/src/push-dispatcher.test.ts",
                changeType = FileChangeType.ADDED,
                additions = 32,
                deletions = 0,
                lines = listOf(
                    DiffLine(null, 1, "+import { describe, it, expect } from 'vitest'", DiffLineType.ADDED),
                    DiffLine(null, 2, "+import { computeRetryDelay } from './push-dispatcher'", DiffLineType.ADDED),
                    DiffLine(null, 3, "+", DiffLineType.ADDED),
                    DiffLine(null, 4, "+describe('computeRetryDelay', () => {", DiffLineType.ADDED),
                    DiffLine(null, 5, "+  it('respects upper ceiling cap of 30s', () => {", DiffLineType.ADDED),
                    DiffLine(null, 6, "+    for (let i = 0; i < 100; i++) {", DiffLineType.ADDED),
                    DiffLine(null, 7, "+      const delay = computeRetryDelay(10)", DiffLineType.ADDED),
                    DiffLine(null, 8, "+      expect(delay).toBeLessThanOrEqual(30000)", DiffLineType.ADDED),
                    DiffLine(null, 9, "+    }", DiffLineType.ADDED),
                    DiffLine(null, 10, "+  })", DiffLineType.ADDED),
                    DiffLine(null, 11, "+})", DiffLineType.ADDED)
                ),
                annotations = emptyList(),
                isStaged = true
            )
        )
    )
    val diffFiles: StateFlow<List<DiffFile>> = _diffFiles.asStateFlow()

    // --- Issues / Tasks ---
    private val _issues = MutableStateFlow(
        listOf(
            IssueItem(
                id = "iss-1",
                identifier = "ORCA-429",
                title = "Support seamless handover between Chat and CLI terminal",
                description = "Users need to toggle active agent context from Chat bubble UI to raw terminal PTY stream without restarting conversation.",
                source = IssueSource.LINEAR,
                priority = IssuePriority.URGENT,
                status = IssueStatus.IN_PROGRESS,
                labels = listOf("ux", "agent-core", "terminal"),
                assignee = "Muhaimin",
                targetBranch = "feat/orchestrator-core",
                assignedAgent = "Claude 3.7 Sonnet"
            ),
            IssueItem(
                id = "iss-2",
                identifier = "ORCA-435",
                title = "Cloud relay reconnect circuit breaker for flaky connections",
                description = "Handle network partitions and automatic session resumption via pre-authenticated splice slots.",
                source = IssueSource.LINEAR,
                priority = IssuePriority.HIGH,
                status = IssueStatus.TODO,
                labels = listOf("backend", "relay", "networking"),
                assignee = "Muhaimin"
            ),
            IssueItem(
                id = "iss-3",
                identifier = "#128",
                title = "Annotate diff lines and send feedback directly to agent prompt",
                description = "Reviewers want to click on added/removed lines in diff view and post focused inline review comments.",
                source = IssueSource.GITHUB,
                priority = IssuePriority.HIGH,
                status = IssueStatus.IN_REVIEW,
                labels = listOf("git", "review", "enhancement"),
                assignee = "Muhaimin",
                targetBranch = "feat/orchestrator-core"
            ),
            IssueItem(
                id = "iss-4",
                identifier = "#142",
                title = "Mobile companion pairing via encrypted QR and push token sync",
                description = "Allow engineers to steer long-running coding agents on their phones when away from desktop.",
                source = IssueSource.GITHUB,
                priority = IssuePriority.MEDIUM,
                status = IssueStatus.DONE,
                labels = listOf("mobile", "apns", "fcm"),
                assignee = "Muhaimin"
            )
        )
    )
    val issues: StateFlow<List<IssueItem>> = _issues.asStateFlow()

    // --- Usage & Limits ---
    private val _usageMetrics = MutableStateFlow(
        listOf(
            ModelUsage(
                id = "u-1",
                modelName = "Claude 3.7 Sonnet (Hybrid)",
                provider = "Anthropic",
                tokensUsed = 348_200,
                tokenLimit = 500_000,
                costDollars = 4.18,
                resetTimeRemaining = "3h 42m"
            ),
            ModelUsage(
                id = "u-2",
                modelName = "OpenAI Codex / o3-mini",
                provider = "OpenAI",
                tokensUsed = 189_400,
                tokenLimit = 400_000,
                costDollars = 2.45,
                resetTimeRemaining = "14h 10m"
            ),
            ModelUsage(
                id = "u-3",
                modelName = "Grok 3 (Beta)",
                provider = "xAI",
                tokensUsed = 48_100,
                tokenLimit = 250_000,
                costDollars = 0.85,
                resetTimeRemaining = "1d 2h"
            ),
            ModelUsage(
                id = "u-4",
                modelName = "DeepSeek Coder V3",
                provider = "DeepSeek",
                tokensUsed = 512_000,
                tokenLimit = 1_000_000,
                costDollars = 1.10,
                resetTimeRemaining = "6h 20m"
            )
        )
    )
    val usageMetrics: StateFlow<List<ModelUsage>> = _usageMetrics.asStateFlow()

    private val _pairing = MutableStateFlow(PairingState())
    val pairing: StateFlow<PairingState> = _pairing.asStateFlow()

    // --- Actions ---

    fun selectWorktree(id: String) {
        _selectedWorktreeId.value = id
    }

    fun createWorktree(repoName: String, branchName: String, hostId: String, initialAgent: AgentType) {
        val host = availableHosts.find { it.id == hostId } ?: localHost
        val newId = "wt-${System.currentTimeMillis() % 10000}"
        val newWorktree = Worktree(
            id = newId,
            repoName = repoName,
            branchName = branchName,
            relativePath = "~/dev/$repoName/.worktrees/$branchName",
            host = host,
            activeAgentsCount = 1,
            changesCount = 0,
            lastActiveTime = "Just now",
            isCoordinatorActive = true
        )
        val newAgent = AgentInfo(
            id = "agent-${System.currentTimeMillis() % 10000}",
            name = "${initialAgent.displayName} Coordinator",
            type = initialAgent,
            role = AgentRole.COORDINATOR,
            status = AgentStatus.IDLE,
            worktreeId = newId,
            currentTask = "Ready for instructions in $branchName",
            tokensUsed = 0
        )
        _worktrees.update { listOf(newWorktree) + it }
        _agents.update { it + newAgent }
        _selectedWorktreeId.value = newId

        _terminalLines.update {
            it + TerminalLine(
                id = UUID.randomUUID().toString(),
                text = "[$branchName] New worktree created on ${host.name} with ${initialAgent.displayName}",
                type = TerminalLineType.SUCCESS
            )
        }
    }

    fun sendUserMessage(content: String) {
        if (content.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.USER,
            content = content,
            timestamp = "Just now"
        )
        _chatMessages.update { it + userMsg }

        // Simulate agent processing
        _terminalLines.update {
            it + TerminalLine(
                id = UUID.randomUUID().toString(),
                text = "$ orca agent prompt: \"${content.take(45)}\"",
                type = TerminalLineType.INPUT
            )
        }

        // Simulate agent intelligent response after user prompt
        val responseMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = MessageSender.AGENT,
            content = "I've reviewed your request: \"$content\". I'm analyzing the workspace files, adjusting the implementation, and preparing the required changes.",
            timestamp = "Just now",
            reasoning = "Parsing user instruction, inspecting AST diffs and preparing targeted patch for verification."
        )
        _chatMessages.update { it + responseMsg }
    }

    fun approveToolCall(toolId: String) {
        _chatMessages.update { messages ->
            messages.map { msg ->
                if (msg.toolCall?.id == toolId) {
                    msg.copy(toolCall = msg.toolCall.copy(status = ToolStatus.APPROVED))
                } else msg
            }
        }
        _terminalLines.update {
            it + listOf(
                TerminalLine(UUID.randomUUID().toString(), "[approval] Tool approved by user: executing in sandbox...", TerminalLineType.SUCCESS),
                TerminalLine(UUID.randomUUID().toString(), "$ pnpm --filter @orca-cloud/push test", TerminalLineType.INPUT),
                TerminalLine(UUID.randomUUID().toString(), "✓ 14 tests passed in push-dispatcher.test.ts (248ms)", TerminalLineType.SUCCESS)
            )
        }
        _agents.update { list ->
            list.map { if (it.id == "agent-1") it.copy(status = AgentStatus.COMPLETED) else it }
        }
    }

    fun rejectToolCall(toolId: String) {
        _chatMessages.update { messages ->
            messages.map { msg ->
                if (msg.toolCall?.id == toolId) {
                    msg.copy(toolCall = msg.toolCall.copy(status = ToolStatus.REJECTED))
                } else msg
            }
        }
        _terminalLines.update {
            it + TerminalLine(UUID.randomUUID().toString(), "[approval] Tool rejected by user. Agent aborting command execution.", TerminalLineType.ERROR)
        }
    }

    fun executeTerminalCommand(cmd: String) {
        if (cmd.isBlank()) return
        val inputLine = TerminalLine(UUID.randomUUID().toString(), "$ $cmd", TerminalLineType.INPUT)
        val responseLine = when {
            cmd.trim() == "orca status" -> TerminalLine(
                UUID.randomUUID().toString(),
                "Worktree: feat/orchestrator-core | Host: Local | Active Agents: 2 | Changes: 2 files (+50, -4)",
                TerminalLineType.SUCCESS
            )
            cmd.trim() == "orca diff" -> TerminalLine(
                UUID.randomUUID().toString(),
                "2 files modified:\n  - cloud/apps/push/src/push-dispatcher.ts (+18, -4)\n  - cloud/apps/push/src/push-dispatcher.test.ts (+32, -0)",
                TerminalLineType.INFO
            )
            cmd.trim().startsWith("orca approve") -> TerminalLine(
                UUID.randomUUID().toString(),
                "Approved pending tool execution.",
                TerminalLineType.SUCCESS
            )
            cmd.trim() == "orca test" -> TerminalLine(
                UUID.randomUUID().toString(),
                "Running test suite: all 18 unit tests passed cleanly.",
                TerminalLineType.SUCCESS
            )
            else -> TerminalLine(
                UUID.randomUUID().toString(),
                "Command executed on host (${availableHosts[0].name}): exit code 0",
                TerminalLineType.OUTPUT
            )
        }
        _terminalLines.update { it + listOf(inputLine, responseLine) }
    }

    fun addLineAnnotation(fileId: String, lineIndex: Int, comment: String) {
        if (comment.isBlank()) return
        val newAnnotation = LineAnnotation(
            id = UUID.randomUUID().toString(),
            lineIndex = lineIndex,
            author = "You",
            comment = comment,
            timestamp = "Just now",
            sentToAgent = true
        )
        _diffFiles.update { files ->
            files.map { file ->
                if (file.id == fileId) {
                    file.copy(annotations = file.annotations + newAnnotation)
                } else file
            }
        }
        // Send annotation feedback to agent chat
        _chatMessages.update {
            it + ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = MessageSender.USER,
                content = "Feedback on line $lineIndex: \"$comment\"",
                timestamp = "Just now"
            )
        }
    }

    fun pairWithPayload(rawInput: String): Boolean {
        try {
            val trimmed = rawInput.trim()
            val base64Payload = when {
                trimmed.startsWith("orca://") -> {
                    val uri = Uri.parse(trimmed)
                    uri.getQueryParameter("code") ?: trimmed.substringAfter("code=")
                }
                trimmed.contains("code=") -> trimmed.substringAfter("code=")
                else -> trimmed
            }

            val decodedBytes = Base64.decode(base64Payload, Base64.DEFAULT)
            val jsonString = String(decodedBytes, StandardCharsets.UTF_8)
            val json = JSONObject(jsonString)

            val endpoint = json.optString("endpoint", "ws://192.168.0.109:6768")
            val deviceToken = json.optString("deviceToken", "")
            val publicKeyB64 = json.optString("publicKeyB64", "")
            val pairedDeviceId = json.optString("pairedDeviceId", "")
            val scope = json.optString("scope", "runtime")

            var installationId = ""
            var machineBinding = ""
            if (json.has("hostDescriptor")) {
                val hostDesc = json.getJSONObject("hostDescriptor")
                installationId = hostDesc.optString("installationId", "")
                machineBinding = hostDesc.optString("machineBinding", "")
            }

            val hostLabel = if (endpoint.contains("://")) {
                val clean = endpoint.substringAfter("://")
                "MacBook Pro ($clean)"
            } else {
                "MacBook Pro ($endpoint)"
            }

            _pairing.value = PairingState(
                isPaired = true,
                hostName = hostLabel,
                relayUrl = endpoint,
                latencyMs = (10..18).random(),
                connectedSince = "Just now",
                activeTunnels = 3,
                endpoint = endpoint,
                deviceToken = deviceToken,
                publicKeyB64 = publicKeyB64,
                pairedDeviceId = pairedDeviceId,
                scope = scope,
                installationId = installationId,
                machineBinding = machineBinding,
                rawPairingUri = rawInput
            )

            // Emit audit log in terminal
            _terminalLines.update { lines ->
                lines + listOf(
                    TerminalLine(
                        id = UUID.randomUUID().toString(),
                        text = "orca remote pair: Handshake established with $endpoint",
                        type = TerminalLineType.COMMAND
                    ),
                    TerminalLine(
                        id = UUID.randomUUID().toString(),
                        text = "✓ Paired device: ${pairedDeviceId.take(8)}... (scope: $scope, machine: ${machineBinding.take(12)}...)",
                        type = TerminalLineType.SUCCESS
                    )
                )
            }

            // Emit notice in chat
            _chatMessages.update { msgs ->
                msgs + ChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.SYSTEM,
                    content = "Connected to Orca Desktop at $endpoint via encrypted device token. Mobile companion ready.",
                    timestamp = "Just now"
                )
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun unpair() {
        _pairing.value = _pairing.value.copy(
            isPaired = false,
            latencyMs = 0,
            connectedSince = "Disconnected"
        )
    }

    fun startWorktreeForIssue(issue: IssueItem) {
        val branch = issue.targetBranch ?: "linear/${issue.identifier.lowercase()}"
        createWorktree("hey-muhaimin/orca", branch, localHost.id, AgentType.CLAUDE_CODE)
        _issues.update { list ->
            list.map {
                if (it.id == issue.id) {
                    it.copy(
                        status = IssueStatus.IN_PROGRESS,
                        targetBranch = branch,
                        assignedAgent = "Claude 3.7 Sonnet"
                    )
                } else it
            }
        }
    }
}

