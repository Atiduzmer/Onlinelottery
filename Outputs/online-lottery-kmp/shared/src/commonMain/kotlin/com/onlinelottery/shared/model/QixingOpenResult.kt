package com.onlinelottery.shared.model

data class QixingPrize(
    val name: String,
    val count: String,
    val money: String,
    val condition: String,
)

data class QixingOpenResult(
    val issueNo: String,
    val openTime: String,
    val numbers: List<String>,
    val saleMoney: String,
    val poolMoney: String,
    val prizes: List<QixingPrize>,
)

expect suspend fun loadQixingOpenResults(): List<QixingOpenResult>

val fallbackQixingOpenResults = listOf(
    QixingOpenResult(
        issueNo = "26083",
        openTime = "2026-07-21",
        numbers = listOf("5", "2", "1", "9", "6", "3", "12"),
        saleMoney = "16,831,732",
        poolMoney = "261,969,773.37",
        prizes = listOf(
            QixingPrize("一等奖", "0", "—", "中7位"),
            QixingPrize("二等奖", "9", "56,135", "中6位"),
            QixingPrize("三等奖", "14", "3,000", "中5位"),
            QixingPrize("四等奖", "875", "500", "中4位"),
            QixingPrize("五等奖", "15,526", "30", "中3位"),
            QixingPrize("六等奖", "450,017", "5", "中2位"),
        ),
    ),
    QixingOpenResult(
        issueNo = "26080",
        openTime = "2026-07-14",
        numbers = listOf("4", "2", "3", "3", "5", "5", "8"),
        saleMoney = "24,755,996",
        poolMoney = "292,134,860.52",
        prizes = listOf(
            QixingPrize("一等奖", "0", "—", "中7位"),
            QixingPrize("二等奖", "6", "61,825", "中6位"),
            QixingPrize("三等奖", "18", "3,000", "中5位"),
            QixingPrize("四等奖", "698", "500", "中4位"),
            QixingPrize("五等奖", "14,302", "30", "中3位"),
            QixingPrize("六等奖", "407,387", "5", "中2位"),
        ),
    ),
)
