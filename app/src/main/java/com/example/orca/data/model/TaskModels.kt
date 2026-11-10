package com.example.orca.data.model

enum class IssueSource(val label: String) {
    GITHUB("GitHub"),
    LINEAR("Linear")
}

enum class IssuePriority(val label: String, val colorHex: Long) {
    URGENT("Urgent", 0xFFEF4444),
    HIGH("High", 0xFFF59E0B),
    MEDIUM("Medium", 0xFF3B82F6),
    LOW("Low", 0xFF94A3B8)
}

enum class IssueStatus(val label: String) {
    TODO("Todo"),
    IN_PROGRESS("In Progress"),
    IN_REVIEW("In Review"),
    DONE("Done")
}

data class IssueItem(
    val id: String,
    val identifier: String, // e.g. "ORCA-429" or "#108"
    val title: String,
    val description: String,
    val source: IssueSource,
    val priority: IssuePriority,
    val status: IssueStatus,
    val labels: List<String>,
    val assignee: String,
    val targetBranch: String? = null,
    val assignedAgent: String? = null
)

data class ModelUsage(
    val id: String,
    val modelName: String,
    val provider: String,
    val tokensUsed: Long,
    val tokenLimit: Long,
    val costDollars: Double,
    val resetTimeRemaining: String
)

data class PairingState(
    val isPaired: Boolean = true,
    val hostName: String = "MacBook Pro (192.168.0.109:6768)",
    val relayUrl: String = "ws://192.168.0.109:6768",
    val latencyMs: Int = 14,
    val connectedSince: String = "Connected now",
    val activeTunnels: Int = 3,
    val endpoint: String = "ws://192.168.0.109:6768",
    val deviceToken: String = "0c916044b1f09d10788e68be70c4df980fbfd6e0d6f196ba",
    val publicKeyB64: String = "zQlpQG/82CqlJJ8VpW9zdPCS4pmGsMynUFBaassldCA=",
    val pairedDeviceId: String = "d8345e0c-c932-443c-89da-e2b72a6966cc",
    val scope: String = "runtime",
    val installationId: String = "f132fd9b-9205-4e4e-9bfa-223130bfde4f",
    val machineBinding: String = "Hem9dWkRx6Ts10QKddg_srqUCA9_aGE0BBmvTve8dGU",
    val rawPairingUri: String = "orca://pair?code=eyJ2IjoyLCJlbmRwb2ludCI6IndzOi8vMTkyLjE2OC4wLjEwOTo2NzY4IiwiZGV2aWNlVG9rZW4iOiIwYzkxNjA0NGIxZjA5ZDEwNzg4ZTY4YmU3MGM0ZGY5ODBmYmZkNmUwZDZmMTk2YmEiLCJwdWJsaWNLZXlCNjQiOiJ6UWxwUUcvODJDcWxKSjhWcFc5emRQQ1M0cG1Hc015blVGQmFhc3NsZENBPSIsInBhaXJlZERldmljZUlkIjoiZDgzNDVlMGMtYzkzMi00NDNjLTg5ZGEtZTJiNzJhNjk2NmNjIiwic2NvcGUiOiJydW50aW1lIiwiaG9zdERlc2NyaXB0b3IiOnsiaW5zdGFsbGF0aW9uSWQiOiJmMTMyZmQ5Yi05MjA1LTRlNGUtOWJmYS0yMjMxMzBiZmRlNGYiLCJtYWNoaW5lQmluZGluZyI6IkhlbTlkV2tSeDZUczEwUUtkZGdfc3JxVUNBOV9hR0UwQkJtdlR2ZThkR1UifX0="
)

