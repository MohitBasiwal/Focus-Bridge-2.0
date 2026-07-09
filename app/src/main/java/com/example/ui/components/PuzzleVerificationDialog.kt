package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import kotlin.random.Random

data class Puzzle(
    val expression: String,
    val answer: Int
)

fun generatePuzzle(difficulty: String): Puzzle {
    return when (difficulty.uppercase()) {
        "EASY" -> {
            val a = Random.nextInt(5, 25)
            val b = Random.nextInt(5, 25)
            val isPlus = Random.nextBoolean()
            if (isPlus) {
                Puzzle("$a + $b", a + b)
            } else {
                val max = maxOf(a, b)
                val min = minOf(a, b)
                Puzzle("$max - $min", max - min)
            }
        }
        "HARD" -> {
            val a = Random.nextInt(5, 15)
            val b = Random.nextInt(4, 12)
            val c = Random.nextInt(5, 15)
            val d = Random.nextInt(3, 10)
            Puzzle("($a × $b) + ($c × $d)", (a * b) + (c * d))
        }
        else -> { // MEDIUM
            val a = Random.nextInt(4, 13)
            val b = Random.nextInt(4, 13)
            val c = Random.nextInt(10, 40)
            Puzzle("($a × $b) - $c", (a * b) - c)
        }
    }
}

@Composable
fun PuzzleVerificationDialog(
    difficulty: String = "MEDIUM",
    actionName: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
    onFailureLog: (String) -> Unit = {}
) {
    val puzzle = remember { mutableStateOf(generatePuzzle(difficulty)) }
    var userInput by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var attempts by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, PureWhite.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDark.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(NeonViolet.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "SECURITY AUTHENTICATION",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Solve the mind challenge to authorize:\n\"$actionName\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSlateMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Puzzle equation board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureWhite.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .border(1.dp, PureWhite.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${puzzle.value.expression} = ?",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = AuroraCyan,
                        textAlign = TextAlign.Center
                    )
                }

                OutlinedTextField(
                    value = userInput,
                    onValueChange = {
                        userInput = it
                        hasError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Your Solution", color = TextSlateLight) },
                    isError = hasError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = AuroraCyan,
                        unfocusedBorderColor = PureWhite.copy(alpha = 0.2f),
                        errorBorderColor = AuroraPink,
                        cursorColor = AuroraCyan
                    )
                )

                if (hasError) {
                    Text(
                        text = "Incorrect solution. Attempts: $attempts",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = AuroraPink,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            val parsedAnswer = userInput.trim().toIntOrNull()
                            if (parsedAnswer != null && parsedAnswer == puzzle.value.answer) {
                                onSuccess()
                            } else {
                                attempts++
                                hasError = true
                                userInput = ""
                                onFailureLog("Failed puzzle solve attempt #$attempts for action '$actionName'. Expression: ${puzzle.value.expression}")
                                // Generate a new puzzle to prevent brute forcing
                                puzzle.value = generatePuzzle(difficulty)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonViolet,
                            contentColor = PureWhite
                        )
                    ) {
                        Text("VERIFY")
                    }
                }
            }
        }
    }
}
