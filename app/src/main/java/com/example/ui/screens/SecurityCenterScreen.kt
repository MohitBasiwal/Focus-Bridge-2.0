package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.database.entity.SecurityEventEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.PuzzleVerificationDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityCenterScreen(
    viewModel: SecurityCenterViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Navigation and pending action state for Puzzle Dialogs
    var puzzleActionName by remember { mutableStateOf<String?>(null) }
    var onPuzzleSuccess by remember { mutableStateOf<(() -> Unit)?>(null) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cohesive atmospheric Aurora Glowing Mesh background layers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicDark)
        )
        // Top-left Indigo Mesh glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        // Bottom-right Violet Mesh glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AuroraCyan.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("SECURITY CENTER", color = PureWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear Logs",
                                tint = PureWhite
                            )
                        }
                    },
                    colors = CenterAlignedTopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // PROTECTION STATUS DASHBOARD BANNER
                item {
                    ProtectionStatusBanner(
                        isActive = state.isProtectionActive,
                        isPaused = state.isProtectionPaused,
                        isBlockingActiveNow = state.isBlockingActiveNow,
                        onTogglePause = {
                            puzzleActionName = if (state.isProtectionPaused) "Resume Protection" else "Pause Protection"
                            onPuzzleSuccess = {
                                viewModel.toggleProtectionPause {
                                    puzzleActionName = null
                                }
                            }
                        }
                    )
                }

                // REQUIRED PERMISSIONS PANEL
                item {
                    PermissionsMonitorCard(
                        permissions = state.permissions,
                        context = context
                    )
                }

                // PUZZLE STRENGTH CONFIGURATION
                item {
                    PuzzleSettingsCard(
                        currentDifficulty = state.puzzleDifficulty,
                        onDifficultyChange = { newDiff ->
                            puzzleActionName = "Set Puzzle Difficulty to $newDiff"
                            onPuzzleSuccess = {
                                viewModel.setPuzzleDifficulty(newDiff)
                                puzzleActionName = null
                            }
                        }
                    )
                }

                // CRITICAL DEVICE RESET CONTROL
                item {
                    ResetAppCard(
                        onResetClick = {
                            puzzleActionName = "Reset App Local Data"
                            onPuzzleSuccess = {
                                viewModel.resetApp {
                                    puzzleActionName = null
                                    onBack()
                                }
                            }
                        }
                    )
                }

                // SECURITY EVENTS LOG HISTORY
                item {
                    Text(
                        text = "LOCAL SECURITY EVENT LOGS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = AuroraCyan,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (state.recentEvents.isEmpty()) {
                    item {
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "No security events recorded.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSlateMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )
                        }
                    }
                } else {
                    items(state.recentEvents) { event ->
                        SecurityEventRow(event)
                    }
                }
            }
        }

        // FULL-SCREEN ERROR WARNING OVERLAY (If crucial permissions are lost)
        val crucialPermissionsGranted = (state.permissions["Accessibility Service"] == true) &&
                (state.permissions["Usage Access"] == true) &&
                (state.permissions["Draw Overlays"] == true)

        if (!crucialPermissionsGranted && state.permissions.isNotEmpty()) {
            FullScreenWarningOverlay(
                permissions = state.permissions,
                context = context
            )
        }

        // DYNAMIC PUZZLE CHALLENGE POPUP
        puzzleActionName?.let { action ->
            PuzzleVerificationDialog(
                difficulty = state.puzzleDifficulty,
                actionName = action,
                onSuccess = {
                    onPuzzleSuccess?.invoke()
                },
                onDismiss = {
                    puzzleActionName = null
                },
                onFailureLog = { details ->
                    viewModel.logFailedPuzzleAttempt(details)
                }
            )
        }
    }
}

@Composable
fun ProtectionStatusBanner(
    isActive: Boolean,
    isPaused: Boolean,
    isBlockingActiveNow: Boolean,
    onTogglePause: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulse Indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isActive) GlassMint.copy(alpha = 0.15f) else AuroraPink.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Shield else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isActive) GlassMint else AuroraPink,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PROTECTION STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSlateMuted
                    )
                    Text(
                        text = if (isActive) "ACTIVE & REINFORCED" else if (isPaused) "PROTECTION PAUSED" else "THREATS DETECTED",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isActive) GlassMint else if (isPaused) AuroraCyan else AuroraPink
                    )
                }
            }

            Divider(color = PureWhite.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BLOCKING STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSlateMuted
                    )
                    Text(
                        text = if (isBlockingActiveNow) "BLOCK RULES RUNNING" else "STANDBY / SCHEDULED",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isBlockingActiveNow) NeonViolet else PureWhite
                    )
                }

                Button(
                    onClick = onTogglePause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaused) GlassMint.copy(alpha = 0.2f) else AuroraPink.copy(alpha = 0.2f),
                        contentColor = if (isPaused) GlassMint else AuroraPink
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "UNPAUSE" else "PAUSE", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun PermissionsMonitorCard(
    permissions: Map<String, Boolean>,
    context: Context
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "SYSTEM PERMISSIONS STATUS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = NeonViolet
            )

            permissions.forEach { (name, granted) ->
                PermissionStatusRow(
                    name = name,
                    isGranted = granted,
                    onConfigureClick = {
                        openSystemSettingIntent(context, name)
                    }
                )
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    name: String,
    isGranted: Boolean,
    onConfigureClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isGranted) GlassMint else AuroraPink,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) PureWhite else TextSlateLight
            )
        }

        if (!isGranted) {
            Text(
                text = "ACTIVATE",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = AuroraCyan,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuroraCyan.copy(alpha = 0.12f))
                    .clickable { onConfigureClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        } else {
            Text(
                text = "SECURE",
                style = MaterialTheme.typography.bodySmall,
                color = GlassMint.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun PuzzleSettingsCard(
    currentDifficulty: String,
    onDifficultyChange: (String) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "PUZZLE AUTH STRENGTH",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = AuroraCyan
            )

            Text(
                text = "Critical actions require answering mental calculations before modifications are authorized. Select difficulty level:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlateMuted
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                    val isSelected = currentDifficulty.uppercase() == diff
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NeonViolet.copy(alpha = 0.2f) else PureWhite.copy(alpha = 0.04f))
                            .border(
                                1.dp,
                                if (isSelected) NeonViolet else PureWhite.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (!isSelected) {
                                    onDifficultyChange(diff)
                                }
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) PureWhite else TextSlateLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResetAppCard(
    onResetClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderStroke = BorderStroke(1.dp, AuroraPink.copy(alpha = 0.15f))
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "DANGER RECONFIGURATION AREA",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = AuroraPink
            )
            Text(
                text = "Reset all schedules, blocking logs, whitelists, and event histories completely. Requires resolving puzzle challenge.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlateMuted
            )
            Button(
                onClick = onResetClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AuroraPink.copy(alpha = 0.15f),
                    contentColor = AuroraPink
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESET ALL DEVICE CONFIGURATIONS", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun SecurityEventRow(event: SecurityEventEntity) {
    val sdf = remember { SimpleDateFormat("HH:mm:ss (dd MMM)", Locale.getDefault()) }
    val formattedTime = remember(event.timestamp) { sdf.format(Date(event.timestamp)) }

    val color = when (event.severity.uppercase()) {
        "CRITICAL" -> AuroraPink
        "WARNING" -> AuroraCyan
        else -> PureWhite
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.eventType,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted
                )
            }

            Text(
                text = event.details,
                style = MaterialTheme.typography.bodySmall,
                color = TextSlateLight
            )
        }
    }
}

@Composable
fun FullScreenWarningOverlay(
    permissions: Map<String, Boolean>,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDark.copy(alpha = 0.98f))
            .clickable(enabled = false) {}, // Intercept clicks
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AuroraPink.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = null,
                    tint = AuroraPink,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = "PROTECTION DEACTIVATED",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = PureWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Focus Bridge security requires all essential parameters enabled to intercept blocked websites and lock distracting apps. One or more mandatory parameters were disabled:",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSlateMuted,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                permissions.forEach { (name, granted) ->
                    if (!granted && name != "Notifications") { // Ignore non-essential notification permission for overlay warning
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PureWhite.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                .border(1.dp, AuroraPink.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .clickable { openSystemSettingIntent(context, name) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                                Text(text = "Tap to configure in system Settings", style = MaterialTheme.typography.bodySmall, color = TextSlateMuted)
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = AuroraPink)
                        }
                    }
                }
            }

            Text(
                text = "Ensure all settings are reactivated to resume offline focus shield protection.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlateMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun openSystemSettingIntent(context: Context, permissionName: String) {
    try {
        val intent = when (permissionName) {
            "Accessibility Service" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            "Usage Access" -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            "Draw Overlays" -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            "Notifications" -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
            }
            else -> Intent(Settings.ACTION_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to general settings if exact package details URL fails
        try {
            val fallbackIntent = when (permissionName) {
                "Accessibility Service" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                "Usage Access" -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                "Draw Overlays" -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                else -> Intent(Settings.ACTION_SETTINGS)
            }
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        } catch (ex: Exception) {
            // Ignore
        }
    }
}
