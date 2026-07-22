package com.onlinelottery.shared.model

data class P3Prize(
    val name: String,
    val count: String,
    val money: String,
    val condition: String,
)

data class P3OpenResult(
    val issueNo: String,
    val openTime: String,
    val numbers: List<String>,
    val saleMoney: String,
    val poolMoney: String,
    val prizes: List<P3Prize>,
)

expect suspend fun loadP3OpenResults(): List<P3OpenResult>

val fallbackP3OpenResults = listOf(
    P3OpenResult(
        issueNo = "26192",
        openTime = "2026-07-21",
        numbers = listOf("3", "0", "6"),
        saleMoney = "41,620,052",
        poolMoney = "0",
        prizes = listOf(
            P3Prize("直选", "12,072", "1,040", "按顺序中3位"),
            P3Prize("组选3", "0", "346", "中两同号与一不同号"),
            P3Prize("组选6", "26,160", "173", "中3个不同号"),
        ),
    ),
)
