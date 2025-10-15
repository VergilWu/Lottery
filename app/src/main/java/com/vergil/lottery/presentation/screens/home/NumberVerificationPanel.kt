package com.vergil.lottery.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.core.constants.LotteryType
import com.vergil.lottery.domain.analyzer.NumberVerificationEngine
import com.vergil.lottery.presentation.components.BackdropLiquidGlassCard


@Composable
fun NumberVerificationPanel(
    backdrop: Backdrop,
    lotteryType: LotteryType,
    inputRedNumbers: List<String>,
    inputBlueNumbers: List<String>,
    verificationResult: NumberVerificationEngine.VerificationResult?,
    onRedNumbersChange: (List<String>) -> Unit,
    onBlueNumbersChange: (List<String>) -> Unit,
    onVerify: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        NumberInputSection(
            backdrop = backdrop,
            lotteryType = lotteryType,
            inputRedNumbers = inputRedNumbers,
            inputBlueNumbers = inputBlueNumbers,
            onRedNumbersChange = onRedNumbersChange,
            onBlueNumbersChange = onBlueNumbersChange
        )

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onVerify,
                modifier = Modifier.weight(1f),
                enabled = inputRedNumbers.isNotEmpty()
            ) {
                Text("验证中奖")
            }

            TextButton(
                onClick = onClear,
                modifier = Modifier.weight(1f)
            ) {
                Text("清空")
            }
        }


        AnimatedVisibility(
            visible = verificationResult != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            verificationResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                VerificationResultCard(backdrop = backdrop, result = result)
            }
        }
    }
}


@Composable
private fun NumberInputSection(
    backdrop: Backdrop,
    lotteryType: LotteryType,
    inputRedNumbers: List<String>,
    inputBlueNumbers: List<String>,
    onRedNumbersChange: (List<String>) -> Unit,
    onBlueNumbersChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "输入号码",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))


            var redInput by remember(inputRedNumbers) { mutableStateOf(inputRedNumbers.joinToString(" ")) }
            OutlinedTextField(
                value = redInput,
                onValueChange = { text ->
                    redInput = text
                    val numbers = text.split(" ", ",", "，", "\n")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.all { char -> char.isDigit() } }
                        .map { it.padStart(2, '0') }
                    onRedNumbersChange(numbers)
                },
                label = { Text("红球号码（用空格分隔）") },
                placeholder = { Text("例如: 03 08 15 22 27 31") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )


            if (lotteryType != LotteryType.FC3D && lotteryType != LotteryType.PL3 && lotteryType != LotteryType.PL5 && lotteryType != LotteryType.KL8) {
                Spacer(modifier = Modifier.height(12.dp))

                var blueInput by remember(inputBlueNumbers) { mutableStateOf(inputBlueNumbers.joinToString(" ")) }
                OutlinedTextField(
                    value = blueInput,
                    onValueChange = { text ->
                        blueInput = text
                        val numbers = text.split(" ", ",", "，", "\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() && it.all { char -> char.isDigit() } }
                            .map { it.padStart(2, '0') }
                        onBlueNumbersChange(numbers)
                    },
                    label = { Text("蓝球号码（用空格分隔）") },
                    placeholder = { Text("例如: 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VerificationResultCard(
    backdrop: Backdrop,
    result: NumberVerificationEngine.VerificationResult,
    modifier: Modifier = Modifier
) {
    val containerColor = if (result.isWin) {
        Color(0xFF4CAF50).copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val iconColor = if (result.isWin) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant

    BackdropLiquidGlassCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (result.isWin) "🎉 ${result.prizeLevel}" else "验证结果",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (result.isWin) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                )

                if (result.isWin) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "中奖",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )


            if (result.matchedRed.isNotEmpty() || result.matchedBlue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "匹配号码：",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    result.matchedRed.forEach { number ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    result.matchedBlue.forEach { number ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E88E5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

