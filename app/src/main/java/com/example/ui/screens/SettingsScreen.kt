package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs by viewModel.userPreferences.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Backup import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SettingsUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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
                    title = { Text("SETTINGS", color = PureWhite, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PureWhite
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
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
                // Theme Customization Card
                item {
                    SettingsSectionHeader(title = "VISUAL IDENTITY & THEME")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Dark Mode Toggle
                            SettingsSwitchRow(
                                name = "Dark Mode",
                                description = "Maintain a high-contrast eye-safe dark slate theme",
                                checked = prefs.darkModeEnabled,
                                onCheckedChange = { viewModel.updateDarkMode(it) },
                                icon = Icons.Default.Brightness4
                            )

                            // Dynamic Color
                            SettingsSwitchRow(
                                name = "Dynamic Color",
                                description = "Inject Android 12+ wallpaper material accents",
                                checked = prefs.dynamicColorEnabled,
                                onCheckedChange = { viewModel.updateDynamicColor(it) },
                                icon = Icons.Default.Palette
                            )

                            // Theme preset selection
                            Text(
                                text = "Color Presets",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSlateLight
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ThemePresetButton(
                                    name = "Glassmorphic Dark",
                                    selected = prefs.selectedTheme == "GlassmorphicDark",
                                    color = NeonViolet,
                                    onClick = { viewModel.updateSelectedTheme("GlassmorphicDark") },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemePresetButton(
                                    name = "Aurora Midnight",
                                    selected = prefs.selectedTheme == "AuroraMidnight",
                                    color = AuroraCyan,
                                    onClick = { viewModel.updateSelectedTheme("AuroraMidnight") },
                                    modifier = Modifier.weight(1f)
                                )
                                ThemePresetButton(
                                    name = "Solar Gold",
                                    selected = prefs.selectedTheme == "SolarGold",
                                    color = Color(0xFFFBC02D),
                                    onClick = { viewModel.updateSelectedTheme("SolarGold") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Educational Paragraph Preference Card
                item {
                    SettingsSectionHeader(title = "LEARNING CHALLENGES")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Paragraph Challenge Category",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                            Text(
                                text = "Choose the topics presented during speech challenge unlock prompts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSlateMuted
                            )

                            // Category chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Motivation", "Science", "History", "Discipline").forEach { cat ->
                                    val isSelected = prefs.paragraphCategory == cat
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateParagraphCategory(cat) },
                                        label = { Text(cat, color = if (isSelected) PureWhite else TextSlateLight) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonViolet.copy(alpha = 0.4f),
                                            containerColor = Color.Transparent
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isSelected) NeonViolet else TransparentBorder,
                                            selectedBorderColor = NeonViolet,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 1.dp
                                        )
                                    )
                                }
                            }

                            // Speech challenge duration slider
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Speech Reading Duration: ${prefs.speechDurationSeconds} seconds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PureWhite
                            )
                            Slider(
                                value = prefs.speechDurationSeconds.toFloat(),
                                onValueChange = { viewModel.updateSpeechDuration(it.toInt()) },
                                valueRange = 10f..60f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonViolet,
                                    activeTrackColor = NeonViolet,
                                    inactiveTrackColor = GlassBorder
                                )
                            )
                        }
                    }
                }

                // Notifications Preferences Card
                item {
                    SettingsSectionHeader(title = "NOTIFICATION PREFERENCES")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SettingsSwitchRow(
                                name = "Study Reminders",
                                description = "Get gentle warnings before your class or focus sessions",
                                checked = prefs.remindersEnabled,
                                onCheckedChange = { viewModel.updateReminders(it) },
                                icon = Icons.Default.NotificationsActive
                            )

                            SettingsSwitchRow(
                                name = "Session Start & End Alerts",
                                description = "Confirm when focus blockades launch or disconnect",
                                checked = prefs.sessionStartEndNotifEnabled,
                                onCheckedChange = { viewModel.updateSessionStartEndNotif(it) },
                                icon = Icons.Default.PlayCircleFilled
                            )

                            SettingsSwitchRow(
                                name = "Missed Study Reminders",
                                description = "Trigger alert if you miss scheduled study windows",
                                checked = prefs.missedRemindersEnabled,
                                onCheckedChange = { viewModel.updateMissedReminders(it) },
                                icon = Icons.Default.Warning
                            )

                            SettingsSwitchRow(
                                name = "Study Summaries",
                                description = "Receive structured daily and weekly progress analysis",
                                checked = prefs.summariesEnabled,
                                onCheckedChange = { viewModel.updateSummaries(it) },
                                icon = Icons.Default.BarChart
                            )
                        }
                    }
                }

                // Interactive Notifications Test Card (Proving capability and extremely interactive!)
                item {
                    SettingsSectionHeader(title = "TEST NOTIFICATIONS (LIVE DEMO)")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Trigger local notifications on demand to test systems instantly:",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSlateLight
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.testPreSessionReminder() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, NeonViolet)
                                ) {
                                    Text("Pre-Reminder", color = PureWhite)
                                }

                                Button(
                                    onClick = { viewModel.testSessionStart() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AuroraCyan.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, AuroraCyan)
                                ) {
                                    Text("Session Start", color = PureWhite)
                                }

                                Button(
                                    onClick = { viewModel.testSessionEnd(true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GlassMint.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, GlassMint)
                                ) {
                                    Text("Session Pass", color = PureWhite)
                                }

                                Button(
                                    onClick = { viewModel.testMissedSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AuroraPink.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, AuroraPink)
                                ) {
                                    Text("Missed Alert", color = PureWhite)
                                }

                                Button(
                                    onClick = { viewModel.testDailySummary() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LightGrey.copy(alpha = 0.2f)),
                                    border = BorderStroke(1.dp, LightGrey)
                                ) {
                                    Text("Daily Summary", color = PureWhite)
                                }

                                Button(
                                    onClick = { viewModel.testWeeklySummary() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicMuted.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.3f))
                                ) {
                                    Text("Weekly Summary", color = PureWhite)
                                }
                            }
                        }
                    }
                }

                // Backup & Restore Card
                item {
                    SettingsSectionHeader(title = "SECURE BACKUP & RESTORE")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Data Synchronization",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = PureWhite
                            )
                            Text(
                                text = "Export all local data (timetable, allowed apps, website rules, speech categories, analytics) to a single JSON file. You can restore this file anytime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSlateMuted
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Export
                                Button(
                                    onClick = {
                                        viewModel.exportBackup { json ->
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, json)
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, "Export Focus Bridge Backup")
                                            context.startActivity(shareIntent)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = PureWhite)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export JSON", color = PureWhite)
                                }

                                // Import
                                Button(
                                    onClick = { importLauncher.launch("application/json") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AuroraCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = PureWhite)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import Backup", color = PureWhite)
                                }
                            }
                        }
                    }
                }

                // Legal and Info Section
                item {
                    SettingsSectionHeader(title = "INFORMATION & POLICIES")
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAboutDialog = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AuroraCyan)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("About Focus Bridge", style = MaterialTheme.typography.bodyLarge, color = PureWhite)
                                }
                                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextSlateMuted, modifier = Modifier.size(14.dp))
                            }

                            Divider(color = GlassBorder)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showPrivacyDialog = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = GlassMint)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Privacy & Security Policy", style = MaterialTheme.typography.bodyLarge, color = PureWhite)
                                }
                                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextSlateMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- About Dialog ---
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = NeonViolet)
                }
            },
            title = { Text("About Focus Bridge", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Focus Bridge is a premium offline study assistant designed to build strong cognitive barriers between you and distracting digital environments.\n\n" +
                            "Version: 1.0.0\n" +
                            "Engine: Jetpack Compose, Material 3 Glassmorphism tokens, and Room Database architecture.\n\n" +
                            "Created as a production-grade, highly performant Android tool to assist in deep scholastic focus.",
                    color = TextSlateLight
                )
            },
            containerColor = CosmicDark,
            tonalElevation = 6.dp
        )
    }

    // --- Privacy Dialog ---
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Understand", color = NeonViolet)
                }
            },
            title = { Text("Privacy Policy", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Focus Bridge respects your privacy completely:\n\n" +
                            "• All data, timetable configurations, rules, and analytics are stored 100% locally on your device.\n" +
                            "• No personal information is gathered, tracked, or sent to external servers.\n" +
                            "• The accessibility service is solely used locally to block distracting applications and web domains during your study sessions.\n\n" +
                            "You are in total, sovereign control of your study analytics and parameters.",
                    color = TextSlateLight
                )
            },
            containerColor = CosmicDark,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.5.sp),
        color = NeonViolet,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    name: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonViolet,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureWhite,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureWhite,
                checkedTrackColor = NeonViolet,
                uncheckedThumbColor = TextSlateLight,
                uncheckedTrackColor = CosmicMuted
            )
        )
    }
}

@Composable
fun ThemePresetButton(
    name: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (selected) color.copy(alpha = 0.25f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) color else GlassBorder
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) PureWhite else TextSlateLight,
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
