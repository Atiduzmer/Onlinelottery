package com.onlinelottery.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.LotteryGame
import com.onlinelottery.shared.model.LotteryKind
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.BrandBlueSoft
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.SecondaryText

private data class NumberArea(
    val title: String,
    val hint: String,
    val numbers: IntRange,
    val required: Int,
    val color: Color,
)

private data class NumberLotteryConfig(
    val issue: String,
    val deadline: String,
    val rule: String,
    val areas: List<NumberArea>,
)

@Composable
fun NumberLotteryDetailScreen(
    game: LotteryGame,
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    onBack: () -> Unit,
    openResults: (() -> Unit)? = null,
) {
    val config = remember(game.kind) { numberLotteryConfig(game.kind) }
    var selections by remember(game.kind) {
        mutableStateOf(config.areas.map { emptySet<Int>() })
    }
    var quickPickSeed by remember(game.kind) { mutableIntStateOf(1) }
    val complete = config.areas.indices.all { index ->
        selections[index].size == config.areas[index].required
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DetailHeader(
                    title = game.name,
                    onBack = onBack,
                    actionLabel = if (game.kind == LotteryKind.SuperLotto || game.kind == LotteryKind.SevenStar || game.kind == LotteryKind.Pick3 || game.kind == LotteryKind.Pick5) "开奖详情" else "玩法说明",
                    onAction = if (game.kind == LotteryKind.SuperLotto || game.kind == LotteryKind.SevenStar || game.kind == LotteryKind.Pick3 || game.kind == LotteryKind.Pick5) {
                        openResults ?: { notify("开奖详情正在准备中") }
                    } else {
                        { notify("${game.name}玩法说明") }
                    },
                )
            }

            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BrandBlue)
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(config.issue, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.weight(1f))
                            Text("每注 2 元", color = Color.White.copy(alpha = 0.82f))
                        }
                        Text("销售截止 ${config.deadline}", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        Text(config.rule, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF5E3))
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE79A18), modifier = Modifier.size(19.dp))
                    Text(
                        "模拟选号，仅用于界面演示，不会产生真实交易",
                        color = Color(0xFF8C6500),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }

            config.areas.forEachIndexed { areaIndex, area ->
                item {
                    NumberAreaCard(
                        area = area,
                        selected = selections[areaIndex],
                    ) { number ->
                        val current = selections[areaIndex]
                        val updated = when {
                            number in current -> current - number
                            current.size < area.required -> current + number
                            area.required == 1 -> setOf(number)
                            else -> {
                                notify("${area.title}最多选择 ${area.required} 个号码")
                                current
                            }
                        }
                        selections = selections.mapIndexed { index, set ->
                            if (index == areaIndex) updated else set
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                onClick = { selections = config.areas.map { emptySet() } },
                modifier = Modifier.background(BrandBlueSoft, CircleShape),
            ) {
                Icon(Icons.Default.Clear, contentDescription = "清空号码", tint = BrandBlue)
            }
            IconButton(
                onClick = {
                    quickPickSeed += 1
                    selections = config.areas.mapIndexed { areaIndex, area ->
                        val source = area.numbers.toList()
                        val offset = (quickPickSeed * 5 + areaIndex * 7) % source.size
                        List(area.required) { index -> source[(offset + index * 3) % source.size] }.toSet()
                    }
                    notify("已生成一注${game.name}号码")
                },
                modifier = Modifier.background(BrandBlueSoft, CircleShape),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "机选一注", tint = BrandBlue)
            }
            Column(Modifier.weight(1f)) {
                Text(
                    if (complete) "已选 1 注" else selectionProgress(config, selections),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(if (complete) "合计 2 元" else "请按规则完成选号", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = {
                    if (complete) {
                        notify("${game.name}模拟投注已加入待确认")
                    } else {
                        notify("请先完成${game.name}选号")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (complete) AlertRed else BrandBlue),
                modifier = Modifier.height(48.dp),
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(19.dp))
                Text("确认选号", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

@Composable
private fun DetailHeader(
    title: String,
    onBack: () -> Unit,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回大厅")
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.weight(1f))
        Text(
            actionLabel,
            color = BrandBlue,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(10.dp),
        )
    }
}

@Composable
private fun NumberAreaCard(
    area: NumberArea,
    selected: Set<Int>,
    onNumberClick: (Int) -> Unit,
) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(area.title, style = MaterialTheme.typography.titleMedium)
                Text("  ${area.hint}", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                if (selected.size == area.required) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(19.dp))
                }
                Text("${selected.size}/${area.required}", color = if (selected.size == area.required) LiveGreen else SecondaryText)
            }

            val chunkSize = if (area.numbers.last <= 10) 10 else 7
            val ballSize = if (chunkSize == 10) 30.dp else 36.dp
            area.numbers.toList().chunked(chunkSize).forEach { rowNumbers ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    rowNumbers.forEach { number ->
                        val isSelected = number in selected
                        Box(
                            modifier = Modifier
                                .size(ballSize)
                                .clip(CircleShape)
                                .background(if (isSelected) area.color else Color(0xFFF1F3F6))
                                .clickable { onNumberClick(number) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (area.numbers.last > 10) number.toString().padStart(2, '0') else number.toString(),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    repeat(chunkSize - rowNumbers.size) {
                        Spacer(Modifier.size(ballSize))
                    }
                }
            }
        }
    }
}

private fun numberLotteryConfig(kind: LotteryKind): NumberLotteryConfig = when (kind) {
    LotteryKind.SuperLotto -> NumberLotteryConfig(
        issue = "第 26088 期",
        deadline = "今天 20:45",
        rule = "前区选择 5 个号码，后区选择 2 个号码",
        areas = listOf(
            NumberArea("前区", "从 01-35 中选 5 个", 1..35, 5, AlertRed),
            NumberArea("后区", "从 01-12 中选 2 个", 1..12, 2, BrandBlue),
        ),
    )

    LotteryKind.Pick3 -> positionConfig("第 26203 期", "今天 20:50", listOf("百位", "十位", "个位"))
    LotteryKind.Pick5 -> positionConfig("第 26203 期", "今天 20:50", listOf("万位", "千位", "百位", "十位", "个位"))
    LotteryKind.SevenStar -> positionConfig("第 26088 期", "今天 20:30", listOf("第一位", "第二位", "第三位", "第四位", "第五位", "第六位", "第七位"))
    LotteryKind.Football,
    LotteryKind.Basketball,
    -> positionConfig("", "", emptyList())
}

private fun positionConfig(issue: String, deadline: String, labels: List<String>) = NumberLotteryConfig(
    issue = issue,
    deadline = deadline,
    rule = "每一位从 0-9 中选择 1 个号码",
    areas = labels.mapIndexed { index, label ->
        NumberArea(label, "选择 1 个数字", 0..9, 1, if (index % 2 == 0) AlertRed else BrandBlue)
    },
)

private fun selectionProgress(config: NumberLotteryConfig, selections: List<Set<Int>>): String {
    val selected = selections.sumOf { it.size }
    val required = config.areas.sumOf { it.required }
    return "已选 $selected/$required 个"
}
