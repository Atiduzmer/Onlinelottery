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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.CalculatorMatch
import com.onlinelottery.shared.model.CalculatorSport
import com.onlinelottery.shared.model.loadCalculatorMatches
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.BrandBlueSoft
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.SecondaryText

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
    var playType by remember { mutableStateOf("竞彩") }
    var selectedDay by remember(sport) { mutableStateOf("") }
    var selectedOdds by remember { mutableStateOf(setOf<String>()) }
    var showBetSlip by remember { mutableStateOf(false) }
    var stake by remember { mutableIntStateOf(20) }
    var submitted by remember { mutableStateOf(false) }
    var matchList by remember(sport) { mutableStateOf<List<CalculatorMatch>>(emptyList()) }
    var isLoading by remember(sport) { mutableStateOf(true) }
    var requestVersion by remember { mutableIntStateOf(0) }

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
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (onBack != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回大厅")
                        }
                        Text(
                            text = if (sport == "足球") "竞彩足球" else "竞彩篮球",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 2.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "玩法说明",
                            color = BrandBlue,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { notify("${if (sport == "足球") "竞彩足球" else "竞彩篮球"}玩法说明") }
                                .padding(8.dp),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (lockedSport) {
                        FilterPill(
                            sport,
                            true,
                            if (sport == "足球") Icons.Default.Star else Icons.Default.Favorite,
                        ) {}
                    } else {
                        FilterPill("足球", sport == "足球", Icons.Default.Star) { sport = "足球" }
                        FilterPill("篮球", sport == "篮球", Icons.Default.Favorite) { sport = "篮球" }
                    }
                    Spacer(Modifier.weight(1f))
                    FilterPill(if (isLoading) "加载中" else "刷新", false, Icons.Default.DateRange) { requestVersion += 1 }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    listOf("竞彩", "北单", "足彩").forEach { item ->
                        TextTab(item, playType == item) {
                            playType = item
                            if (item != "竞彩") notify("${item}数据准备中")
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(matchList.map { it.businessDate }.distinct().size) { index ->
                        val date = matchList.map { it.businessDate }.distinct()[index]
                        DayTab(date.removePrefix("2026-"), selectedDay == date) { selectedDay = date }
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
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF20242C))
                    .clickable { showBetSlip = true }
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.White)
                Text(
                    "投注单 · 已选 ${selectedOdds.size} 项",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 9.dp),
                )
                Spacer(Modifier.weight(1f))
                Text("查看", color = Color(0xFF8FB0FF))
            }
        }
    }

    if (showBetSlip && selectedOdds.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = { showBetSlip = false }) {
            BetSlip(
                count = selectedOdds.size,
                stake = stake,
                submitted = submitted,
                onStakeChange = { stake = it.coerceAtLeast(0) },
                onClear = {
                    selectedOdds = emptySet()
                    showBetSlip = false
                },
                onSubmit = {
                    submitted = true
                    notify("模拟投注已提交，等待线下门店确认")
                },
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) BrandBlue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(19.dp))
        Text(label, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TextTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.onSurface else SecondaryText, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        Box(
            Modifier
                .padding(top = 5.dp)
                .width(34.dp)
                .height(3.dp)
                .background(if (selected) BrandBlue else Color.Transparent, CircleShape),
        )
    }
}

@Composable
private fun DayTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        textAlign = TextAlign.Center,
        color = if (selected) MaterialTheme.colorScheme.onSurface else SecondaryText,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) BrandBlueSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

@Composable
private fun MatchCard(
    match: CalculatorMatch,
    selectedOdds: Set<String>,
    onSelectOdd: (Int) -> Unit,
) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${match.number} · ${match.league}", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.weight(1f))
                Text(match.time, color = SecondaryText, fontWeight = FontWeight.Medium)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamName(match.home, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
                    Text(
                        if (match.status == "Selling") "VS" else match.status,
                        color = if (match.status == "Selling") MaterialTheme.colorScheme.onSurface else LiveGreen,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(if (match.status == "Selling") "销售中" else "已停售", color = if (match.status == "Selling") AlertRed else SecondaryText, style = MaterialTheme.typography.bodySmall)
                }
                TeamName(match.away, Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(match.oddsTitle + " SP", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                match.odds.forEachIndexed { index, odd ->
                    val selected = "${match.id}-$index" in selectedOdds
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) BrandBlue else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onSelectOdd(index) }
                            .padding(vertical = 7.dp),
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
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF37A96A), modifier = Modifier.size(22.dp))
        Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
private fun BetSlip(
    count: Int,
    stake: Int,
    submitted: Boolean,
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
                Text("模拟投注已提交", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp))
                Text("等待线下门店确认出票", color = SecondaryText)
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            ) {
                Text("确认模拟投注")
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
