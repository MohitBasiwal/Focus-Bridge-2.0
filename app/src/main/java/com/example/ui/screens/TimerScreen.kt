package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.GlassmorphicButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    minutes: Int,
    category: String,
    onSessionCompleted: (Int) -> Unit,
    onAborted: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Elegant glowing focus target badge
            GlassmorphicCard(
                cornerRadius = 32.dp,
                glassColor = AuroraPink.copy(alpha = 0.08f),
                contentPadding = 8.dp
            ) {
                Text(
                    text = "ACTIVE BARRIER ENABLED",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AuroraPink,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Big elegant countdown display (sleek monospace font style)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:00", minutes),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = PureWhite
                )
                Text(
                    text = "Remaining in Focus Phase",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSlateMuted
                )
            }

            // Central info glass card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                glassColor = GlassBase
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = GlassMint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Your selected distraction barrier is active. Leaving this app will break the Focus Bridge.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSlateLight,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Abort action
                OutlinedButton(
                    onClick = onAborted,
                    border = BorderStroke(1.dp, GlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSlateMuted),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ABORT")
                }

                // Simulate session complete
                GlassmorphicButton(
                    text = "COMPLETE",
                    onClick = {
                        viewModel.completeSession(minutes, category)
                        onSessionCompleted(minutes)
                    },
                    icon = Icons.Default.Done,
                    modifier = Modifier.weight(1.3f),
                    glowAccent = GlassMint.copy(alpha = 0.3f)
                )
            }
        }
    }
}
