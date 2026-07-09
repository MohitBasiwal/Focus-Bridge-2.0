package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SpeechChallengeScreen(
    viewModel: SpeechViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Request permission on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onAction(SpeechAction.PermissionStateChanged(isGranted))
    }

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            viewModel.onAction(SpeechAction.PermissionStateChanged(true))
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SpeechEvent.ToastMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isWideScreen = maxWidth > 600.dp

        // Ambient aurora glowing slate background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicDark)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonViolet.copy(alpha = 0.18f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AuroraPink.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "VOCAL INTEGRITY UNLOCK",
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                
                // 1. Check for Active Bypass Countdown Timer (Completed State)
                if (state.isBypassActive) {
                    item {
                        BypassTimerCard(
                            remainingMs = state.bypassRemainingMs,
                            onClearBypass = { viewModel.onAction(SpeechAction.ClearBypassNow) }
                        )
                    }
                } else {
                    // Challenge Active View
                    item {
                        ActiveChallengeCard(state = state, viewModel = viewModel)
                    }
                }

                // 2. Setup Category Selection Cards
                item {
                    CategorySetupCard(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BypassTimerCard(
    remainingMs: Long,
    onClearBypass: () -> Unit
) {
    // Format countdown
    val totalSeconds = remainingMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    // Breathing border animation for active unlocked bypass
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = CosmicDark.copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(GlassMint.copy(alpha = 0.08f), CircleShape)
                    .border(BorderStroke(2.dp, GlassMint.copy(alpha = alphaPulse)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "Bypass active",
                        tint = GlassMint,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = PureWhite
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "TEMPORARY BARRIER SUSPENSION",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = GlassMint
                )
                Text(
                    text = "Vocal integrity check cleared successfully. All blocked apps and websites are accessible. Blocking will re-engage automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateLight,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            GlassmorphicButton(
                text = "RE-ENGAGE BARRIER NOW",
                onClick = onClearBypass,
                icon = Icons.Default.Lock,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveChallengeCard(
    state: SpeechState,
    viewModel: SpeechViewModel
) {
    // Breathing scale for listening mic icon
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        glassColor = CosmicDark.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header: Category and Audio Status
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
                            .background(NeonViolet.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.currentParagraph?.category?.uppercase() ?: "SCIENCE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonViolet
                        )
                    }
                    Text(
                        text = "${state.spokenWordCount}/${state.paragraphWords.size} WORDS",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlateMuted
                    )
                }

                // Breathing Mic Icon
                if (state.isListening) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(AuroraPink, CircleShape)
                        )
                        Text(
                            text = "LISTENING",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = AuroraPink
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(TextSlateMuted, CircleShape)
                        )
                        Text(
                            text = "IDLE",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSlateMuted
                        )
                    }
                }
            }

            // Error Display
            if (state.speechError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuroraPink.copy(alpha = 0.15f))
                        .border(1.dp, AuroraPink, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.speechError,
                            style = MaterialTheme.typography.bodySmall,
                            color = AuroraPink,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.onAction(SpeechAction.DismissError) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = AuroraPink)
                        }
                    }
                }
            }

            // Paragraph Reading Area
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp),
                glassColor = CosmicDark.copy(alpha = 0.6f)
            ) {
                if (state.paragraphWords.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonViolet)
                    }
                } else {
                    // Renders the words in flow, wrapping beautifully
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.paragraphWords.forEachIndexed { index, word ->
                            val isSpoken = index < state.spokenWordCount
                            val isActiveTarget = index == state.spokenWordCount

                            Text(
                                text = word,
                                fontSize = 16.sp,
                                fontWeight = if (isSpoken || isActiveTarget) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSpoken -> GlassMint
                                    isActiveTarget -> AuroraCyan
                                    else -> TextSlateLight.copy(alpha = 0.45f)
                                },
                                modifier = if (isActiveTarget) {
                                    Modifier
                                        .border(
                                            BorderStroke(0.5.dp, AuroraCyan.copy(alpha = 0.5f)),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                } else Modifier
                            )
                        }
                    }
                }
            }

            // Silence Timer & Prompt Warning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.infoMessage ?: "Read the paragraph aloud to verify focus.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.spokenWordCount > 0) GlassMint else TextSlateMuted,
                    modifier = Modifier.weight(1f)
                )

                if (state.isListening && state.spokenWordCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Silence Alert",
                            tint = AuroraPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "RESET IN ${state.silenceRemainingSeconds}S",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                            color = AuroraPink
                        )
                    }
                }
            }

            // Control Action Button: Start/Stop Challenge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.isListening) {
                    GlassmorphicButton(
                        text = "STOP LISTENING",
                        onClick = { viewModel.onAction(SpeechAction.StopChallenge) },
                        icon = Icons.Default.MicOff,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    GlassmorphicButton(
                        text = "START MIC & TALK",
                        onClick = { viewModel.onAction(SpeechAction.StartChallenge) },
                        icon = Icons.Default.Mic,
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(
                    onClick = { viewModel.onAction(SpeechAction.RestartChallenge) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(GlassBase, RoundedCornerShape(12.dp))
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Skip/Restart paragraph",
                        tint = PureWhite
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySetupCard(
    state: SpeechState,
    viewModel: SpeechViewModel
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = AuroraCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "PARAGRAPH DOMAIN FILTER",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black),
                        color = PureWhite
                    )
                    Text(
                        text = "Select content categories for your educational challenges",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSlateMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.categories.forEach { category ->
                    val isSelected = category.isSelected
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AuroraCyan.copy(alpha = 0.2f) else GlassBase)
                            .border(
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) AuroraCyan else GlassBorder
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.onAction(SpeechAction.ToggleCategory(category.name)) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isSelected) AuroraCyan else TextSlateMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) PureWhite else TextSlateMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
