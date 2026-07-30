package com.onlinelottery.shared.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.model.LocalDashboard
import com.onlinelottery.shared.model.LocalOrderCounts
import com.onlinelottery.shared.model.LocalProfile
import com.onlinelottery.shared.model.loadLocalDashboard
import com.onlinelottery.shared.model.markLocalMessagesRead
import com.onlinelottery.shared.model.rechargeLocalWallet
import com.onlinelottery.shared.model.withdrawLocalWallet
import com.onlinelottery.shared.generated.resources.Res
import com.onlinelottery.shared.generated.resources.lottery_football_raster
import com.onlinelottery.shared.generated.resources.profile_balance_gold
import com.onlinelottery.shared.generated.resources.profile_football_avatar
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.DarkPageBackground
import com.onlinelottery.shared.theme.DarkSecondary
import com.onlinelottery.shared.theme.PrimaryText
import com.onlinelottery.shared.theme.SecondaryText
import com.onlinelottery.shared.theme.WarmAmber
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private enum class MoneyAction(val title: String) { Recharge("充值测试余额"), Withdraw("提现测试余额") }

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    openSettings: () -> Unit,
    requestedFeature: String? = null,
    onFeatureRequestConsumed: () -> Unit = {},
) {
    val dark = MaterialTheme.colorScheme.background == DarkPageBackground
    var showBalance by remember { mutableStateOf(true) }
    var dashboard by remember { mutableStateOf<LocalDashboard?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var refreshVersion by remember { mutableIntStateOf(0) }
    var selectedFeature by remember { mutableStateOf<String?>(null) }
    var moneyAction by remember { mutableStateOf<MoneyAction?>(null) }
    var moneyYuan by remember { mutableStateOf("100") }
    var actionBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val text = if (dark) Color(0xFFF5F5F7) else PrimaryText
    val muted = if (dark) DarkSecondary else SecondaryText

    LaunchedEffect(refreshVersion) {
        runCatching { loadLocalDashboard() }
            .onSuccess { dashboard = it; loadError = null }
            .onFailure { loadError = it.message ?: "本地服务不可用" }
    }

    LaunchedEffect(requestedFeature) {
        requestedFeature?.let {
            selectedFeature = it
            onFeatureRequestConsumed()
        }
    }

    val currentDashboard = dashboard
    if (selectedFeature != null && currentDashboard != null) {
        LocalFeatureScreen(
            title = selectedFeature!!,
            dashboard = currentDashboard,
            contentPadding = contentPadding,
            onBack = { selectedFeature = null },
            onMarkMessagesRead = {
                scope.launch {
                    runCatching { markLocalMessagesRead() }
                        .onSuccess { dashboard = it; notify("消息已全部标记为已读") }
                        .onFailure { notify("操作失败：${it.message}") }
                }
            },
        )
        return
    }

    if (currentDashboard == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loadError == null) {
                    CircularProgressIndicator(color = BrandBlue)
                    Text("正在连接本地账户…", color = muted)
                } else {
                    Text("连接失败：$loadError", color = SecondaryText)
                    Button(onClick = { loadError = null; refreshVersion += 1 }) { Text("重新连接") }
                }
            }
        }
        return
    }

    moneyAction?.let { action ->
        AlertDialog(
            onDismissRequest = { if (!actionBusy) moneyAction = null },
            title = { Text(action.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("金额会真实写入本机 PostgreSQL，但不连接任何真实支付渠道。", color = SecondaryText)
                    OutlinedTextField(
                        value = moneyYuan,
                        onValueChange = { moneyYuan = it.filter { char -> char.isDigit() }.take(6) },
                        label = { Text("金额（元）") },
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !actionBusy,
                    onClick = {
                        val cents = (moneyYuan.toLongOrNull() ?: 0L) * 100
                        if (cents < 100) {
                            notify("请输入至少 1 元")
                        } else {
                            scope.launch {
                                actionBusy = true
                                runCatching {
                                    if (action == MoneyAction.Recharge) rechargeLocalWallet(cents) else withdrawLocalWallet(cents)
                                }.onSuccess { result ->
                                    dashboard = loadLocalDashboard()
                                    moneyAction = null
                                    notify(result.message)
                                }.onFailure { notify("操作失败：${it.message ?: "本地服务不可用"}") }
                                actionBusy = false
                            }
                        }
                    },
                ) { Text(if (actionBusy) "处理中…" else "确认") }
            },
            dismissButton = { TextButton(onClick = { moneyAction = null }, enabled = !actionBusy) { Text("取消") } },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
            end = 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ProfileHeader(
                profile = currentDashboard.profile,
                text = text,
                muted = muted,
                dark = dark,
                openMessages = { selectedFeature = "消息中心" },
                openSettings = openSettings,
            )
        }
        item {
            BalanceCard(
                dark = dark,
                profile = currentDashboard.profile,
                showBalance = showBalance,
                onToggle = { showBalance = !showBalance },
                onRecharge = { moneyYuan = "100"; moneyAction = MoneyAction.Recharge },
                onWithdraw = { moneyYuan = "100"; moneyAction = MoneyAction.Withdraw },
            )
        }
        item { OrderCard(dark = dark, counts = currentDashboard.profile.orderCounts, onOpen = { selectedFeature = it }) }
        item { QuickGrid(dark = dark, onOpen = { selectedFeature = it }) }
        item { SecurityCard(dark = dark, openSettings = openSettings, onOpen = { selectedFeature = it }) }
    }
}

@Composable
private fun ProfileHeader(
    profile: LocalProfile,
    text: Color,
    muted: Color,
    dark: Boolean,
    openMessages: () -> Unit,
    openSettings: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(88.dp), contentAlignment = Alignment.BottomCenter) {
            Image(
                painterResource(Res.drawable.profile_football_avatar),
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(82.dp).clip(CircleShape).border(2.dp, if (dark) WarmAmber else BrandBlue, CircleShape),
            )
            Text("VIP", color = if (dark) Color(0xFFFFD98A) else Color.White, style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(if (dark) Color(0xFF322919) else Color(0xFF1B2434)).padding(horizontal = 13.dp, vertical = 3.dp))
        }
        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(profile.nickname, color = text, style = MaterialTheme.typography.headlineSmall)
                Text(profile.levelCode, color = Color.White, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 9.dp).clip(RoundedCornerShape(9.dp)).background(BrandBlue).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            Text("ID：${profile.userNo}  ·  ${if (profile.kycStatus == "VERIFIED") "已认证" else profile.kycStatus}", color = muted, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Notifications, contentDescription = "通知", tint = text, modifier = Modifier.size(24.dp).clickable(onClick = openMessages))
                Icon(Icons.Default.Settings, contentDescription = "设置", tint = text, modifier = Modifier.size(24.dp).clickable(onClick = openSettings))
            }
            Row(Modifier.clip(RoundedCornerShape(14.dp)).border(1.dp, if (dark) WarmAmber.copy(alpha = .65f) else BrandBlue.copy(alpha = .35f), RoundedCornerShape(14.dp)).padding(horizontal = 9.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = if (dark) WarmAmber else BrandBlue, modifier = Modifier.size(15.dp))
                Text(profile.growthPoints.toString(), color = text, style = MaterialTheme.typography.labelMedium)
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun BalanceCard(dark: Boolean, profile: LocalProfile, showBalance: Boolean, onToggle: () -> Unit, onRecharge: () -> Unit, onWithdraw: () -> Unit) {
    val title = if (dark) Color(0xFFFFDFA6) else Color(0xFF20395F)
    val cardBrush = if (dark) Brush.linearGradient(listOf(Color(0xFF1A1A1C), Color(0xFF0D1016))) else Brush.linearGradient(listOf(Color(0xFFE9F2FF), Color.White))
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(cardBrush).border(1.dp, if (dark) WarmAmber.copy(alpha = .48f) else BrandBlue.copy(alpha = .20f), RoundedCornerShape(24.dp))) {
        if (dark) {
            Image(
                painterResource(Res.drawable.profile_balance_gold),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().clip(RoundedCornerShape(24.dp)),
            )
        }
        Column {
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("账户余额（元）", color = title, style = MaterialTheme.typography.bodyLarge)
                        Icon(Icons.Default.AccountCircle, contentDescription = "显示余额", tint = title, modifier = Modifier.padding(start = 8.dp).size(20.dp).clickable(onClick = onToggle))
                    }
                    Text(if (showBalance) formatMoney(profile.availableBalanceCents) else "••••••", color = if (dark) Color(0xFFFFE6B7) else Color(0xFF16325B), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                Button(onClick = onRecharge, colors = ButtonDefaults.buttonColors(containerColor = if (dark) Color(0xFFFFD37A) else Color(0xFFFFC95E), contentColor = Color(0xFF30230F)), shape = RoundedCornerShape(22.dp)) { Text("充值") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onWithdraw, shape = RoundedCornerShape(22.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (dark) Color(0xFFFFDFA6) else BrandBlue)) { Text("提现", color = title) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp).border(1.dp, title.copy(alpha = .12f), RoundedCornerShape(0.dp)), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("累计中奖", formatMoney(profile.totalPrizeCents), title)
                Stat("奖金账户", formatMoney(profile.prizeBalanceCents), title)
                Stat("优惠券", "${profile.couponCount} 张", title)
            }
        }
    }
}

@Composable private fun Stat(label: String, value: String, color: Color) { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(label, color = color.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall); Text(value, color = color, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) } }

@Composable
private fun OrderCard(dark: Boolean, counts: LocalOrderCounts, onOpen: (String) -> Unit) {
    SurfaceCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth().clickable { onOpen("全部订单") }, verticalAlignment = Alignment.CenterVertically) { Text("我的订单", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("全部订单", color = if (dark) DarkSecondary else SecondaryText); Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = if (dark) DarkSecondary else SecondaryText) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(
                    Triple("待付款", counts.pendingPayment, Icons.Default.ShoppingCart),
                    Triple("待开奖", counts.awaitingDraw, Icons.Default.DateRange),
                    Triple("中奖", counts.won, Icons.Default.Star),
                    Triple("已完成", counts.completed, Icons.Default.CheckCircle),
                    Triple("已撤单", counts.cancelled, Icons.Default.Clear),
                ).forEach { (label, count, icon) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onOpen(label) }) {
                        Box(contentAlignment = Alignment.TopEnd) { Icon(icon, contentDescription = label, tint = if (dark) Color(0xFFFFDFA6) else BrandBlue, modifier = Modifier.size(31.dp)); if (count > 0) Text(count.toString(), color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.clip(CircleShape).background(Color(0xFFFF4D4F)).padding(horizontal = 5.dp, vertical = 2.dp)) }
                        Text(label, color = if (dark) DarkSecondary else SecondaryText, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickGrid(dark: Boolean, onOpen: (String) -> Unit) {
    val items = listOf("投注记录" to Icons.Default.List, "追号记录" to Icons.Default.Refresh, "我的关注" to Icons.Default.Star, "资金明细" to Icons.Default.AccountCircle, "优惠券" to Icons.Default.ShoppingCart, "分享赚钱" to Icons.Default.Share, "消息中心" to Icons.Default.Notifications, "帮助中心" to Icons.Default.Person)
    SurfaceCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(vertical = 5.dp)) { items.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { row.forEach { (label, icon) -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(74.dp).clickable { onOpen(label) }.padding(vertical = 7.dp)) { Icon(icon, contentDescription = label, tint = if (dark) Color(0xFF62A2FF) else BrandBlue, modifier = Modifier.size(30.dp)); Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 5.dp)) } } } } } }
}

@Composable
private fun SecurityCard(dark: Boolean, openSettings: () -> Unit, onOpen: (String) -> Unit) {
    val rows = listOf("实名认证" to Icons.Default.CheckCircle, "账户安全" to Icons.Default.Lock, "银行卡管理" to Icons.Default.List, "设置" to Icons.Default.Settings)
    SurfaceCard(Modifier.fillMaxWidth()) { Column { rows.forEachIndexed { index, (label, icon) -> Row(Modifier.fillMaxWidth().clickable { if (label == "设置") openSettings() else onOpen(label) }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, contentDescription = label, tint = if (dark) Color(0xFFFFDFA6) else BrandBlue, modifier = Modifier.size(23.dp)); Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 13.dp)); Spacer(Modifier.weight(1f)); if (label == "实名认证") Text("已认证", color = if (dark) DarkSecondary else SecondaryText, style = MaterialTheme.typography.bodySmall); Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = if (dark) DarkSecondary else SecondaryText); }; if (index < rows.lastIndex) Spacer(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = .35f)).padding(horizontal = 16.dp)) } } }
}

private fun formatMoney(cents: Long): String = "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
