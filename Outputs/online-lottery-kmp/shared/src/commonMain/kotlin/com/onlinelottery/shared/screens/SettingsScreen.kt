package com.onlinelottery.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SectionHeader
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.theme.AppThemeMode
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.SecondaryText

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    mode: AppThemeMode,
    onModeChange: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "返回个人中心",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack)
                        .padding(9.dp)
                        .size(22.dp),
                )
                Text("设置", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp))
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("外观")
                    Text("选择应用的显示方式，修改后立即对整个应用生效", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    ThemeOption("跟随系统", "根据设备的系统外观自动切换", AppThemeMode.System, mode, onModeChange)
                    ThemeOption("浅色模式", "保持明亮、清晰的界面", AppThemeMode.Light, mode, onModeChange)
                    ThemeOption("深色模式", "夜间使用更舒适", AppThemeMode.Dark, mode, onModeChange)
                }
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    SettingsRow("通知与提醒", "管理消息和开奖提醒")
                    SettingsRow("隐私与安全", "账户保护和登录设备")
                    SettingsRow("关于好运彩", "版本 1.0.0")
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    option: AppThemeMode,
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
) {
    val isSelected = option == selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable { onSelect(option) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isSelected) BrandBlue else SecondaryText,
            modifier = Modifier.size(22.dp),
        )
        Column(Modifier.padding(start = 11.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.weight(1f))
        Text("›", color = SecondaryText, style = MaterialTheme.typography.titleLarge)
    }
}
