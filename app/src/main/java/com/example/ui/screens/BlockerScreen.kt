package com.example.ui.screens

import android.widget.Toast
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
import com.example.data.database.entity.AllowedAppEntity
import com.example.data.database.entity.BlockedWebsiteEntity
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.GlassmorphicTextField
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerScreen(
    viewModel: BlockerViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Security puzzle interception states
    var puzzleActionName by remember { mutableStateOf<String?>(null) }
    var onPuzzleSuccess by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is BlockerEvent.ToastMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth > 600.dp

        // Cohesive atmospheric Aurora glowing mesh background
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
                            text = "DISTRACTION SHIELD",
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
                // Info Banner
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        glassColor = NeonViolet.copy(alpha = 0.08f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = NeonViolet,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "AUTOMATIC STUDY LOCK",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonViolet
                                )
                                Text(
                                    text = "All registered blocking rules apply automatically only during active scheduled study sessions. Access is fully restored when the session concludes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSlateLight
                                )
                            }
                        }
                    }
                }

                if (state.errorMessage != null) {
                    item {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            glassColor = AuroraPink.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.errorMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AuroraPink,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.onAction(BlockerAction.DismissError) }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AuroraPink)
                                }
                            }
                        }
                    }
                }

                // App/Website layout splits based on width configuration
                if (isWideScreen) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                AppBlockingSection(state, viewModel)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                WebsiteBlockingSection(state, viewModel)
                            }
                        }
                    }
                } else {
                    // Portrait/Compact stacking list
                    item {
                        AppBlockingSection(state, viewModel)
                    }
                    item {
                        WebsiteBlockingSection(state, viewModel)
                    }
                }
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
                    viewModel.onAction(BlockerAction.LogFailedPuzzle(details))
                }
            )
        }
    }
}

@Composable
fun AppBlockingSection(
    state: BlockerState,
    viewModel: BlockerViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "APP WHITELIST SYSTEM",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = AuroraCyan,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Predefined easy toggle apps list
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "QUICK TOGGLE STUDY APPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSlateMuted
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.studyAppsPreset.forEach { preset ->
                        val isAllowed = state.allowedApps.any { it.packageName == preset.packageName }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllowed) GlassMint.copy(alpha = 0.2f) else GlassBase)
                                .border(
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isAllowed) GlassMint else GlassBorder
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isAllowed) {
                                        viewModel.onAction(
                                            BlockerAction.TogglePredefinedApp(
                                                preset.name,
                                                preset.packageName
                                            )
                                        )
                                    } else {
                                        puzzleActionName = "Allow application '${preset.name}'"
                                        onPuzzleSuccess = {
                                            viewModel.onAction(
                                                BlockerAction.TogglePredefinedApp(
                                                    preset.name,
                                                    preset.packageName
                                                )
                                            )
                                            puzzleActionName = null
                                        }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAllowed) Icons.Default.Done else Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (isAllowed) GlassMint else TextSlateMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isAllowed) PureWhite else TextSlateMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom manual package app adding
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "REGISTER CUSTOM APPLICATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSlateMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GlassmorphicTextField(
                            value = state.customAppNameInput,
                            onValueChange = { viewModel.onAction(BlockerAction.CustomAppNameInputChanged(it)) },
                            placeholder = "App Name (e.g., Coursera)"
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        GlassmorphicTextField(
                            value = state.customPackageInput,
                            onValueChange = { viewModel.onAction(BlockerAction.CustomPackageInputChanged(it)) },
                            placeholder = "Package (e.g., org.coursera)"
                        )
                    }
                }

                GlassmorphicButton(
                    text = "ALLOW APPLICATION",
                    onClick = {
                        val appName = state.customAppNameInput.trim()
                        val pkgName = state.customPackageInput.trim()
                        if (appName.isEmpty() || pkgName.isEmpty()) {
                            viewModel.onAction(BlockerAction.AddCustomApp(appName, pkgName))
                        } else {
                            puzzleActionName = "Allow custom application '$appName'"
                            onPuzzleSuccess = {
                                viewModel.onAction(BlockerAction.AddCustomApp(appName, pkgName))
                                puzzleActionName = null
                            }
                        }
                    },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // List of currently allowed apps
        Text(
            text = "CURRENTLY ALLOWED APPS (${state.allowedApps.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextSlateMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (state.allowedApps.isEmpty()) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "All apps are currently blocked during study sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            state.allowedApps.forEach { app ->
                AllowedAppRow(app = app, onRemove = { viewModel.onAction(BlockerAction.RemoveAllowedApp(it)) })
            }
        }
    }
}

@Composable
fun WebsiteBlockingSection(
    state: BlockerState,
    viewModel: BlockerViewModel
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "WEBSITE BLOCKLIST SYSTEM",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = AuroraPink,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Add domain rules
        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "REGISTER WEBSITE DOMAIN RULE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSlateMuted
                )

                GlassmorphicTextField(
                    value = state.websiteDomainInput,
                    onValueChange = { viewModel.onAction(BlockerAction.WebsiteDomainInputChanged(it)) },
                    placeholder = "e.g., instagram.com"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBlock = state.websiteIsBlockedType
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBlock) AuroraPink.copy(alpha = 0.25f) else GlassBase)
                            .border(1.dp, if (isBlock) AuroraPink else GlassBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.onAction(BlockerAction.WebsiteIsBlockedTypeChanged(true)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BLACKLIST / BLOCK",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isBlock) PureWhite else TextSlateMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isBlock) GlassMint.copy(alpha = 0.25f) else GlassBase)
                            .border(1.dp, if (!isBlock) GlassMint else GlassBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.onAction(BlockerAction.WebsiteIsBlockedTypeChanged(false)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "WHITELIST / ALLOW",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isBlock) PureWhite else TextSlateMuted
                        )
                    }
                }

                GlassmorphicButton(
                    text = "REGISTER RULE",
                    onClick = { viewModel.onAction(BlockerAction.AddBlockedWebsite) },
                    icon = Icons.Default.Add,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // List of registered website domains rules
        Text(
            text = "REGISTERED DOMAIN RULES (${state.blockedWebsites.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextSlateMuted,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        if (state.blockedWebsites.isEmpty()) {
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No domain filter rules registered yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMuted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            state.blockedWebsites.forEach { website ->
                WebsiteRuleRow(
                    website = website,
                    onRemove = {
                        puzzleActionName = "Remove website rule for '${website.domain}'"
                        onPuzzleSuccess = {
                            viewModel.onAction(BlockerAction.RemoveBlockedWebsite(website))
                            puzzleActionName = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AllowedAppRow(
    app: AllowedAppEntity,
    onRemove: (AllowedAppEntity) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = GlassBase
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = GlassMint,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(text = app.appName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                    Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = TextSlateMuted)
                }
            }

            IconButton(onClick = { onRemove(app) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete rule", tint = AuroraPink)
            }
        }
    }
}

@Composable
fun WebsiteRuleRow(
    website: BlockedWebsiteEntity,
    onRemove: (BlockedWebsiteEntity) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = GlassBase
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
                    imageVector = if (website.isBlocked) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (website.isBlocked) AuroraPink else GlassMint,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(text = website.domain, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = PureWhite)
                    Text(
                        text = if (website.isBlocked) "BLACKLISTED / BLOCKED" else "WHITELISTED / ALLOWED",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (website.isBlocked) AuroraPink else GlassMint
                    )
                }
            }

            IconButton(onClick = { onRemove(website) }) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete rule", tint = AuroraPink)
            }
        }
    }
}
