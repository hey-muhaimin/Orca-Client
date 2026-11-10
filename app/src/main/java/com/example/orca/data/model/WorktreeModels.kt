package com.example.orca.data.model

enum class HostType(val label: String) {
    LOCAL("Local Machine"),
    SSH("Remote SSH"),
    CLOUD_VM("Cloud VM"),
    DOCKER("Docker Sandbox")
}

enum class HostStatus {
    ONLINE,
    BUSY,
    OFFLINE
}

data class HostEnvironment(
    val id: String,
    val name: String,
    val type: HostType,
    val endpoint: String,
    val status: HostStatus = HostStatus.ONLINE,
    val cpuPercent: Int = 18,
    val memoryUsageGb: String = "4.2 / 16 GB"
)

data class Worktree(
    val id: String,
    val repoName: String,
    val branchName: String,
    val relativePath: String,
    val host: HostEnvironment,
    val activeAgentsCount: Int = 1,
    val changesCount: Int = 0,
    val lastActiveTime: String = "Just now",
    val isCoordinatorActive: Boolean = true
)
