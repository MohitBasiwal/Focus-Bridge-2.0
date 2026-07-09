package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Achievement
import com.example.domain.model.AnalyticsData
import com.example.domain.model.ChartItem
import com.example.domain.model.SubjectDistributionItem
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Stats & Charts, 1: Achievements

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AnalyticsEvent.Message -> {
                    Toast.makeText(context, event.content, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth > 600.dp

        // Cohesive atmospheric Aurora Glowing Mesh background layers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicDark)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AuroraCyan.copy(alpha = 0.12f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "DEEP ANALYTICS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = PureWhite
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuroraCyan)
                }
            } else {
                val data = state.data
                if (data != null) {
                    if (isWideScreen) {
                        // Wide Screen: Dual panel or side-by-side
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                FocusScoreOverviewCard(score = data.focusScore)
                                
                                TabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                                
                                if (selectedTab == 0) {
                                    AnalyticsMetricsGrid(data = data, isWide = true)
                                    SubjectDistributionCard(distribution = data.subjectDistribution)
                                    SimulationControlPanel(onAction = viewModel::onAction)
                                } else {
                                    AchievementsList(achievements = data.achievements)
                                }
                            }

                            if (selectedTab == 0) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    ChartSection(
                                        title = "DAILY STUDY MINUTES (THIS WEEK)",
                                        items = data.dailyChartData,
                                        color = GlassMint,
                                        isMinutes = true
                                    )
                                    ChartSection(
                                        title = "WEEKLY STUDY TIME (HOURS)",
                                        items = data.weeklyChartData,
                                        color = AuroraCyan,
                                        isMinutes = false
                                    )
                                    ChartSection(
                                        title = "MONTHLY STUDY TIME (HOURS)",
                                        items = data.monthlyChartData,
                                        color = NeonViolet,
                                        isMinutes = false
                                    )
                                }
                            }
                        }
                    } else {
                        // Compact screen (Single column)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                FocusScoreOverviewCard(score = data.focusScore)
                            }
                            item {
                                TabSelector(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                            }
                            
                            if (selectedTab == 0) {
                                item {
                                    AnalyticsMetricsGrid(data = data, isWide = false)
                                }
                                item {
                                    ChartSection(
                                        title = "DAILY STUDY MINUTES",
                                        items = data.dailyChartData,
                                        color = GlassMint,
                                        isMinutes = true
                                    )
                                }
                                item {
                                    ChartSection(
                                        title = "WEEKLY STUDY HOURS",
                                        items = data.weeklyChartData,
                                        color = AuroraCyan,
                                        isMinutes = false
                                    )
                                }
                                item {
                                    ChartSection(
                                        title = "MONTHLY STUDY HOURS",
                                        items = data.monthlyChartData,
                                        color = NeonViolet,
                                        isMinutes = false
                                    )
                                }
                                item {
                                    SubjectDistributionCard(distribution = data.subjectDistribution)
                                }
                                item {
                                    SimulationControlPanel(onAction = viewModel::onAction)
                                }
                            } else {
                                item {
                                    AchievementsList(achievements = data.achievements)
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No analytics logs loaded", color = TextSlateMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("STATISTICS & CHARTS", "MILESTONES & TROPHIES")
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NeonViolet.copy(alpha = 0.2f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) NeonViolet else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (isSelected) PureWhite else TextSlateMuted
                    )
                }
            }
        }
    }
}

@Composable
fun FocusScoreOverviewCard(score: Int) {
    val rating = when {
        score >= 85 -> "EXCELLENT BRIDGE ADHERENCE"
        score >= 70 -> "STRONG FOCUS CYCLE"
        score >= 50 -> "STABLE SYSTEM STATUS"
        else -> "SYSTEM COLD / NEEDS STUDY"
    }
    
    val ratingColor = when {
        score >= 85 -> GlassMint
        score >= 70 -> AuroraCyan
        score >= 50 -> NeonViolet
        else -> AuroraPink
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SYSTEM FLOW SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSlateMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rating,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                    color = ratingColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Computed from study duration, timeline adherence, streak, and distraction blockers logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Custom arc drawing for the rating
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                Canvas(modifier = Modifier.size(80.dp)) {
                    // Gray background circle
                    drawCircle(
                        color = GlassBorder.copy(alpha = 0.2f),
                        style = Stroke(width = 8.dp.toPx())
                    )
                    
                    // Score Progress Arc
                    drawArc(
                        color = ratingColor,
                        startAngle = -90f,
                        sweepAngle = (score / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp
                        ),
                        color = PureWhite
                    )
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSlateMuted
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricsGrid(data: AnalyticsData, isWide: Boolean) {
    val items = listOf(
        MetricItem("Total Study Hours", "${data.totalStudyHours}h", Icons.Default.DateRange, AuroraCyan),
        MetricItem("Distractions Blocked", "${data.totalBlockedDistractions}", Icons.Default.Shield, AuroraPink),
        MetricItem("Avg Focus Duration", "${data.averageFocusTimeMinutes}m", Icons.Default.Timer, GlassMint),
        MetricItem("Missed Sessions", "${data.missedSessionsCount}", Icons.Default.Warning, AuroraPink),
        MetricItem("Current Streak", "${data.currentStreak} Days", Icons.Default.Star, NeonViolet),
        MetricItem("Favorite Category", data.mostStudiedSubject, Icons.Default.Favorite, PureWhite)
    )

    if (isWide) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            MetricCard(item = item)
                        }
                    }
                }
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            MetricCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

data class MetricItem(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

@Composable
fun MetricCard(item: MetricItem) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 12.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, item.tint.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted
                )
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                    color = PureWhite
                )
            }
        }
    }
}

@Composable
fun ChartSection(
    title: String,
    items: List<ChartItem>,
    color: Color,
    isMinutes: Boolean
) {
    val maxValue = (items.maxOfOrNull { it.value } ?: 10f).coerceAtLeast(10f)

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            
            // Draw a high fidelity Custom Canvas Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val barHeightRatio = if (maxValue > 0f) item.value / maxValue else 0f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // The Bar
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(90.dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            // Dark background slot
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GlassBorder.copy(alpha = 0.15f))
                            )
                            // Glowing color fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(barHeightRatio)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(color, color.copy(alpha = 0.4f))
                                        )
                                    )
                                    .border(1.dp, color.copy(alpha = 0.8f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            )
                        }
                        
                        // Label
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSlateMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubjectDistributionCard(distribution: List<SubjectDistributionItem>) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "SUBJECT-WISE DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraCyan
            )
            
            if (distribution.isEmpty()) {
                Text(
                    text = "No study distribution data. Log successful sessions to see categorization.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                // Stacked multi-segmented bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(GlassBorder.copy(alpha = 0.15f))
                ) {
                    distribution.forEach { item ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (e: Exception) {
                            AuroraCyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(item.percentage.coerceAtLeast(1f))
                                .fillMaxHeight()
                                .background(parsedColor)
                        )
                    }
                }
                
                // Legends List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    distribution.forEach { item ->
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(item.colorHex))
                        } catch (e: Exception) {
                            AuroraCyan
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(parsedColor, CircleShape)
                                )
                                Text(
                                    text = item.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureWhite
                                )
                            }
                            Text(
                                text = "${item.minutes}m (${Math.round(item.percentage)}%)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextSlateLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementsList(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        achievements.forEach { achievement ->
            val color = if (achievement.isUnlocked) AuroraCyan else TextSlateDark
            
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = if (achievement.isUnlocked) AuroraCyan.copy(alpha = 0.05f) else GlassBase
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Trohpy cup
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(color.copy(alpha = 0.12f), CircleShape)
                            .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (achievement.isUnlocked) AuroraCyan else TextSlateMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = achievement.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                                color = if (achievement.isUnlocked) PureWhite else TextSlateMuted
                            )
                            
                            if (achievement.isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .background(GlassMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .border(1.dp, GlassMint, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "UNLOCKED",
                                        color = GlassMint,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "${Math.round(achievement.progress * 100)}%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextSlateMuted
                                )
                            }
                        }
                        
                        Text(
                            text = achievement.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                        
                        if (!achievement.isUnlocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Progress indicator
                            LinearProgressIndicator(
                                progress = { achievement.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = NeonViolet,
                                trackColor = GlassBorder.copy(alpha = 0.2f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationControlPanel(onAction: (AnalyticsAction) -> Unit) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = AuroraPink.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "SANDBOX SIMULATION CONTROLS",
                style = MaterialTheme.typography.labelSmall,
                color = AuroraPink
            )
            
            Text(
                text = "Since you operate on a sandbox emulator environment, use these controls to inject data and verify stats calculations reactively.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSlateMuted
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassmorphicButton(
                    text = "+25m WORK",
                    onClick = { onAction(AnalyticsAction.SimulateSuccessSession(25, "Work")) },
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f),
                    glowAccent = AuroraCyan.copy(alpha = 0.15f)
                )
                GlassmorphicButton(
                    text = "+45m CODING",
                    onClick = { onAction(AnalyticsAction.SimulateSuccessSession(45, "Coding")) },
                    icon = Icons.Default.Code,
                    modifier = Modifier.weight(1f),
                    glowAccent = NeonViolet.copy(alpha = 0.15f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassmorphicButton(
                    text = "BLOCK DISTRACTION",
                    onClick = { onAction(AnalyticsAction.SimulateDistraction("com.instagram.android", "Instagram")) },
                    icon = Icons.Default.Shield,
                    modifier = Modifier.weight(1f),
                    glowAccent = AuroraPink.copy(alpha = 0.15f)
                )
                GlassmorphicButton(
                    text = "LOG MISSED CLASS",
                    onClick = { onAction(AnalyticsAction.LogMissedSession("Calculus III", "10:00", "11:30")) },
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f),
                    glowAccent = AuroraPink.copy(alpha = 0.15f)
                )
            }
            
            OutlinedButton(
                onClick = { onAction(AnalyticsAction.ClearAllData) },
                border = BorderStroke(1.dp, AuroraPink.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AuroraPink),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RESET SYSTEM STATISTICS")
            }
        }
    }
}
