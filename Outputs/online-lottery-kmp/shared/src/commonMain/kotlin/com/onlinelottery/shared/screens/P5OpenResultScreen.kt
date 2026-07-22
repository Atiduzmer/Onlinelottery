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
import com.onlinelottery.shared.model.P5OpenResult
import com.onlinelottery.shared.model.P5Prize
import com.onlinelottery.shared.model.fallbackP5OpenResults
import com.onlinelottery.shared.model.loadP5OpenResults
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.BrandBlueSoft
import com.onlinelottery.shared.theme.SecondaryText

@Composable
fun P5OpenResultScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    onBack: () -> Unit,
) {
    var resultList by remember { mutableStateOf(fallbackP5OpenResults) }
    var selectedIssue by remember { mutableStateOf(fallbackP5OpenResults.first().issueNo) }
    var requestVersion by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(true) }
    var usingFallback by remember { mutableStateOf(false) }

    LaunchedEffect(requestVersion) {
        isRefreshing = true
        val loaded = runCatching { loadP5OpenResults() }.getOrNull().orEmpty()
        if (loaded.isNotEmpty()) {
            resultList = loaded
            selectedIssue = loaded.first().issueNo
            usingFallback = false
        } else {
            resultList = fallbackP5OpenResults
            selectedIssue = resultList.first().issueNo
            usingFallback = true
            if (requestVersion > 0) notify("网络暂不可用，正在展示最近一次排列五开奖数据")
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
        item { P5Header(isRefreshing, onBack) { requestVersion += 1 } }
        item { P5IssueSelector(resultList, current.issueNo) { selectedIssue = it } }
        item { P5NumbersCard(current, usingFallback) }
        item { P5SalesCard(current) }
        item { P5PrizeTable(current.prizes) }
    }
}

@Composable
private fun P5Header(isRefreshing: Boolean, onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "返回排列五")
        }
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = AlertRed,
            modifier = Modifier
                .size(27.dp)
                .background(Color(0xFFFFE7E6), CircleShape)
                .padding(5.dp),
        )
        Text("排列五开奖详情", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onRefresh, enabled = !isRefreshing) {
            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Refresh, contentDescription = "刷新排列五开奖数据", tint = BrandBlue)
            }
        }
    }
}

@Composable
private fun P5IssueSelector(resultList: List<P5OpenResult>, selectedIssue: String, onSelected: (String) -> Unit) {
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
                        .clickable { onSelected(result.issueNo) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun P5NumbersCard(result: P5OpenResult, usingFallback: Boolean) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("第 ${result.issueNo} 期", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                Text(
                    if (usingFallback) "本地兜底数据" else "体彩开奖数据",
                    color = if (usingFallback) SecondaryText else BrandBlue,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(result.openTime, color = SecondaryText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 5.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                result.numbers.forEach { number -> P5Ball(number) }
            }
        }
    }
}

@Composable
private fun P5Ball(number: String) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(AlertRed, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun P5SalesCard(result: P5OpenResult) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            P5SalesValue("本期销量", result.saleMoney.p5Money())
            P5SalesValue("奖池滚存", result.poolMoney.p5Money())
        }
    }
}

@Composable
private fun P5SalesValue(label: String, value: String) {
    Column {
        Text(label, color = SecondaryText, style = MaterialTheme.typography.bodySmall)
        Text("¥$value", color = AlertRed, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun P5PrizeTable(prizes: List<P5Prize>) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 13.dp)) {
            Text("中奖详情", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp))
            P5PrizeRow("奖项", "中奖注数", "单注奖金", "中奖条件", true)
            prizes.forEach { prize ->
                P5PrizeRow(prize.name, prize.count, prize.money.p5Money(), prize.condition, false)
            }
        }
    }
}

@Composable
private fun P5PrizeRow(name: String, count: String, money: String, condition: String, header: Boolean) {
    val textColor = if (header) SecondaryText else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = if (header) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        P5TableText(name, Modifier.weight(0.82f), textColor, TextAlign.Start)
        P5TableText(count.ifBlank { "--" }, Modifier.weight(0.9f), textColor, TextAlign.Center)
        P5TableText(if (header) money else "¥$money", Modifier.weight(1.0f), if (header) textColor else AlertRed, TextAlign.Center)
        P5TableText(condition.ifBlank { "--" }, Modifier.weight(0.86f), textColor, TextAlign.End)
    }
}

@Composable
private fun P5TableText(value: String, modifier: Modifier, color: Color, alignment: TextAlign) {
    Text(
        value,
        color = color,
        textAlign = alignment,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

private fun String.p5Money(): String = ifBlank { "--" }
