package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.configureAppCheck
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.VipFloatingHud
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.JarvisViewModel

enum class MainNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    COMMAND_CENTER("HUD", Icons.Default.GraphicEq, "nav_hud"),
    PHONE_CONTROL("Control", Icons.Default.PhoneAndroid, "nav_control"),
    COMMUNICATIONS("Comms", Icons.Default.Call, "nav_comms"),
    ISLAMIC("Islamic", Icons.Default.Mosque, "nav_islamic"),
    SECURITY("Security", Icons.Default.Security, "nav_security")
}

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory Firebase App Check initialization before UI per guidelines
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: JarvisViewModel) {
    var currentDestination by remember { mutableStateOf(MainNavDestination.COMMAND_CENTER) }
    val activeConfirmation by viewModel.activeConfirmation.collectAsState()
    val status by viewModel.status.collectAsState()
    val transcript by viewModel.transcript.collectAsState()
    val floatingHudExpanded by viewModel.floatingHudExpanded.collectAsState()
    val floatingHudEnabled by viewModel.preferences.floatingHudEnabled.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBlack),
        containerColor = JarvisBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MUSLIM JARVIS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = JarvisGoldBright,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(JarvisGoldContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VIP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = JarvisGoldBright
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFloatingHud() },
                        modifier = Modifier.testTag("topbar_hud_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "Toggle Floating HUD",
                            tint = if (floatingHudExpanded) JarvisGoldBright else JarvisTextSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.emergencyStop() },
                        modifier = Modifier.testTag("topbar_emergency_stop")
                    ) {
                        Icon(
                            imageVector = Icons.Default.StopCircle,
                            contentDescription = "Emergency Stop",
                            tint = JarvisRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JarvisSurfaceDark,
                    titleContentColor = JarvisGoldBright
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisSurfaceDark,
                contentColor = JarvisGold,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                MainNavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = if (isSelected) JarvisGoldBright else JarvisTextMuted
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) JarvisGoldBright else JarvisTextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = JarvisGoldContainer
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                MainNavDestination.COMMAND_CENTER -> CommandCenterScreen(viewModel = viewModel)
                MainNavDestination.PHONE_CONTROL -> PhoneControlScreen(viewModel = viewModel)
                MainNavDestination.COMMUNICATIONS -> CommunicationsScreen(viewModel = viewModel)
                MainNavDestination.ISLAMIC -> IslamicSanctuaryScreen(viewModel = viewModel)
                MainNavDestination.SECURITY -> SecurityPrivacyScreen(viewModel = viewModel)
            }

            // Floating Assistant HUD
            if (floatingHudEnabled) {
                VipFloatingHud(
                    status = status,
                    transcript = transcript,
                    isExpanded = floatingHudExpanded,
                    onToggleExpand = { viewModel.toggleFloatingHud() },
                    onMicClick = {
                        if (status == com.example.model.JarvisStatus.LISTENING) {
                            viewModel.stopListening()
                        } else {
                            viewModel.startListening()
                        }
                    },
                    onQuickAction = { cmd ->
                        viewModel.processSpokenCommand(cmd)
                    },
                    onEmergencyHalt = {
                        viewModel.emergencyStop()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }

            // Safety Confirmation Dialog Modal (e.g. before sending SMS)
            if (activeConfirmation != null) {
                ConfirmationDialog(
                    payload = activeConfirmation!!,
                    onDismiss = { viewModel.dismissConfirmation() }
                )
            }
        }
    }
}
