package com.onlinelottery.shared.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val CalculatorApiBase = "https://webapi.sporttery.cn/gateway/uniform"

actual suspend fun loadCalculatorMatches(sport: CalculatorSport): List<CalculatorMatch> = withContext(Dispatchers.IO) {
    val sportPath = if (sport == CalculatorSport.Football) "football" else "basketball"
    val apiUrl = "$CalculatorApiBase/$sportPath/getMatchCalculatorV1.qry?channel=web"
    val connection = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8_000
        readTimeout = 8_000
        setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
        setRequestProperty("Referer", "https://www.lottery.gov.cn/jc/jsq/zqspf/")
        setRequestProperty("Origin", "https://www.lottery.gov.cn")
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/150 Mobile Safari/537.36")
        setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7")
    }
    try {
        check(connection.responseCode in 200..299) { "竞彩赛事接口返回 ${connection.responseCode}" }
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(body)
        check(root.optBoolean("success")) { root.optString("errorMessage", "竞彩赛事接口返回失败") }
        val groups = root.getJSONObject("value").optJSONArray("matchInfoList") ?: JSONArray()
        buildList {
            for (groupIndex in 0 until groups.length()) {
                val group = groups.optJSONObject(groupIndex) ?: continue
                val matchArray = group.optJSONArray("subMatchList") ?: JSONArray()
                for (matchIndex in 0 until matchArray.length()) {
                    val match = matchArray.optJSONObject(matchIndex) ?: continue
                    match.toCalculatorMatch(sport)?.let(::add)
                }
            }
        }
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.toCalculatorMatch(sport: CalculatorSport): CalculatorMatch? {
    val id = optString("matchId")
    if (id.isBlank()) return null
    val odds = if (sport == CalculatorSport.Football) footballOdds() else basketballOdds()
    if (odds.isEmpty()) return null
    return CalculatorMatch(
        id = id,
        number = optString("matchNumStr"),
        businessDate = optString("businessDate"),
        league = optString("leagueAbbName").ifBlank { optString("leagueAllName") },
        time = "${optString("matchDate").removePrefix(optString("businessDate").take(5))} ${optString("matchTime").take(5)}".trim(),
        home = optString("homeTeamAbbName").ifBlank { optString("homeTeamAllName") },
        away = optString("awayTeamAbbName").ifBlank { optString("awayTeamAllName") },
        status = optString("matchStatus"),
        oddsTitle = if (sport == CalculatorSport.Football) "胜平负" else "胜负",
        odds = odds,
    )
}

private fun JSONObject.footballOdds(): List<CalculatorOdd> {
    val had = optJSONObject("had") ?: return emptyList()
    return listOf(
        CalculatorOdd("胜", had.optString("h")),
        CalculatorOdd("平", had.optString("d")),
        CalculatorOdd("负", had.optString("a")),
    ).filter { it.value.isNotBlank() && it.value != "-" }
}

private fun JSONObject.basketballOdds(): List<CalculatorOdd> {
    val mnl = optJSONObject("mnl") ?: return emptyList()
    return listOf(
        CalculatorOdd("主胜", mnl.optString("h")),
        CalculatorOdd("客胜", mnl.optString("a")),
    ).filter { it.value.isNotBlank() && it.value != "-" }
}
