package com.onlinelottery.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.CalculatorMatch
import com.onlinelottery.shared.model.CalculatorSport
import com.onlinelottery.shared.model.loadCalculatorMatches
import com.onlinelottery.shared.model.placeLocalBet
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.BrandBlueSoft
import com.onlinelottery.shared.theme.CardBorder
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    initialSport: String = "足球",
    onBack: (() -> Unit)? = null,
    lockedSport: Boolean = false,
) {
    var sport by remember(initialSport) { mutableStateOf(initialSport) }
    var selectedDay by remember(sport) { mutableStateOf("") }
    var selectedOdds by remember { mutableStateOf(setOf<String>()) }
    var showBetSlip by remember { mutableStateOf(false) }
    var stake by remember { mutableIntStateOf(20) }
    var submitted by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var matchList by remember(sport) { mutableStateOf<List<CalculatorMatch>>(emptyList()) }
    var isLoading by remember(sport) { mutableStateOf(true) }
    var requestVersion by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(sport, requestVersion) {
        isLoading = true
        selectedOdds = emptySet()
        val targetSport = if (sport == "足球") CalculatorSport.Football else CalculatorSport.Basketball
        val loaded = runCatching { loadCalculatorMatches(targetSport) }.getOrNull().orEmpty()
        matchList = loaded
        if (selectedDay !in loaded.map { it.businessDate }) selectedDay = loaded.firstOrNull()?.businessDate.orEmpty()
        if (loaded.isEmpty() && requestVersion > 0) notify("赛事数据暂不可用，请稍后刷新")
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回大厅")
                        }
                    }
                    Column(Modifier.padding(start = if (onBack == null) 0.dp else 4.dp)) {
                        Text(
                            text = if (lockedSport) if (sport == "足球") "竞彩足球" else "竞彩篮球" else "比赛中心",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text("官方赛程 · SP 实时更新", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(BrandBlueSoft)
                            .clickable { requestVersion += 1 }
                            .padding(horizontal = 11.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(17.dp))
                        Text(if (isLoading) "加载中" else "刷新", color = BrandBlue, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (lockedSport) {
                        FilterPill(
                            sport,
                            true,
                            Modifier.weight(1f),
                        ) {}
                    } else {
                        FilterPill("足球", sport == "足球", Modifier.weight(1f)) { sport = "足球" }
                        FilterPill("篮球", sport == "篮球", Modifier.weight(1f)) { sport = "篮球" }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("比赛日", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    LazyRow(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(matchList.map { it.businessDate }.distinct().size) { index ->
                            val date = matchList.map { it.businessDate }.distinct()[index]
                            DayTab(date.removePrefix("2026-"), selectedDay == date) { selectedDay = date }
                        }
                    }
                }
            }

            val currentMatches = matchList.filter { selectedDay.isBlank() || it.businessDate == selectedDay }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 12.dp,
                    end = 12.dp,
                    bottom = contentPadding.calculateBottomPadding() + if (selectedOdds.isEmpty()) 18.dp else 92.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                if (isLoading) {
                    item { MatchLoadState("正在读取官方赛事与赔率…") }
                } else if (currentMatches.isEmpty()) {
                    item { MatchLoadState("暂时没有可投注赛事，点击刷新后重试") }
                }
                items(currentMatches.size) { index ->
                    val match = currentMatches[index]
                    MatchCard(
                        match = match,
                        selectedOdds = selectedOdds,
                    ) { oddsIndex ->
                        val key = "${match.id}-$oddsIndex"
                        selectedOdds = if (key in selectedOdds) selectedOdds - key else selectedOdds + key
                        submitted = false
                    }
                }
            }
        }

        if (selectedOdds.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = contentPadding.calculateBottomPadding() + 10.dp,
                    )
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.inverseSurface)
                    .clickable { showBetSlip = true }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.inverseOnSurface)
                Text(
                    "投注单 · 已选 ${selectedOdds.size} 项",
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 9.dp),
                )
                Spacer(Modifier.weight(1f))
                Text("查看投注单", color = Color(0xFFAFC2FF), style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    if (showBetSlip && selectedOdds.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = { showBetSlip = false }) {
            BetSlip(
                count = selectedOdds.size,
                stake = stake,
                submitted = submitted,
                isSubmitting = isSubmitting,
                onStakeChange = { stake = it.coerceAtLeast(0) },
                onClear = {
                    selectedOdds = emptySet()
                    showBetSlip = false
                },
                onSubmit = {
                    if (!isSubmitting) {
                        scope.launch {
                            isSubmitting = true
                            val content = selectedOdds.sorted().joinToString(
                                prefix = "{\"type\":\"sports\",\"sport\":\"$sport\",\"selections\":[",
                                postfix = "]}",
                            ) { "\"$it\"" }
                            runCatching {
                                placeLocalBet(
                                    gameCode = if (sport == "足球") "FOOTBALL" else "BASKETBALL",
                                    stakeCents = stake.toLong() * 100,
                                    content = content,
                                )
                            }.onSuccess { result ->
                                submitted = true
                                notify(result.message)
                            }.onFailure { error ->
                                notify("提交失败：${error.message ?: "本地服务不可用"}")
                            }
                            isSubmitting = false
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, if (selected) BrandBlue else CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(label, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TextTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

@Composable
private fun DayTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        textAlign = TextAlign.Center,
        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) BrandBlue else Color.Transparent)
            .border(1.dp, if (selected) BrandBlue else CardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp),
    )
}

@Composable
private fun MatchCard(
    match: CalculatorMatch,
    selectedOdds: Set<String>,
    onSelectOdd: (Int) -> Unit,
) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    match.league,
                    color = BrandBlue,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(BrandBlueSoft)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Text(match.number, color = SecondaryText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 7.dp))
                Spacer(Modifier.weight(1f))
                Text(match.time, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamName(match.home, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(78.dp)) {
                    Text(
                        if (match.status == "Selling") "VS" else match.status,
                        color = if (match.status == "Selling") MaterialTheme.colorScheme.onSurface else LiveGreen,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        if (match.status == "Selling") "销售中" else "已停售",
                        color = if (match.status == "Selling") AlertRed else SecondaryText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (match.status == "Selling") AlertRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
                TeamName(match.away, Modifier.weight(1f))
            }
            Text(match.oddsTitle + " · SP", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.padding(top = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                match.odds.forEachIndexed { index, odd ->
                    val selected = "${match.id}-$index" in selectedOdds
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (selected) BrandBlue else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectOdd(index) }
                            .padding(vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(odd.label, color = if (selected) Color.White else SecondaryText, style = MaterialTheme.typography.bodySmall)
                        Text(odd.value, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchLoadState(message: String) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Text(
            message,
            color = SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
        )
    }
}

@Composable
private fun TeamName(name: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        TeamCrest(name)
        Text(
            name,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
        )
    }
}

private val TeamCrestShape = GenericShape { size, _ ->
    moveTo(size.width * 0.5f, 0f)
    lineTo(size.width * 0.92f, size.height * 0.14f)
    lineTo(size.width * 0.86f, size.height * 0.68f)
    lineTo(size.width * 0.5f, size.height)
    lineTo(size.width * 0.14f, size.height * 0.68f)
    lineTo(size.width * 0.08f, size.height * 0.14f)
    close()
}

@Composable
private fun TeamCrest(teamName: String) {
    val palette = when (teamName.fold(0) { total, char -> total + char.code } % 6) {
        0 -> Color(0xFF1C68D9) to Color(0xFF0C2F7B)
        1 -> Color(0xFFDC4C4C) to Color(0xFF741D35)
        2 -> Color(0xFFEE9A2B) to Color(0xFF8A3D14)
        3 -> Color(0xFF34A37B) to Color(0xFF135B52)
        4 -> Color(0xFF8C5BE8) to Color(0xFF3E237B)
        else -> Color(0xFF2E9CCB) to Color(0xFF174A86)
    }
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(TeamCrestShape)
            .background(Brush.verticalGradient(listOf(palette.first, palette.second)))
            .border(1.dp, Color.White.copy(alpha = 0.58f), TeamCrestShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            teamName.trim().take(1).ifBlank { "队" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun BetSlip(
    count: Int,
    stake: Int,
    submitted: Boolean,
    isSubmitting: Boolean,
    onStakeChange: (Int) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("投注单", style = MaterialTheme.typography.titleLarge)
        if (submitted) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(58.dp))
                Text("本地投注已提交", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                Text("测试余额已扣减，本地测试票已写入数据库", color = SecondaryText)
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("已选择 $count 个结果")
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = "清空")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("投注金额", fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onStakeChange((stake - 2).coerceAtLeast(2)) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "减少")
                }
                OutlinedTextField(
                    value = stake.toString(),
                    onValueChange = { value -> onStakeChange(value.filter(Char::isDigit).toIntOrNull() ?: 0) },
                    singleLine = true,
                    modifier = Modifier.width(92.dp),
                )
                IconButton(onClick = { onStakeChange(stake + 2) }) {
                    Icon(Icons.Default.Add, contentDescription = "增加")
                }
            }
            Row {
                Text("预计最高返还", color = SecondaryText)
                Spacer(Modifier.weight(1f))
                Text("¥ ${formatPotential(stake, count)}", color = AlertRed, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                Text(if (isSubmitting) "正在提交…" else "确认投注")
            }
        }
        Spacer(Modifier.height(14.dp))
    }
}

private fun formatPotential(stake: Int, count: Int): String {
    val totalCents = stake * count.coerceAtLeast(1) * 171
    val yuan = totalCents / 100
    val cents = (totalCents % 100).toString().padStart(2, '0')
    return "$yuan.$cents"
}
