package com.onlinelottery.shared.model

data class LotteryGame(
    val name: String,
    val description: String,
    val shortName: String,
    val tone: Long,
    val kind: LotteryKind,
)

enum class LotteryKind {
    Football,
    Basketball,
    SuperLotto,
    Pick3,
    Pick5,
    SevenStar,
}

data class MatchInfo(
    val id: Int,
    val league: String,
    val time: String,
    val home: String,
    val away: String,
    val score: String,
    val half: String,
    val isLive: Boolean,
    val odds: List<String>,
)

val lotteryGames = listOf(
    LotteryGame("竞彩足球", "8场赛事可投", "足", 0xFFFF7386L, LotteryKind.Football),
    LotteryGame("竞彩篮球", "3场赛事可投", "篮", 0xFFFFA25EL, LotteryKind.Basketball),
    LotteryGame("大乐透", "奖池8.15亿", "乐", 0xFFFFC84BL, LotteryKind.SuperLotto),
    LotteryGame("排列三", "今晚21点停", "3", 0xFF5BC7E8L, LotteryKind.Pick3),
    LotteryGame("排列五", "今晚21点停", "5", 0xFF7889F4L, LotteryKind.Pick5),
    LotteryGame("7星彩", "最高奖500万", "7", 0xFF48C99BL, LotteryKind.SevenStar),
)

val matchList = listOf(
    MatchInfo(201, "韩职", "45++", "济州SK", "江原FC", "1-1", "上半场 1-1", true, listOf("4.62", "3.05", "1.71")),
    MatchInfo(202, "韩职", "45++", "全北现代", "大田市民", "0-0", "上半场 0-0", true, listOf("1.77", "3.28", "3.85")),
    MatchInfo(203, "韩职", "中场", "蔚山现代", "仁川联", "1-1", "上半场 1-1", true, listOf("2.02", "2.85", "3.53")),
    MatchInfo(205, "欧冠", "07-22 00:00", "萨巴赫", "库奥皮奥", "VS", "未开赛", false, listOf("1.44", "4.10", "5.25")),
    MatchInfo(206, "欧冠", "07-22 01:00", "奥胡斯", "波兹南", "VS", "未开赛", false, listOf("2.36", "3.15", "2.82")),
)

val basketballMatchList = listOf(
    MatchInfo(301, "美职篮", "10:00", "洛杉矶湖人", "金州勇士", "VS", "让分 -3.5", false, listOf("1.72", "1.88", "2.04")),
    MatchInfo(302, "美职篮", "10:30", "波士顿凯尔特人", "纽约尼克斯", "VS", "大小分 226.5", false, listOf("1.65", "1.91", "2.18")),
    MatchInfo(303, "欧篮联", "01:45", "费内巴切", "皇家马德里", "VS", "让分 +1.5", false, listOf("1.96", "1.82", "1.86")),
)
