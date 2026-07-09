package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.FocusSession
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Highly polished responsive Dashboard screen implementing Premium Glassmorphic design.
 * Automatically adapts between compact (phone) and expanded (tablet/landscape) viewports.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTimer: (Int, String) -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToTimetable: () -> Unit,
    onNavigateToBlocker: () -> Unit,
    onNavigateToSpeechChallenge: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Collect one-shot View Events (like Toasts or Navigation triggers)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is DashboardEvent.NavigateToTimer -> {
                    onNavigateToTimer(event.minutes, event.category)
                }
                is DashboardEvent.Message -> {
                    Toast.makeText(context, event.content, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Dynamic layout determination based on simple orientation/width rules (Adaptive Always)
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cohesive atmospheric Aurora Glowing Mesh background layers (Matches premium design HTML)
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

        val isWideScreen = maxWidth > 600.dp

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "FOCUS BRIDGE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                            ),
                            color = PureWhite
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToAnalytics) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = "Deep Analytics",
                                tint = GlassMint
                            )
                        }
                        IconButton(onClick = onNavigateToSecurity) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security Center",
                                tint = AuroraCyan
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSlateMuted
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (isWideScreen) {
                // Expanded visual format: Side-by-side pane layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Left Column: Timer Configs & Action triggers
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SecurityStatusBanner(
                            isPaused = state.isProtectionPaused,
                            onClick = onNavigateToSecurity,
                            modifier = Modifier.fillMaxWidth()
                        )
                        DashboardAnalyticsBanner(
                            score = state.focusScore,
                            completedCount = state.todayCompletedSessionsCount,
                            totalMinutes = state.todayFocusTimeMinutes,
                            streak = state.currentStreak,
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TimerControlCard(
                            defaultMinutes = state.defaultMinutes,
                            selectedCategory = state.selectedCategory,
                            onAction = viewModel::onAction
                        )
                        AppBlockerCard(
                            isBlockingEnabled = state.isAppBlockingEnabled,
                            onAction = viewModel::onAction,
                            onConfigureShield = onNavigateToBlocker
                        )
                        VocalUnlockCard(
                            onNavigateToSpeechChallenge = onNavigateToSpeechChallenge
                        )
                    }

                    // Right Column: Active session logger history pane
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UpcomingSessionCard(
                            session = state.upcomingStudySession,
                            onNavigateToTimetable = onNavigateToTimetable
                        )
                        QuickStatsCard(sessions = state.recentSessions)
                        RecentLogsCard(
                            sessions = state.recentSessions,
                            onClearLogs = { viewModel.onAction(DashboardAction.ClearHistory) }
                        )
                    }
                }
            } else {
                // Standard visual format: Single scrollable column layout (Compact screens)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        SecurityStatusBanner(
                            isPaused = state.isProtectionPaused,
                            onClick = onNavigateToSecurity,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        DashboardAnalyticsBanner(
                            score = state.focusScore,
                            completedCount = state.todayCompletedSessionsCount,
                            totalMinutes = state.todayFocusTimeMinutes,
                            streak = state.currentStreak,
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        UpcomingSessionCard(
                            session = state.upcomingStudySession,
                            onNavigateToTimetable = onNavigateToTimetable
                        )
                    }
                    item {
                        TimerControlCard(
                            defaultMinutes = state.defaultMinutes,
                            selectedCategory = state.selectedCategory,
                            onAction = viewModel::onAction
                        )
                    }
                    item {
                        AppBlockerCard(
                            isBlockingEnabled = state.isAppBlockingEnabled,
                            onAction = viewModel::onAction,
                            onConfigureShield = onNavigateToBlocker
                        )
                    }
                    item {
                        VocalUnlockCard(
                            onNavigateToSpeechChallenge = onNavigateToSpeechChallenge
                        )
                    }
                    item {
                        RecentLogsCard(
                            sessions = state.recentSessions,
                            onClearLogs = { viewModel.onAction(DashboardAction.ClearHistory) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingSessionCard(
    session: com.example.domain.model.StudySession?,
    onNavigateToTimetable: () -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE",
                    style = MaterialTheme.typography.labelLarge,
                    color = AuroraCyan
                )
                
                Text(
                    text = "VIEW TIMETABLE",
                    color = NeonViolet,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onNavigateToTimetable() }
                        .padding(4.dp)
                )
            }

            if (session == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TextSlateMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No classes scheduled for today",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "Enjoy your study breaks or schedule a class!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(NeonViolet.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, NeonViolet), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.subjectName,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                            color = PureWhite
                        )
                        Text(
                            text = "${session.startTime} - ${session.endTime} (${session.repeatType})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            GlassmorphicButton(
                text = "OPEN SCHEDULE MATRIX",
                onClick = onNavigateToTimetable,
                icon = Icons.Default.DateRange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimerControlCard(
    defaultMinutes: Int,
    selectedCategory: String,
    onAction: (DashboardAction) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "BRIDGE CONTROL",
                style = MaterialTheme.typography.labelLarge,
                color = NeonViolet
            )

            // Dynamic Category selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf("Work", "Study", "Coding")
                categories.forEach { category ->
                    val isSelected = category == selectedCategory
                    Button(
                        onClick = { onAction(DashboardAction.SelectCategory(category)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) NeonViolet.copy(alpha = 0.2f) else GlassBase,
                            contentColor = if (isSelected) PureWhite else TextSlateMuted
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = category, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Quick minute selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val times = listOf(15, 25, 45, 60)
                times.forEach { mins ->
                    val isSelected = mins == defaultMinutes
                    IconButton(
                        onClick = { onAction(DashboardAction.SelectMinutes(mins)) },
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) AuroraCyan.copy(alpha = 0.25f) else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                    ) {
                        Text(
                            text = "${mins}m",
                            color = if (isSelected) AuroraCyan else TextSlateMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            GlassmorphicButton(
                text = "ACTIVATE BRIDGE",
                onClick = { onAction(DashboardAction.StartFocusSession) },
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppBlockerCard(
    isBlockingEnabled: Boolean,
    onAction: (DashboardAction) -> Unit,
    onConfigureShield: () -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AuroraPink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "DISTRACTION BARRIER",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "Locks unlisted apps during sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                    }
                }

                Switch(
                    checked = isBlockingEnabled,
                    onCheckedChange = { onAction(DashboardAction.ToggleAppBlocking(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GlassMint,
                        checkedTrackColor = NeonViolet.copy(alpha = 0.5f),
                        uncheckedThumbColor = TextSlateMuted,
                        uncheckedTrackColor = GlassBase
                    )
                )
            }

            GlassmorphicButton(
                text = "CONFIGURE SHIELD",
                onClick = onConfigureShield,
                icon = Icons.Default.Settings,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun RecentLogsCard(
    sessions: List<FocusSession>,
    onClearLogs: () -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BRIDGE HISTORY LOGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = AuroraCyan
                )

                if (sessions.isNotEmpty()) {
                    TextButton(onClick = onClearLogs) {
                        Text("Clear", color = AuroraPink, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextSlateDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No focus records found on this device",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                    }
                }
            } else {
                sessions.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (session.success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (session.success) GlassMint else AuroraPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = session.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSlateLight
                            )
                        }
                        Text(
                            text = "${session.durationMinutes} min",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStatsCard(sessions: List<FocusSession>) {
    val totalMins = sessions.sumOf { if (it.success) it.durationMinutes else 0 }
    val count = sessions.count { it.success }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TOTAL FOCUS", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                Text(
                    text = "${totalMins}m",
                    style = MaterialTheme.typography.titleLarge,
                    color = GlassMint,
                    fontWeight = FontWeight.Black
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SESSIONS", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge,
                    color = AuroraCyan,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun VocalUnlockCard(
    onNavigateToSpeechChallenge: () -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = AuroraCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "VOCAL INTEGRITY UNLOCK",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite
                        )
                        Text(
                            text = "Read educational text to bypass limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                    }
                }
            }

            GlassmorphicButton(
                text = "OPEN CHALLENGE CONSOLE",
                onClick = onNavigateToSpeechChallenge,
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SecurityStatusBanner(
    isPaused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.clickable { onClick() },
        glassColor = if (isPaused) AuroraPink.copy(alpha = 0.25f) else GlassMint.copy(alpha = 0.25f),
        borderColor = if (isPaused) AuroraPink else GlassMint
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.Warning else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isPaused) AuroraPink else GlassMint,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = if (isPaused) "PROTECTION PAUSED" else "SYSTEM SHIELD ACTIVE",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                    Text(
                        text = if (isPaused) "Required permissions missing. Tap to fix!" else "Security Center monitoring active.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlateMuted
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = PureWhite.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun DashboardAnalyticsBanner(
    score: Int,
    completedCount: Int,
    totalMinutes: Int,
    streak: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.clickable { onClick() },
        glassColor = NeonViolet.copy(alpha = 0.15f),
        borderColor = NeonViolet
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = GlassMint,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "FOCUS CORE ANALYTICS",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = PureWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular rating bubble
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(GlassMint.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, GlassMint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                            color = PureWhite
                        )
                    }
                    Column {
                        Text(
                            text = "SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSlateMuted
                        )
                        Text(
                            text = when {
                                score >= 85 -> "Excellent"
                                score >= 70 -> "Strong"
                                score >= 50 -> "Stable"
                                else -> "Cold"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassMint
                        )
                    }
                }

                // Streak & Sessions details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "STREAK", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${streak}d",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = PureWhite
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TODAY", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AuroraCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = "$completedCount",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = PureWhite
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TIME", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = AuroraPink, modifier = Modifier.size(14.dp))
                            Text(
                                text = "${totalMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = PureWhite
                            )
                        }
                    }
                }
            }
        }
    }
}

