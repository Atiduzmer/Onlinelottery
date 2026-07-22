package com.onlinelottery.shared.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.onlinelottery.shared.components.QuickAction
import com.onlinelottery.shared.components.SectionHeader
import com.onlinelottery.shared.components.SurfaceCard
import com.onlinelottery.shared.generated.resources.Res
import com.onlinelottery.shared.generated.resources.reference_home
import com.onlinelottery.shared.model.LotteryGame
import com.onlinelottery.shared.model.lotteryGames
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.LiveGreen
import com.onlinelottery.shared.theme.PrimaryText
import com.onlinelottery.shared.theme.SecondaryText
import com.onlinelottery.shared.theme.WarmAmber
import org.jetbrains.compose.resources.imageResource

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    notify: (String) -> Unit,
    openMatches: () -> Unit,
    openLottery: (LotteryGame) -> Unit,
) {
    var hallMode by remember { mutableStateOf("合买大厅") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 14.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            end = 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 18.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("线下店铺", color = BrandBlue, style = MaterialTheme.typography.titleMedium)
                    Text("实体出票", style = MaterialTheme.typography.headlineSmall)
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFFFF1C8))
                        .clickable { notify("已开启理性购彩提醒") }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(18.dp))
                    Text("放心玩", color = Color(0xFF8C6500), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        item { HeroBanner() }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("账户余额", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    Text("¥ 8,101.84", style = MaterialTheme.typography.headlineSmall)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        QuickAction("投诉", Icons.Default.Call, WarmAmber, { notify("投诉入口已打开") }, Modifier.weight(1f))
                        QuickAction("店主", Icons.Default.Phone, BrandBlue, { notify("已为你联系店主") }, Modifier.weight(1f))
                        QuickAction("微信", Icons.Default.Share, LiveGreen, { notify("分享卡片已准备") }, Modifier.weight(1f))
                        QuickAction("邀请", Icons.Default.Email, Color(0xFF925BEA), { notify("邀请码已复制") }, Modifier.weight(1f))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandBlue),
            ) {
                HallChoice(
                    modifier = Modifier.weight(1f),
                    title = "合买大厅",
                    subtitle = "参与发起合买",
                    icon = Icons.Default.Person,
                    selected = hallMode == "合买大厅",
                ) {
                    hallMode = "合买大厅"
                    notify("已切换至合买大厅")
                }
                HallChoice(
                    modifier = Modifier.weight(1f),
                    title = "跟单大厅",
                    subtitle = "发单赚佣金",
                    icon = Icons.Default.List,
                    selected = hallMode == "跟单大厅",
                ) {
                    hallMode = "跟单大厅"
                    notify("已切换至跟单大厅")
                }
            }
        }

        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
                    SectionHeader("彩种选择", "全部", openMatches)
                    lotteryGames.chunked(4).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            rowItems.forEachIndexed { index, game ->
                                LotteryTile(
                                    game = game,
                                    icon = lotteryIcons[(lotteryGames.indexOf(game)) % lotteryIcons.size],
                                    modifier = Modifier.weight(1f),
                                ) {
                                    openLottery(game)
                                }
                            }
                            repeat(4 - rowItems.size) {
                                Box(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBanner() {
    val sourceImage = imageResource(Res.drawable.reference_home)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(206.dp)
            .clip(RoundedCornerShape(14.dp))
            .semantics { contentDescription = "2026体育赛事活动" },
    ) {
        drawImage(
            image = sourceImage,
            srcOffset = IntOffset(0, 350),
            srcSize = IntSize(1280, 720),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        )
    }
}

@Composable
private fun HallChoice(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(if (selected) Color.White.copy(alpha = 0.13f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(27.dp))
        Column(Modifier.padding(start = 9.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private val lotteryIcons = listOf(
    Icons.Default.Star,
    Icons.Default.Favorite,
    Icons.Default.CheckCircle,
    Icons.Default.List,
    Icons.Default.ShoppingCart,
    Icons.Default.DateRange,
)

@Composable
private fun LotteryTile(
    game: LotteryGame,
    icon: ImageVector,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = game.name,
            tint = Color.White,
            modifier = Modifier
                .size(52.dp)
                .background(Color(game.tone), RoundedCornerShape(15.dp))
                .padding(12.dp),
        )
        Text(
            text = game.name,
            color = PrimaryText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(top = 7.dp),
        )
        Text(
            text = game.description,
            color = SecondaryText,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
