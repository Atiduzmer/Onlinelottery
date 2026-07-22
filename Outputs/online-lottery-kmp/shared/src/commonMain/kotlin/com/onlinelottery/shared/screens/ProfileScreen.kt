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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.QuickAction
import com.onlinelottery.shared.components.SectionHeader
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.theme.AlertRed
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.SecondaryText
import com.onlinelottery.shared.theme.WarmAmber

private data class ServiceItem(val label: String, val icon: ImageVector)

private val serviceItems = listOf(
    ServiceItem("合买记录", Icons.Default.Person),
    ServiceItem("追号记录", Icons.Default.Refresh),
    ServiceItem("保存记录", Icons.Default.List),
    ServiceItem("店铺信息", Icons.Default.Home),
    ServiceItem("我的红包", Icons.Default.Star),
    ServiceItem("我的消息", Icons.Default.Notifications),
    ServiceItem("推送设置", Icons.Default.Send),
    ServiceItem("账号管理", Icons.Default.AccountCircle),
    ServiceItem("分享好友", Icons.Default.Share),
    ServiceItem("我的资料", Icons.Default.AccountBox),
    ServiceItem("联系店主", Icons.Default.Call),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
) {
    var balanceVisible by remember { mutableStateOf(true) }
    var moneyAction by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 14.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
            end = 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 18.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFFB129), CircleShape)
                        .padding(13.dp),
                )
                Column(Modifier.padding(start = 14.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(LiveGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiveGreen, modifier = Modifier.size(14.dp))
                        Text("已实名认证", color = LiveGreen, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("180****1860", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 4.dp))
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { notify("设置面板即将开放") }) {
                    Icon(Icons.Default.Settings, contentDescription = "设置", modifier = Modifier.size(28.dp))
                }
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        Text("当前店铺：恭喜发财", modifier = Modifier.padding(start = 7.dp), color = SecondaryText)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Refresh, contentDescription = "切换店铺", tint = SecondaryText)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("账户总额", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { balanceVisible = !balanceVisible }, modifier = Modifier.size(34.dp)) {
                                    Icon(
                                        if (balanceVisible) Icons.Default.Info else Icons.Default.Clear,
                                        contentDescription = "切换余额显示",
                                        tint = SecondaryText,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Text(
                                if (balanceVisible) "¥ 8,101.84" else "¥ ••••••",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        QuickAction("提现", Icons.Default.Star, WarmAmber, { moneyAction = "提现" }, Modifier.weight(1f))
                        QuickAction("充值", Icons.Default.ShoppingCart, BrandBlue, { moneyAction = "充值" }, Modifier.weight(1f))
                        QuickAction("明细", Icons.Default.List, AlertRed, { notify("资金明细已打开") }, Modifier.weight(1f))
                        QuickAction("邀请", Icons.Default.Star, Color(0xFF925BEA), { notify("邀请码已复制") }, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    SectionHeader("投注记录", "全部记录") { notify("已打开全部记录") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        listOf("0" to "待出票", "1" to "待开奖", "3" to "已中奖", "0" to "今日中奖").forEach { (number, label) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { notify("${label}记录已筛选") }
                                    .padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(number, color = BrandBlue, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(top = 5.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 10.dp)) {
                    serviceItems.chunked(4).forEach { rowItems ->
                        Row(Modifier.fillMaxWidth()) {
                            rowItems.forEach { item ->
                                ServiceButton(item, Modifier.weight(1f)) { notify("${item.label}已打开") }
                            }
                            repeat(4 - rowItems.size) { Box(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(BrandBlue.copy(alpha = 0.08f))
                    .clickable { notify("责任彩票与未成年人保护") }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandBlue)
                Text("理性购彩 · 未成年人禁止购彩", color = BrandBlue, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = BrandBlue)
            }
        }
    }

    moneyAction?.let { action ->
        ModalBottomSheet(onDismissRequest = { moneyAction = null }) {
            MoneySheet(
                action = action,
                close = { moneyAction = null },
                confirm = { amount ->
                    moneyAction = null
                    notify("模拟${action}申请已提交：¥$amount")
                },
            )
        }
    }
}

@Composable
private fun ServiceButton(item: ServiceItem, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(item.icon, contentDescription = item.label, tint = BrandBlue, modifier = Modifier.size(27.dp))
        Text(item.label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun MoneySheet(
    action: String,
    close: () -> Unit,
    confirm: (String) -> Unit,
) {
    var amount by remember { mutableStateOf(if (action == "充值") "100" else "200") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Text("模拟$action", style = MaterialTheme.typography.titleLarge)
        Text("当前原型不会发起真实支付或资金划转", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter(Char::isDigit).take(8) },
            label = { Text("金额（元）") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("50", "100", "200", "500").forEach { preset ->
                Text(
                    preset,
                    color = if (amount == preset) Color.White else BrandBlue,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (amount == preset) BrandBlue else BrandBlue.copy(alpha = 0.1f))
                        .clickable { amount = preset }
                        .padding(vertical = 10.dp),
                )
            }
        }
        Button(
            onClick = { if (amount.isNotBlank() && amount != "0") confirm(amount) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
        ) {
            Text("确认$action")
        }
        Spacer(Modifier.height(14.dp))
    }
}
