package com.onlinelottery.shared.model

data class P5Prize(
    val name: String,
    val count: String,
    val money: String,
    val condition: String,
)

data class P5OpenResult(
    val issueNo: String,
    val openTime: String,
    val numbers: List<String>,
    val saleMoney: String,
    val poolMoney: String,
    val prizes: List<P5Prize>,
)

expect suspend fun loadP5OpenResults(): List<P5OpenResult>

val fallbackP5OpenResults = listOf(
    P5OpenResult(
        issueNo = "26192",
        openTime = "2026-07-21",
        numbers = listOf("3", "0", "6", "1", "4"),
        saleMoney = "21,949,768",
        poolMoney = "64,701,991.16",
        prizes = listOf(P5Prize("一等奖", "104", "100,000", "中5位")),
    ),
)
