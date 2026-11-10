package com.example.orca

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.orca.ui.components.OrcaBottomBar
import com.example.orca.ui.components.OrcaTopBar
import com.example.orca.ui.components.PairingDialog
import com.example.orca.ui.screens.*
import com.example.orca.ui.theme.OrcaBackground
import com.example.orca.ui.theme.OrcaTheme
import com.example.orca.ui.viewmodel.OrcaViewModel
import com.example.orca.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: OrcaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            OrcaTheme {
                OrcaMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "orca" && data.host == "pair") {
            viewModel.pairWithPayload(data.toString())
        }
    }
}

@Composable
fun OrcaMainScreen(viewModel: OrcaViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedWorktree by viewModel.selectedWorktree.collectAsState()
    val pairing by viewModel.pairing.collectAsState()
    var showPairingDialog by remember { mutableStateOf(false) }

    // Back handling: if on secondary tab, return to Workspaces tab
    if (currentTab != ScreenTab.WORKTREES) {
        BackHandler {
            viewModel.setTab(ScreenTab.WORKTREES)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OrcaBackground)
            .testTag("orca_main_scaffold"),
        topBar = {
            OrcaTopBar(
                selectedWorktree = selectedWorktree,
                pairing = pairing,
                onSwitchWorktreeClick = { viewModel.setTab(ScreenTab.WORKTREES) },
                onOpenPairingClick = { showPairingDialog = true }
            )
        },
        bottomBar = {
            OrcaBottomBar(
                currentTab = currentTab,
                pairing = pairing,
                onTabSelected = { tab -> viewModel.setTab(tab) }
            )
        },
        containerColor = OrcaBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.WORKTREES -> WorktreesScreen(viewModel = viewModel)
                ScreenTab.AGENT_SESSION -> AgentSessionScreen(viewModel = viewModel)
                ScreenTab.DIFF_REVIEW -> DiffReviewScreen(viewModel = viewModel)
                ScreenTab.ISSUES -> IssuesScreen(viewModel = viewModel)
                ScreenTab.REMOTE_USAGE -> RemoteUsageScreen(viewModel = viewModel)
            }
        }
    }

    if (showPairingDialog) {
        PairingDialog(
            pairing = pairing,
            onDismiss = { showPairingDialog = false },
            onPairSuccess = {
                showPairingDialog = false
            }
        )
    }
}
