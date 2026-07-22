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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.DaletouOpenResult
import com.onlinelottery.shared.model.DaletouPrize
import com.onlinelottery.shared.model.fallbackDaletouOpenResults
import com.onlinelottery.shared.model.loadDaletouOpenResults
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.BrandBlueSoft
import com.onlinelottery.shared.theme.SecondaryText

@Composable
fun DaletouOpenResultScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    onBack: () -> Unit,
) {
    var resultList by remember { mutableStateOf(fallbackDaletouOpenResults) }
    var selectedIssue by remember { mutableStateOf(fallbackDaletouOpenResults.first().issueNo) }
    var requestVersion by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(true) }
    var usingFallback by remember { mutableStateOf(false) }

    LaunchedEffect(requestVersion) {
        isRefreshing = true
        val remote = runCatching { loadDaletouOpenResults() }
        val loaded = remote.getOrNull().orEmpty()
        if (loaded.isNotEmpty()) {
            resultList = loaded
            selectedIssue = loaded.first().issueNo
            usingFallback = false
        } else {
            resultList = fallbackDaletouOpenResults
            selectedIssue = resultList.first().issueNo
            usingFallback = true
            if (requestVersion > 0) notify("网络暂不可用，正在展示最近一次开奖数据")
        }
        isRefreshing = false
    }

    val current = resultList.firstOrNull { it.issueNo == selectedIssue } ?: resultList.first()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
        contentPadding = PaddingValues(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ResultHeader(
                isRefreshing = isRefreshing,
                onBack = onBack,
                onRefresh = { requestVersion += 1 },
            )
        }

        item {
            IssueSelector(
                resultList = resultList,
                selectedIssue = current.issueNo,
                onIssueSelected = { selectedIssue = it },
            )
        }

        item {
            OpenNumbersCard(current, usingFallback)
        }

        item {
            SalesCard(current)
        }

        item {
            PrizeTable(current.prizes)
        }
    }
}

@Composable
private fun ResultHeader(
    isRefreshing: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
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
            Icon(Icons.Default.ArrowBack, contentDescription = "返回大乐透")
        }
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = AlertRed,
            modifier = Modifier
                .size(27.dp)
                .background(Color(0xFFFFE5E5), CircleShape)
                .padding(5.dp),
        )
        Text(
            "大乐透开奖详情",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Refresh, contentDescription = "刷新开奖数据", tint = BrandBlue)
            }
        }
    }
}

@Composable
private fun IssueSelector(
    resultList: List<DaletouOpenResult>,
    selectedIssue: String,
    onIssueSelected: (String) -> Unit,
) {
    Column {
        Text("最新与往期开奖", style = MaterialTheme.typography.titleMedium)
        LazyRow(
            modifier = Modifier.padding(top = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(resultList.size) { index ->
                val result = resultList[index]
                val selected = result.issueNo == selectedIssue
                Text(
                    text = "${result.issueNo}期",
                    color = if (selected) Color.White else BrandBlue,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) BrandBlue else BrandBlueSoft)
                        .clickable { onIssueSelected(result.issueNo) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun OpenNumbersCard(result: DaletouOpenResult, usingFallback: Boolean) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("第 ${result.issueNo} 期", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    if (usingFallback) "本地兜底数据" else "新浪开奖数据",
                    color = if (usingFallback) SecondaryText else BrandBlue,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                result.openTime.formatOpenTime(),
                color = SecondaryText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                result.redNumbers.forEach { number ->
                    ResultBall(number, AlertRed)
                }
                result.blueNumbers.forEach { number ->
                    ResultBall(number, Color(0xFF4C95EE))
                }
            }
        }
    }
}

@Composable
private fun ResultBall(number: String, color: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SalesCard(result: DaletouOpenResult) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SalesValue("本期销量", result.saleMoney.formatMoney())
            SalesValue("奖池滚存", result.poolMoney.formatMoney())
        }
    }
}

@Composable
private fun SalesValue(label: String, value: String) {
    Column {
        Text(label, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        Text(value, color = AlertRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun PrizeTable(prizes: List<DaletouPrize>) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp)) {
            Text("奖项明细", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(6.dp))
            PrizeTableRow(DaletouPrize("奖项", "中奖注数", "单注奖金", "中奖条件"), header = true)
            prizes.forEach { prize -> PrizeTableRow(prize, header = false) }
        }
    }
}

@Composable
private fun PrizeTableRow(prize: DaletouPrize, header: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (header) Color(0xFFF3F5F8) else Color.White)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableText(prize.name, Modifier.weight(1.15f), header)
        TableText(prize.count, Modifier.weight(1.05f), header)
        TableText(prize.money, Modifier.weight(1.25f), header, if (!header) AlertRed else MaterialTheme.colorScheme.onSurface)
        TableText(prize.condition, Modifier.weight(1.45f), header)
    }
}

@Composable
private fun TableText(text: String, modifier: Modifier, header: Boolean, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = text,
        color = color,
        fontWeight = if (header) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.padding(horizontal = 2.dp),
    )
}

private fun String.formatOpenTime(): String = replace("-", ".").replace(" ", " ")

private fun String.formatMoney(): String {
    if (isBlank()) return "--"
    val split = split(".", limit = 2)
    val integer = split[0].filter(Char::isDigit)
    if (integer.isEmpty()) return "¥$this"
    val grouped = integer.reversed().chunked(3).joinToString(",").reversed()
    return "¥$grouped" + if (split.size > 1) ".${split[1].take(2)}" else ""
}
