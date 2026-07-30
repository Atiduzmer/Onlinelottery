package com.onlinelottery.shared.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.LocalDashboard
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.SecondaryText

private data class FeatureRow(val title: String, val subtitle: String, val status: String = "")

@Composable
fun LocalFeatureScreen(
    title: String,
    dashboard: LocalDashboard,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onMarkMessagesRead: () -> Unit,
) {
    val rows = featureRows(title, dashboard)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "返回个人中心") }
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                if (title == "消息中心" && dashboard.profile.unreadMessages > 0) {
                    Button(
                        onClick = onMarkMessagesRead,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    ) { Text("全部已读") }
                }
            }
        }
        if (rows.isEmpty()) {
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 44.dp, horizontal = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryText)
                        Text("当前没有记录", style = MaterialTheme.typography.titleMedium)
                        Text("完成相应操作后，数据会从本地数据库自动显示在这里", color = SecondaryText)
                    }
                }
            }
        } else {
            items(rows.size) { index ->
                val row = rows[index]
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(row.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (row.status.isNotBlank()) {
                                Text(
                                    statusLabel(row.status),
                                    color = if (row.status in setOf("POSTED", "TICKETED", "AVAILABLE", "READ", "VERIFIED")) LiveGreen else BrandBlue,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        Text(row.subtitle, color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun featureRows(title: String, dashboard: LocalDashboard): List<FeatureRow> = when (title) {
    "全部订单", "投注记录", "待付款", "待开奖", "中奖", "已完成", "已撤单" -> dashboard.orders
        .filter { order ->
            when (title) {
                "待付款" -> order.status in setOf("CREATED", "PAYMENT_PENDING")
                "待开奖" -> order.status in setOf("PAID", "TICKETING", "TICKETED", "PARTIAL_TICKETED", "SETTLING")
                "中奖" -> order.status == "SETTLED"
                "已完成" -> order.status == "SETTLED"
                "已撤单" -> order.status in setOf("FAILED", "CANCELLED", "REFUNDED")
                else -> true
            }
        }
        .map {
            FeatureRow(
                "${it.gameName} · ${formatCents(it.stakeCents)} 元",
                "${it.orderNo}\n${orderContentLabel(it.content)}",
                it.status,
            )
        }

    "资金明细" -> dashboard.walletEntries.map {
        FeatureRow(it.description.ifBlank { it.businessType }, "${formatCents(it.amountCents)} 元 · ${it.transactionNo}", it.status)
    }

    "优惠券" -> dashboard.coupons.map { FeatureRow(it.title, it.subtitle, it.status) }
    "消息中心" -> dashboard.messages.map { FeatureRow(it.title, it.subtitle, it.status) }
    "帮助中心" -> dashboard.helpArticles.map { FeatureRow(it.title, it.subtitle, it.status) }
    "分享赚钱" -> listOf(FeatureRow("本地邀请码 LOCAL8888", "邀请奖励已接入数据库；本地沙盒不会产生真实现金奖励", "AVAILABLE"))
    "实名认证" -> listOf(FeatureRow("本地测试身份", "状态：${dashboard.profile.kycStatus} · 用户号 ${dashboard.profile.userNo}", dashboard.profile.kycStatus))
    "账户安全" -> listOf(FeatureRow("测试会话安全", "本地接口仅绑定当前模拟器，不包含真实手机号或证件信息", "AVAILABLE"))
    "银行卡管理" -> listOf(FeatureRow("本地测试银行卡", "LOCAL BANK · 尾号 8888 · 仅用于沙盒提现", "AVAILABLE"))
    else -> emptyList()
}

private fun formatCents(cents: Long): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

private fun orderContentLabel(content: String): String = when {
    "\"type\": \"number\"" in content || "\"type\":\"number\"" in content -> {
        val numbers = Regex("\\d+").findAll(content.substringAfter("\"zones\":"))
            .map { it.value }
            .toList()
        if (numbers.isEmpty()) "数字彩选号" else "选号：${numbers.joinToString(" · ")}"
    }
    "\"type\":\"sports\"" in content -> {
        val choices = Regex("\"(\\d+-\\d+)\"").findAll(content)
            .map { it.groupValues[1] }
            .toList()
        if (choices.isEmpty()) "赛事投注" else "已选 ${choices.size} 项：${choices.joinToString("、")}"
    }
    else -> "订单内容已保存"
}

private fun statusLabel(status: String): String = when (status) {
    "TICKETED" -> "待开奖"
    "POSTED" -> "已入账"
    "AVAILABLE" -> "可用"
    "DELIVERED" -> "未读"
    "READ" -> "已读"
    "VERIFIED" -> "已认证"
    "PUBLISHED" -> "已发布"
    else -> status
}
