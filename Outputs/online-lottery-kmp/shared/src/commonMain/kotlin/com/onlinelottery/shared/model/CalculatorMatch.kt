package com.onlinelottery.shared.model

enum class CalculatorSport {
    Football,
    Basketball,
}

data class CalculatorOdd(
    val label: String,
    val value: String,
)

data class CalculatorMatch(
    val id: String,
    val number: String,
    val businessDate: String,
    val league: String,
    val time: String,
    val home: String,
    val away: String,
    val status: String,
    val oddsTitle: String,
    val odds: List<CalculatorOdd>,
)

expect suspend fun loadCalculatorMatches(sport: CalculatorSport): List<CalculatorMatch>
