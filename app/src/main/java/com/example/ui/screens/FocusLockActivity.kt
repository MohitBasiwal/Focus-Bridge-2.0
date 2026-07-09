package com.example.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Screen displayed when a blocked application or website is accessed during an active study session.
 * Lock screen is rendered with the signature premium frosted glass design theme.
 * Integrates Speech Challenge Vocal Unlock to allow 5-minute temporary bypasses.
 */
@AndroidEntryPoint
class FocusLockActivity : ComponentActivity() {
    
    private val speechViewModel: SpeechViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusBridgeTheme {
                val state by speechViewModel.uiState.collectAsState()
                var showSpeechChallenge by remember { mutableStateOf(false) }

                // Auto finish lock screen if speech unlock bypass is active
                LaunchedEffect(state.isBypassActive) {
                    if (state.isBypassActive) {
                        finish()
                    }
                }

                if (showSpeechChallenge) {
                    SpeechChallengeScreen(
                        viewModel = speechViewModel,
                        onBack = {
                            speechViewModel.onAction(SpeechAction.StopChallenge)
                            showSpeechChallenge = false
                        }
                    )
                } else {
                    LockScreenContent(
                        onExitClick = {
                            // Exit lock screen and go home
                            finishAffinity()
                        },
                        onStartSpeechUnlock = {
                            showSpeechChallenge = true
                        }
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        // Prevent dismissal via back button to maintain strict focus enforcement
        finishAffinity()
    }
}

@Composable
fun LockScreenContent(
    onExitClick: () -> Unit,
    onStartSpeechUnlock: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Atmospheric glowing background
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
                        colors = listOf(NeonViolet.copy(alpha = 0.2f), Color.Transparent)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                cornerRadius = 32.dp,
                glassColor = CosmicDark.copy(alpha = 0.85f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Shield Glow Icon
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(AuroraPink.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .border(BorderStroke(1.5.dp, AuroraPink), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = AuroraPink,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FOCUS ZONE ACTIVE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = PureWhite,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "This app is currently blocked to help you stay committed to your scheduled study session.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSlateLight,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassmorphicButton(
                            text = "TEMPORARY SPEECH UNLOCK",
                            onClick = onStartSpeechUnlock,
                            icon = Icons.Default.Mic,
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlassmorphicButton(
                            text = "RETURN TO HOME SCREEN",
                            onClick = onExitClick,
                            icon = Icons.Default.Lock,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
