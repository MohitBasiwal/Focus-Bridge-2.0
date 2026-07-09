package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StudySession
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassmorphicTextField
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Highly stylized Timetable Screen with Premium Frosted Glass elements.
 * Features schedule list, add/edit overlay flow, and schedule conflict validation warnings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    viewModel: TimetableViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Track selected tab/day for filtered view (1 = Monday, ..., 7 = Sunday)
    var selectedFilterDay by remember { mutableStateOf(1) }

    // Security puzzle interception states
    var puzzleActionName by remember { mutableStateOf<String?>(null) }
    var onPuzzleSuccess by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is TimetableEvent.ToastMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is TimetableEvent.SaveSuccess -> {
                    // Reset or custom flow on successful schedule
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth > 600.dp

        // Ambient atmospheric glowing background
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
                        colors = listOf(NeonViolet.copy(alpha = 0.15f), Color.Transparent)
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
                        colors = listOf(AuroraCyan.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "TIMETABLE BRIDGE",
                            color = PureWhite,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
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
                    actions = {
                        // Quick Add FAB alternative in top bar
                        IconButton(onClick = { viewModel.onAction(TimetableAction.AddSessionClick) }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Session",
                                tint = GlassMint
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Day Filter Selector
                DayOfWeekSelector(
                    selectedDay = selectedFilterDay,
                    onDaySelected = { selectedFilterDay = it }
                )

                // Main Layout
                if (isWideScreen) {
                    // Wide/Tablet Split View
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Filtered Study sessions column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "SCHEDULE FOR " + getDayName(selectedFilterDay).uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = AuroraCyan,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            FilteredSessionsList(
                                sessions = state.studySessions,
                                filterDay = selectedFilterDay,
                                onEdit = { viewModel.onAction(TimetableAction.EditSessionClick(it)) },
                                onDelete = { session ->
                                    puzzleActionName = "Delete study slot '${session.subjectName}'"
                                    onPuzzleSuccess = {
                                        viewModel.onAction(TimetableAction.DeleteSessionClick(session))
                                        puzzleActionName = null
                                    }
                                }
                            )
                        }

                        // Analytics/Summary Card
                        Column(
                            modifier = Modifier
                                .weight(0.8f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        text = "TIMETABLE COVERAGE",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = NeonViolet
                                    )
                                    val totalScheduled = state.studySessions.size
                                    val totalDuration = state.studySessions.sumOf { s ->
                                        // Count active days * duration
                                        s.activeDaysSet.size * (s.endTimeMinutes - s.startTimeMinutes)
                                    }
                                    
                                    CoverageRow(label = "Total Active Slots", value = "$totalScheduled classes")
                                    CoverageRow(label = "Weekly Study Hours", value = String.format("%.1f hrs", totalDuration / 60.0))
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    GlassmorphicButton(
                                        text = "SCHEDULE NEW CLASS",
                                        onClick = { viewModel.onAction(TimetableAction.AddSessionClick) },
                                        icon = Icons.Default.Add,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Compact Phone Column View
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SCHEDULED CLASSES",
                            style = MaterialTheme.typography.labelLarge,
                            color = AuroraCyan,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        FilteredSessionsList(
                            sessions = state.studySessions,
                            filterDay = selectedFilterDay,
                            onEdit = { viewModel.onAction(TimetableAction.EditSessionClick(it)) },
                            onDelete = { session ->
                                puzzleActionName = "Delete study slot '${session.subjectName}'"
                                onPuzzleSuccess = {
                                    viewModel.onAction(TimetableAction.DeleteSessionClick(session))
                                    puzzleActionName = null
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add/Edit Session overlay modal with frosted glass layout
        if (state.isEditingSession) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(enabled = true, onClick = { /* consume click to prevent dim dismiss */ }),
                contentAlignment = Alignment.Center
            ) {
                EditSessionGlassCard(
                    state = state,
                    onAction = { action ->
                        if (action is TimetableAction.SaveSessionClick) {
                            puzzleActionName = if (state.selectedSessionId == null) {
                                "Schedule class '${state.subjectNameInput}'"
                            } else {
                                "Modify class schedule"
                            }
                            onPuzzleSuccess = {
                                viewModel.onAction(TimetableAction.SaveSessionClick)
                                puzzleActionName = null
                            }
                        } else {
                            viewModel.onAction(action)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(if (isWideScreen) 0.6f else 0.92f)
                        .padding(16.dp)
                )
            }
        }

        // Dynamic puzzle verification overlay
        puzzleActionName?.let { action ->
            com.example.ui.components.PuzzleVerificationDialog(
                difficulty = state.puzzleDifficulty,
                actionName = action,
                onSuccess = {
                    onPuzzleSuccess?.invoke()
                },
                onDismiss = {
                    puzzleActionName = null
                },
                onFailureLog = { details ->
                    viewModel.onAction(TimetableAction.LogFailedPuzzle(details))
                }
            )
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items((1..7).toList()) { day ->
            val isSelected = day == selectedDay
            val name = getDayName(day).substring(0, 3)

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else GlassBase)
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) NeonViolet else GlassBorder
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onDaySelected(day) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) PureWhite else TextSlateMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AuroraCyan else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun FilteredSessionsList(
    sessions: List<StudySession>,
    filterDay: Int,
    onEdit: (StudySession) -> Unit,
    onDelete: (StudySession) -> Unit
) {
    val filtered = sessions.filter { it.activeDaysSet.contains(filterDay) }

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = TextSlateDark,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No study classes scheduled for this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMuted
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { session ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    glassColor = GlassBase
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = session.subjectName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = PureWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Done, // Simulating a schedule icon
                                    contentDescription = null,
                                    tint = AuroraCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${session.startTime} - ${session.endTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlateLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Repeat Mode indicator badge
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = when (session.repeatType) {
                                            "Daily" -> GlassMint.copy(alpha = 0.15f)
                                            "Weekly" -> NeonViolet.copy(alpha = 0.15f)
                                            "Custom" -> AuroraCyan.copy(alpha = 0.15f)
                                            else -> TextSlateDark.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = session.repeatType.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = when (session.repeatType) {
                                        "Daily" -> GlassMint
                                        "Weekly" -> NeonViolet
                                        "Custom" -> AuroraCyan
                                        else -> TextSlateMuted
                                    }
                                )
                            }
                        }

                        // Actions
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onEdit(session) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit class",
                                    tint = TextSlateMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = { onDelete(session) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete class",
                                    tint = AuroraPink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditSessionGlassCard(
    state: TimetableState,
    onAction: (TimetableAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectLocal by remember { mutableStateOf(state.subjectNameInput) }
    var startLocal by remember { mutableStateOf(state.startTimeInput) }
    var endLocal by remember { mutableStateOf(state.endTimeInput) }
    var repeatLocal by remember { mutableStateOf(state.repeatTypeInput) }

    GlassmorphicCard(
        modifier = modifier,
        cornerRadius = 24.dp,
        glassColor = CosmicDark.copy(alpha = 0.9f) // High-density glass contrast
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (state.selectedSessionId == null) "SCHEDULE CLASS" else "EDIT SCHEDULE",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = PureWhite
            )

            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuroraPink.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, AuroraPink, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AuroraPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = state.errorMessage, style = MaterialTheme.typography.bodySmall, color = AuroraPink, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onAction(TimetableAction.DismissError) }, modifier = Modifier.size(16.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = AuroraPink)
                        }
                    }
                }
            }

            // Subject name field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "SUBJECT NAME", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                GlassmorphicTextField(
                    value = subjectLocal,
                    onValueChange = {
                        subjectLocal = it
                        onAction(TimetableAction.SubjectNameChanged(it))
                    },
                    placeholder = "e.g., Computer Science"
                )
            }

            // Time selectors (Hours & Minutes slider/pickers for friction-free touch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time Adjustment
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "START TIME", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                    TimeAdjustmentWidget(
                        timeStr = startLocal,
                        onTimeChanged = {
                            startLocal = it
                            onAction(TimetableAction.StartTimeChanged(it))
                        }
                    )
                }

                // End Time Adjustment
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "END TIME", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                    TimeAdjustmentWidget(
                        timeStr = endLocal,
                        onTimeChanged = {
                            endLocal = it
                            onAction(TimetableAction.EndTimeChanged(it))
                        }
                    )
                }
            }

            // Repeat Configuration selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "REPEAT PATTERN", style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val patterns = listOf("None", "Daily", "Weekly", "Custom")
                    patterns.forEach { pattern ->
                        val isSelected = pattern == repeatLocal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.25f) else GlassBase)
                                .border(1.dp, if (isSelected) NeonViolet else GlassBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    repeatLocal = pattern
                                    onAction(TimetableAction.RepeatTypeChanged(pattern))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = pattern,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) PureWhite else TextSlateMuted
                            )
                        }
                    }
                }
            }

            // Custom Days selection grid (shown if repeat mode is Custom, or to assign the single active day for None/Weekly)
            if (repeatLocal == "Custom" || repeatLocal == "None" || repeatLocal == "Weekly") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val label = when (repeatLocal) {
                        "Custom" -> "ACTIVE DAYS (SELECT MULTIPLE)"
                        "Weekly" -> "REPEATING DAY OF WEEK"
                        else -> "SCHEDULE FOR WHICH DAY"
                    }
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSlateMuted)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..7).forEach { day ->
                            val isSelected = state.customDaysInput.contains(day)
                            val initial = getDayName(day).substring(0, 1)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AuroraCyan.copy(alpha = 0.3f) else GlassBase)
                                    .border(1.dp, if (isSelected) AuroraCyan else GlassBorder, CircleShape)
                                    .clickable {
                                        if (repeatLocal == "None" || repeatLocal == "Weekly") {
                                            // Only single day selection allowed
                                            onAction(TimetableAction.RepeatTypeChanged(repeatLocal))
                                            onAction(TimetableAction.CustomDayToggled(day))
                                        } else {
                                            onAction(TimetableAction.CustomDayToggled(day))
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) PureWhite else TextSlateMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onAction(TimetableAction.CancelEditClick) },
                    border = BorderStroke(1.dp, GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSlateMuted),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("CANCEL")
                }

                GlassmorphicButton(
                    text = if (state.isSaving) "SAVING..." else "CONFIRM",
                    onClick = { onAction(TimetableAction.SaveSessionClick) },
                    modifier = Modifier.weight(1.3f),
                    enabled = !state.isSaving
                )
            }
        }
    }
}

/**
 * Custom tactile interactive time-adjustment widget.
 * Avoids default system dialog pops to maintain beautiful Glassmorphic context.
 */
@Composable
fun TimeAdjustmentWidget(
    timeStr: String,
    onTimeChanged: (String) -> Unit
) {
    val parts = timeStr.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GlassBase)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Decrease Hours
        IconButton(
            onClick = {
                val newHour = if (hour == 0) 23 else hour - 1
                onTimeChanged(String.format("%02d:%02d", newHour, minute))
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextSlateMuted)
        }

        // Display
        Text(
            text = timeStr,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = PureWhite
        )

        // Increase Hours
        IconButton(
            onClick = {
                val newHour = (hour + 1) % 24
                onTimeChanged(String.format("%02d:%02d", newHour, minute))
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = null, tint = TextSlateMuted)
        }
    }
}

@Composable
fun CoverageRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSlateMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
    }
}

// Helpers
fun getDayName(day: Int): String {
    return when (day) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "Monday"
    }
}
