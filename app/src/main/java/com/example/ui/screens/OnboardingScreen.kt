package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Cohesive atmospheric Aurora Glowing Mesh background layers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicDark)
        )
        // Top-left Violet Mesh glow
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        // Bottom-right Cyan Mesh glow
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header / Step progress bar
            OnboardingHeader(currentStep = currentStep)

            // Dynamic Step Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "step_transition"
                ) { step ->
                    when (step) {
                        0 -> WelcomeStep()
                        1 -> HowItWorksStep()
                        2 -> PermissionsStep()
                        3 -> TimetableStep(viewModel)
                        4 -> AppsStep(viewModel)
                        5 -> WebsitesStep(viewModel)
                        6 -> CategoriesStep(viewModel)
                        7 -> SecurityStep(viewModel)
                        8 -> CompletionStep(viewModel, onComplete)
                    }
                }
            }

            // Navigation Controls Footer
            if (currentStep < 8) {
                OnboardingFooter(
                    currentStep = currentStep,
                    onNext = { viewModel.nextStep() },
                    onPrev = { viewModel.previousStep() }
                )
            }
        }
    }
}

@Composable
fun OnboardingHeader(currentStep: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FOCUS BRIDGE",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                color = AuroraCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "STEP ${currentStep + 1} OF 9",
                style = MaterialTheme.typography.labelSmall,
                color = TextSlateMuted
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0..8) {
                val isCompleted = i <= currentStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (isCompleted) NeonViolet else CosmicMuted,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun OnboardingFooter(
    currentStep: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentStep > 0) {
            Button(
                onClick = onPrev,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureWhite)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back", color = PureWhite)
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next", color = PureWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PureWhite)
        }
    }
}

// --- Step 0: Welcome Screen ---
@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AllInclusive,
            contentDescription = null,
            tint = NeonViolet,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = "Welcome to Focus Bridge",
            style = MaterialTheme.typography.headlineLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "An ultra-premium, cognitive study guard designed to eliminate digital distractions and build deep-work habits.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSlateLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BulletPoint(icon = Icons.Default.Timer, text = "Schedule strict class & study sessions")
                BulletPoint(icon = Icons.Default.Block, text = "Block non-educational apps & websites")
                BulletPoint(icon = Icons.Default.Mic, text = "Bypass locks via highly engaging Speech Challenges")
                BulletPoint(icon = Icons.Default.Security, text = "100% offline local privacy database protection")
            }
        }
    }
}

// --- Step 1: How It Works ---
@Composable
fun HowItWorksStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "How Focus Bridge Works",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExplanationRow(
                    stepNumber = "1",
                    title = "Set Your Timetable",
                    description = "Input your study schedules. Focus Bridge starts blocking operations automatically when a session begins."
                )
                ExplanationRow(
                    stepNumber = "2",
                    title = "Strict Lockout Enforced",
                    description = "When locked, trying to open social media, games, or fun websites displays our blocker barrier."
                )
                ExplanationRow(
                    stepNumber = "3",
                    title = "The Speech Unlock Bypass",
                    description = "Need to open an app urgently? Read our assigned learning paragraph aloud cleanly to earn a 5-minute temporary bypass."
                )
            }
        }
    }
}

// --- Step 2: Permissions Guide ---
@Composable
fun PermissionsStep() {
    var isAccessibilityGranted by remember { mutableStateOf(false) }
    var isNotificationGranted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Grant System Access",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Focus Bridge requires these permissions to run blocker blockades and post alerts locally.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSlateLight,
            textAlign = TextAlign.Center
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PermissionToggleRow(
                    name = "Accessibility Blocker Service",
                    description = "Enables app & website blocking mechanics locally.",
                    granted = isAccessibilityGranted,
                    onGrant = { isAccessibilityGranted = true }
                )
                PermissionToggleRow(
                    name = "Push Notifications",
                    description = "Sends start alerts, pre-reminders, and summary cards.",
                    granted = isNotificationGranted,
                    onGrant = { isNotificationGranted = true }
                )
            }
        }
    }
}

// --- Step 3: Timetable Setup ---
@Composable
fun TimetableStep(viewModel: OnboardingViewModel) {
    val name by viewModel.subjectName.collectAsState()
    val start by viewModel.startTime.collectAsState()
    val end by viewModel.endTime.collectAsState()
    val repeatSet by viewModel.repeatDays.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "First Study Session",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Subject/Class Name", color = PureWhite, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.subjectName.value = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start Time", color = PureWhite, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = start,
                            onValueChange = { viewModel.startTime.value = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonViolet,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End Time", color = PureWhite, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = end,
                            onValueChange = { viewModel.endTime.value = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = PureWhite,
                                unfocusedTextColor = PureWhite,
                                focusedBorderColor = NeonViolet,
                                unfocusedBorderColor = GlassBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text("Repeat Days", color = PureWhite, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        val isSelected = repeatSet.contains(day)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .background(
                                    if (isSelected) NeonViolet else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) NeonViolet else GlassBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val current = repeatSet.toMutableSet()
                                    if (current.contains(day)) current.remove(day) else current.add(day)
                                    viewModel.repeatDays.value = current
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.take(1),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) PureWhite else TextSlateLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Step 4: Allowed Apps ---
@Composable
fun AppsStep(viewModel: OnboardingViewModel) {
    val selectedApps by viewModel.allowedApps.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Whitelisted Applications",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select study-friendly programs allowed to pass blockades during active study sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSlateLight,
            textAlign = TextAlign.Center
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availableApps) { app ->
                    val isChecked = selectedApps.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleAllowedApp(app.packageName) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = if (isChecked) NeonViolet else TextSlateMuted)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(app.name, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(app.packageName, color = TextSlateMuted, fontSize = 11.sp)
                            }
                        }
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { viewModel.toggleAllowedApp(app.packageName) },
                            colors = CheckboxDefaults.colors(checkedColor = NeonViolet, uncheckedColor = TextSlateMuted)
                        )
                    }
                }
            }
        }
    }
}

// --- Step 5: Website Blockade rules ---
@Composable
fun WebsitesStep(viewModel: OnboardingViewModel) {
    val blockedWebs by viewModel.blockedWebsites.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Restricted Websites",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Determine web filters automatically guarded against opening while focus runs.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSlateLight,
            textAlign = TextAlign.Center
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.availableWebsites) { web ->
                    val isBlocked = blockedWebs.contains(web.domain)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleBlockedWebsite(web.domain) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = if (isBlocked) AuroraPink else TextSlateMuted)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(web.name, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(web.domain, color = TextSlateMuted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isBlocked,
                            onCheckedChange = { viewModel.toggleBlockedWebsite(web.domain) },
                            colors = SwitchDefaults.colors(checkedTrackColor = AuroraPink)
                        )
                    }
                }
            }
        }
    }
}

// --- Step 6: Learning Categories ---
@Composable
fun CategoriesStep(viewModel: OnboardingViewModel) {
    val selectedCats by viewModel.selectedCategories.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Learning Categories",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Choose study challenge categories loaded during lock overrides.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSlateLight,
            textAlign = TextAlign.Center
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                viewModel.availableCategories.forEach { cat ->
                    val isSelected = selectedCats.contains(cat)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleCategory(cat) }
                            .border(1.dp, if (isSelected) NeonViolet else GlassBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat, color = PureWhite, fontWeight = FontWeight.SemiBold)
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonViolet)
                        }
                    }
                }
            }
        }
    }
}

// --- Step 7: Security Setup ---
@Composable
fun SecurityStep(viewModel: OnboardingViewModel) {
    val enabled by viewModel.isPasscodeEnabled.collectAsState()
    val code by viewModel.passcode.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Security Protection",
            style = MaterialTheme.typography.headlineMedium,
            color = PureWhite,
            fontWeight = FontWeight.Bold
        )

        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Settings Passcode", color = PureWhite, fontWeight = FontWeight.SemiBold)
                        Text("Locks changes to settings, ensuring strict study rules are maintained.", color = TextSlateMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { viewModel.isPasscodeEnabled.value = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = NeonViolet)
                    )
                }

                if (enabled) {
                    Text("Settings Passcode (4-digit)", color = PureWhite, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = code,
                        onValueChange = { if (it.length <= 4) viewModel.passcode.value = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = NeonViolet,
                            unfocusedBorderColor = GlassBorder
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

// --- Step 8: Completion Screen ---
@Composable
fun CompletionStep(viewModel: OnboardingViewModel, onComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GlassMint,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Focus Bridge is Armed!",
            style = MaterialTheme.typography.headlineLarge,
            color = PureWhite,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Setup is complete. You have successfully structured your first class schedule, configured whitelists, website restricted rules, and prepared speech categories.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSlateLight,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(
            onClick = { viewModel.saveAndCompleteOnboarding(onComplete) },
            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
        ) {
            Text("LAUNCH FOCUS BRIDGE", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun BulletPoint(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = PureWhite)
    }
}

@Composable
fun ExplanationRow(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(NeonViolet.copy(alpha = 0.3f), shape = RoundedCornerShape(6.dp))
                .border(1.dp, NeonViolet, shape = RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(stepNumber, color = PureWhite, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = PureWhite, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSlateMuted)
        }
    }
}

@Composable
fun PermissionToggleRow(
    name: String,
    description: String,
    granted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = PureWhite, fontWeight = FontWeight.SemiBold)
            Text(description, color = TextSlateMuted, fontSize = 11.sp)
        }
        Button(
            onClick = onGrant,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (granted) GlassMint.copy(alpha = 0.3f) else NeonViolet
            ),
            border = if (granted) BorderStroke(1.dp, GlassMint) else null,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (granted) "Granted" else "Grant", color = PureWhite)
        }
    }
}
