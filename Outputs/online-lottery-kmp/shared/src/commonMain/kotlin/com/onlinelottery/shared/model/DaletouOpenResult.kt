package com.onlinelottery.shared.model

data class DaletouPrize(
    val name: String,
    val count: String,
    val money: String,
    val condition: String,
)

data class DaletouOpenResult(
    val issueNo: String,
    val openTime: String,
    val redNumbers: List<String>,
    val blueNumbers: List<String>,
    val saleMoney: String,
    val poolMoney: String,
    val prizes: List<DaletouPrize>,
)

expect suspend fun loadDaletouOpenResults(): List<DaletouOpenResult>

val fallbackDaletouOpenResults = listOf(
    DaletouOpenResult(
        issueNo = "26080",
        openTime = "2026-07-18 21:25:00",
        redNumbers = listOf("05", "10", "15", "21", "23"),
        blueNumbers = listOf("07", "08"),
        saleMoney = "325550165",
        poolMoney = "765513154.43",
        prizes = listOf(
            DaletouPrize("一等奖", "10", "6,006,451", "中5+2"),
            DaletouPrize("追加一等奖", "5", "4,805,160", "中5+2"),
            DaletouPrize("二等奖", "173", "75,312", "中5+1"),
            DaletouPrize("追加二等奖", "41", "60,250", "中5+1"),
            DaletouPrize("三等奖", "1499", "5,000", "中5+0/4+2"),
            DaletouPrize("四等奖", "24435", "300", "中4+1"),
            DaletouPrize("五等奖", "82732", "150", "中4+0/3+2"),
            DaletouPrize("六等奖", "962406", "15", "中3+1/2+2"),
            DaletouPrize("七等奖", "9479324", "5", "中3+0/2+1/1+2/0+2"),
        ),
    ),
    DaletouOpenResult(
        issueNo = "26079",
        openTime = "2026-07-15 21:25:00",
        redNumbers = listOf("03", "09", "14", "25", "31"),
        blueNumbers = listOf("02", "11"),
        saleMoney = "298406275",
        poolMoney = "742012989.15",
        prizes = listOf(
            DaletouPrize("一等奖", "4", "8,221,040", "中5+2"),
            DaletouPrize("追加一等奖", "1", "6,576,832", "中5+2"),
            DaletouPrize("二等奖", "126", "62,380", "中5+1"),
            DaletouPrize("追加二等奖", "28", "49,904", "中5+1"),
            DaletouPrize("三等奖", "1183", "5,000", "中5+0/4+2"),
            DaletouPrize("四等奖", "21329", "300", "中4+1"),
            DaletouPrize("五等奖", "78605", "150", "中4+0/3+2"),
            DaletouPrize("六等奖", "903818", "15", "中3+1/2+2"),
            DaletouPrize("七等奖", "8562016", "5", "中3+0/2+1/1+2/0+2"),
        ),
    ),
)
