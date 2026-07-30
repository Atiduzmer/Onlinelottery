package com.onlinelottery.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.onlinelottery.shared.screens.HomeScreen
import com.onlinelottery.shared.screens.DaletouOpenResultScreen
import com.onlinelottery.shared.screens.P3OpenResultScreen
import com.onlinelottery.shared.screens.P5OpenResultScreen
import com.onlinelottery.shared.screens.QixingOpenResultScreen
import com.onlinelottery.shared.screens.MatchScreen
import com.onlinelottery.shared.screens.NumberLotteryDetailScreen
import com.onlinelottery.shared.screens.ProfileScreen
import com.onlinelottery.shared.screens.SettingsScreen
import com.onlinelottery.shared.model.LotteryGame
import com.onlinelottery.shared.model.LotteryKind
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.DarkPageBackground
import com.onlinelottery.shared.theme.DarkSecondary
import com.onlinelottery.shared.theme.PageBackground
import com.onlinelottery.shared.theme.SecondaryText
import com.onlinelottery.shared.theme.OnlineLotteryTheme
import com.onlinelottery.shared.theme.AppThemeMode
import com.onlinelottery.shared.theme.isDarkTheme
import kotlinx.coroutines.launch

private enum class MainTab(
    val label: String,
    val icon: ImageVector,
) {
    Home("大厅", Icons.Default.Home),
    Matches("比赛", Icons.Default.DateRange),
    Profile("个人", Icons.Default.Person),
}

@Composable
fun OnlineLotteryApp(onThemeChanged: (Boolean) -> Unit = {}) {
    var themeModeName by rememberSaveable { mutableStateOf(AppThemeMode.System.name) }
    val themeMode = AppThemeMode.valueOf(themeModeName)
    OnlineLotteryTheme(themeMode) {
        var activeTab by remember { mutableStateOf(MainTab.Home) }
        var selectedGame by remember { mutableStateOf<LotteryGame?>(null) }
        var showingSettings by remember { mutableStateOf(false) }
        var showingDaletouResults by remember { mutableStateOf(false) }
        var showingQixingResults by remember { mutableStateOf(false) }
        var showingP3Results by remember { mutableStateOf(false) }
        var showingP5Results by remember { mutableStateOf(false) }
        var profileFeatureRequest by remember { mutableStateOf<String?>(null) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val notify: (String) -> Unit = { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        }
        val darkHome = isDarkTheme()
        SideEffect { onThemeChanged(darkHome) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (darkHome) DarkPageBackground else PageBackground)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (darkHome) DarkPageBackground else PageBackground,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (selectedGame == null && !showingSettings) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (darkHome) Color(0xFF0D111A) else Color.White,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            shadowElevation = 5.dp,
                        ) {
                            NavigationBar(
                                modifier = Modifier.height(78.dp),
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                            ) {
                                MainTab.entries.forEach { tab ->
                                    NavigationBarItem(
                                        selected = activeTab == tab,
                                        onClick = { activeTab = tab },
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = if (darkHome) Color.White else BrandBlue,
                                            selectedTextColor = if (darkHome) Color.White else BrandBlue,
                                            indicatorColor = if (darkHome) Color.White.copy(alpha = 0.12f) else BrandBlue.copy(alpha = 0.10f),
                                            unselectedIconColor = if (darkHome) DarkSecondary else SecondaryText,
                                            unselectedTextColor = if (darkHome) DarkSecondary else SecondaryText,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                },
            ) { innerPadding ->
                val game = selectedGame
                if (showingSettings) {
                    SettingsScreen(
                        contentPadding = innerPadding,
                        mode = themeMode,
                        onModeChange = { themeModeName = it.name },
                        onBack = { showingSettings = false },
                    )
                } else if (game != null) {
                    when (game.kind) {
                        LotteryKind.Football,
                        LotteryKind.Basketball,
                        -> MatchScreen(
                            contentPadding = innerPadding,
                            notify = notify,
                            initialSport = if (game.kind == LotteryKind.Football) "足球" else "篮球",
                            onBack = { selectedGame = null },
                            lockedSport = true,
                        )

                        else -> {
                            if (showingDaletouResults && game.kind == LotteryKind.SuperLotto) {
                                DaletouOpenResultScreen(
                                    contentPadding = innerPadding,
                                    notify = notify,
                                    onBack = { showingDaletouResults = false },
                                )
                            } else if (showingQixingResults && game.kind == LotteryKind.SevenStar) {
                                QixingOpenResultScreen(
                                    contentPadding = innerPadding,
                                    notify = notify,
                                    onBack = { showingQixingResults = false },
                                )
                            } else if (showingP5Results && game.kind == LotteryKind.Pick5) {
                                P5OpenResultScreen(
                                    contentPadding = innerPadding,
                                    notify = notify,
                                    onBack = { showingP5Results = false },
                                )
                            } else if (showingP3Results && game.kind == LotteryKind.Pick3) {
                                P3OpenResultScreen(
                                    contentPadding = innerPadding,
                                    notify = notify,
                                    onBack = { showingP3Results = false },
                                )
                            } else {
                                NumberLotteryDetailScreen(
                                    game = game,
                                    contentPadding = innerPadding,
                                    notify = notify,
                                    onBack = {
                                        showingDaletouResults = false
                                        showingQixingResults = false
                                        showingP3Results = false
                                        showingP5Results = false
                                        selectedGame = null
                                    },
                                    openResults = {
                                        when (game.kind) {
                                            LotteryKind.SuperLotto -> {
                                                showingDaletouResults = true
                                            }
                                            LotteryKind.SevenStar -> {
                                                showingQixingResults = true
                                            }
                                            LotteryKind.Pick3 -> {
                                                showingP3Results = true
                                            }
                                            LotteryKind.Pick5 -> {
                                                showingP5Results = true
                                            }
                                            else -> Unit
                                        }
                                    },
                                )
                            }
                        }
                    }
                } else when (activeTab) {
                    MainTab.Home -> HomeScreen(
                        contentPadding = innerPadding,
                        openMatches = { activeTab = MainTab.Matches },
                        openMessages = {
                            profileFeatureRequest = "消息中心"
                            activeTab = MainTab.Profile
                        },
                        openLottery = {
                            showingDaletouResults = false
                            showingQixingResults = false
                            showingP3Results = false
                            showingP5Results = false
                            selectedGame = it
                        },
                    )

                    MainTab.Matches -> MatchScreen(
                        contentPadding = innerPadding,
                        notify = notify,
                    )

                    MainTab.Profile -> ProfileScreen(
                        contentPadding = innerPadding,
                        notify = notify,
                        openSettings = { showingSettings = true },
                        requestedFeature = profileFeatureRequest,
                        onFeatureRequestConsumed = { profileFeatureRequest = null },
                    )
                }
            }
        }
    }
}
