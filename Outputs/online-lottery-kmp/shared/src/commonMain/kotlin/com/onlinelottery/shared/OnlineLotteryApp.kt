package com.onlinelottery.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.onlinelottery.shared.screens.HomeScreen
import com.onlinelottery.shared.screens.DaletouOpenResultScreen
import com.onlinelottery.shared.screens.P3OpenResultScreen
import com.onlinelottery.shared.screens.P5OpenResultScreen
import com.onlinelottery.shared.screens.QixingOpenResultScreen
import com.onlinelottery.shared.screens.MatchScreen
import com.onlinelottery.shared.screens.NumberLotteryDetailScreen
import com.onlinelottery.shared.screens.ProfileScreen
import com.onlinelottery.shared.model.LotteryGame
import com.onlinelottery.shared.model.LotteryKind
import com.onlinelottery.shared.theme.BrandBlue
import com.onlinelottery.shared.theme.OnlineLotteryTheme
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
fun OnlineLotteryApp() {
    OnlineLotteryTheme {
        var activeTab by remember { mutableStateOf(MainTab.Home) }
        var selectedGame by remember { mutableStateOf<LotteryGame?>(null) }
        var showingDaletouResults by remember { mutableStateOf(false) }
        var showingQixingResults by remember { mutableStateOf(false) }
        var showingP3Results by remember { mutableStateOf(false) }
        var showingP5Results by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val notify: (String) -> Unit = { message ->
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (selectedGame == null) {
                        NavigationBar {
                            MainTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BrandBlue,
                                        selectedTextColor = BrandBlue,
                                        indicatorColor = BrandBlue.copy(alpha = 0.12f),
                                    ),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                val game = selectedGame
                if (game != null) {
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
                                    openResults = if (game.kind == LotteryKind.SuperLotto || game.kind == LotteryKind.SevenStar || game.kind == LotteryKind.Pick3 || game.kind == LotteryKind.Pick5) {
                                        {
                                            if (game.kind == LotteryKind.SuperLotto) {
                                                showingDaletouResults = true
                                            } else if (game.kind == LotteryKind.SevenStar) {
                                                showingQixingResults = true
                                            } else if (game.kind == LotteryKind.Pick3) {
                                                showingP3Results = true
                                            } else {
                                                showingP5Results = true
                                            }
                                        }
                                    } else {
                                        null
                                    },
                                )
                            }
                        }
                    }
                } else when (activeTab) {
                    MainTab.Home -> HomeScreen(
                        contentPadding = innerPadding,
                        notify = notify,
                        openMatches = { activeTab = MainTab.Matches },
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
                    )
                }
            }
        }
    }
}
