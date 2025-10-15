package com.vergil.lottery.presentation.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.vergil.lottery.R
import com.vergil.lottery.presentation.components.BackdropLiquidGlassCard
import com.vergil.lottery.core.constants.ThemeMode


@Composable
fun AboutScreen(
    backdrop: Backdrop,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 72.dp + 48.dp + 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))


        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "App Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))


        Text(
            text = "彩票工具",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))


        Text(
            text = "版本 1.0.0",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))


        BackdropLiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = themeMode
        ) {
            Text(
                text = "一款专业的彩票工具软件，采用 Jetpack Compose + MVI 架构，提供开奖查询、数据分析、智能预测等功能。",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        BackdropLiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = themeMode
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "技术栈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                InfoRow(icon = Icons.Default.Code, text = "Kotlin + Jetpack Compose")
                InfoRow(icon = Icons.Default.Code, text = "MVI 架构模式")
                InfoRow(icon = Icons.Default.Code, text = "Kotlin Coroutines + Flow")
                InfoRow(icon = Icons.Default.Code, text = "TensorFlow Lite (LSTM)")
                InfoRow(icon = Icons.Default.Code, text = "Material 3 + Liquid Glass UI")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        BackdropLiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = themeMode
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "联系我们",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                InfoRow(icon = Icons.Default.Email, text = "vergilcat@gmail.com")
                InfoRow(icon = Icons.Default.Code, text = "https://github.com/VergilWu/Lottery")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        BackdropLiquidGlassCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            themeMode = themeMode
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "免责声明",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "本应用仅供学习和研究使用，不构成任何投注建议。预测结果仅供参考，不保证准确性。用户应理性对待彩票，量力而行。开发者不承担因使用本应用而产生的任何损失。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "重要声明：本应用不提供任何形式的投注服务，不参与任何赌博活动。所有数据来源于公开渠道，预测算法仅用于技术研究。用户使用本应用即表示同意承担所有风险，开发者不承担任何法律责任。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "© 2025 彩票工具. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun InfoRow(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

