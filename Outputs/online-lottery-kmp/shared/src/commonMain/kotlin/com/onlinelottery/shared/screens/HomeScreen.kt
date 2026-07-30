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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.generated.resources.Res
import com.onlinelottery.shared.generated.resources.hall_card_basketball
import com.onlinelottery.shared.generated.resources.hall_card_football
import com.onlinelottery.shared.generated.resources.hall_card_pick3
import com.onlinelottery.shared.generated.resources.hall_card_pick5
import com.onlinelottery.shared.generated.resources.hall_card_seven_star
import com.onlinelottery.shared.generated.resources.hall_card_super_lotto
import com.onlinelottery.shared.generated.resources.hall_hero_trophy
import com.onlinelottery.shared.model.LotteryGame
import com.onlinelottery.shared.model.lotteryGames
import com.onlinelottery.shared.theme.DarkPageBackground
import com.onlinelottery.shared.theme.DarkSecondary
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.PageBackground
import com.onlinelottery.shared.theme.PrimaryText
import com.onlinelottery.shared.theme.SecondaryText
import com.onlinelottery.shared.theme.isDarkTheme
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    openMatches: () -> Unit,
    openMessages: () -> Unit,
    openLottery: (LotteryGame) -> Unit,
) {
    val dark = isDarkTheme()
    val page = if (dark) DarkPageBackground else PageBackground
    val text = if (dark) Color.White else PrimaryText
    val muted = if (dark) DarkSecondary else SecondaryText
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(page),
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 14.dp,
            top = contentPadding.calculateTopPadding() + 14.dp,
            end = 14.dp,
            bottom = contentPadding.calculateBottomPadding() + 26.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("购彩大厅", color = text, style = MaterialTheme.typography.headlineSmall)
                    Text("公平 · 公正 · 透明", color = muted, style = MaterialTheme.typography.bodyMedium)
                }
                Box {
                    CircleIconButton(Icons.Default.Notifications, "消息通知", dark, openMessages)
                    Text(
                        "3",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(Color(0xFFFF453A))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }
        }
        item { HallHeroBanner(onClick = openMatches) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("选择彩种", color = text, style = MaterialTheme.typography.titleMedium)
                lotteryGames.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(136.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowItems.forEachIndexed { index, game ->
                                PremiumLotteryCard(
                                    game = game,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    openLottery(game)
                                }
                            }
                            repeat(2 - rowItems.size) {
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
private fun CircleIconButton(
    icon: ImageVector,
    label: String,
    dark: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (dark) Color.White.copy(alpha = 0.07f) else BrandBlue.copy(alpha = 0.08f))
            .border(1.dp, if (dark) Color.White.copy(alpha = 0.16f) else BrandBlue.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (dark) Color.White else PrimaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun PremiumLotteryCard(
    game: LotteryGame,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    val subtitle = when (game.kind) {
        com.onlinelottery.shared.model.LotteryKind.Football -> "精彩赛事 · 等你来猜"
        com.onlinelottery.shared.model.LotteryKind.Basketball -> "NBA · CBA 热血对决"
        com.onlinelottery.shared.model.LotteryKind.SuperLotto -> "小梦想 · 大乐透"
        com.onlinelottery.shared.model.LotteryKind.Pick3 -> "天天开奖 · 玩法简单"
        com.onlinelottery.shared.model.LotteryKind.Pick5 -> "中奖更易 · 奖金更高"
        com.onlinelottery.shared.model.LotteryKind.SevenStar -> "幸运之星 · 由你点亮"
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(cardBackground(game)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                        Brush.linearGradient(
                        listOf(Color.Black.copy(alpha = 0.05f), Color.Black.copy(alpha = 0.52f)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(game.name, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(17.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(subtitle, color = Color.White.copy(alpha = 0.72f), style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(if (game.kind == com.onlinelottery.shared.model.LotteryKind.Football || game.kind == com.onlinelottery.shared.model.LotteryKind.Basketball) "立即竞猜" else "立即选号", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Text("›", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(start = 5.dp))
                }
            }
        }
    }
}

@Composable
private fun HallHeroBanner(onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(shape)
            .border(1.dp, Color(0xFFFFD98A).copy(alpha = 0.38f), shape)
            .clickable(onClick = onClick),
    ) {
        Image(
            painter = painterResource(Res.drawable.hall_hero_trophy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF050B16).copy(alpha = 0.18f), Color.Transparent))))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 18.dp, bottom = 15.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("精彩不停  好运常在", color = Color(0xFFFFE0A5), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("随时随地  畅享竞猜乐趣", color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "立即参与  ›",
                color = Color(0xFF392207),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFCE72))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

private fun cardBackground(game: LotteryGame): DrawableResource = when (game.kind) {
    com.onlinelottery.shared.model.LotteryKind.Football -> Res.drawable.hall_card_football
    com.onlinelottery.shared.model.LotteryKind.Basketball -> Res.drawable.hall_card_basketball
    com.onlinelottery.shared.model.LotteryKind.SuperLotto -> Res.drawable.hall_card_super_lotto
    com.onlinelottery.shared.model.LotteryKind.Pick3 -> Res.drawable.hall_card_pick3
    com.onlinelottery.shared.model.LotteryKind.Pick5 -> Res.drawable.hall_card_pick5
    com.onlinelottery.shared.model.LotteryKind.SevenStar -> Res.drawable.hall_card_seven_star
}
