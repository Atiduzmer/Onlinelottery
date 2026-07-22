package com.onlinelottery.shared.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val DaletouApi = "https://mix.lottery.sina.com.cn/gateway/index/entry?format=json&__caller__=web&__version__=1.0.0&__verno__=1&cat1=gameOpenInfo&lottoType=201&dpc=1"

actual suspend fun loadDaletouOpenResults(): List<DaletouOpenResult> = withContext(Dispatchers.IO) {
    val latest = requestDaletouResult(null)
    val latestIssue = latest.issueNo.toIntOrNull() ?: return@withContext listOf(latest)
    val history = (1..8).mapNotNull { offset ->
        runCatching { requestDaletouResult((latestIssue - offset).toString()) }.getOrNull()
    }
    (listOf(latest) + history).distinctBy { it.issueNo }
}

private fun requestDaletouResult(issueNo: String?): DaletouOpenResult {
    val url = if (issueNo.isNullOrBlank()) DaletouApi else "$DaletouApi&issueNo=$issueNo"
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8_000
        readTimeout = 8_000
        setRequestProperty("Accept", "application/json")
        setRequestProperty("Referer", "https://lottery.sina.com.cn/")
    }
    try {
        check(connection.responseCode in 200..299) { "新浪开奖接口返回 ${connection.responseCode}" }
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val data = JSONObject(body).getJSONObject("result").getJSONObject("data")
        return DaletouOpenResult(
            issueNo = data.optString("issueNo"),
            openTime = data.optString("openTime"),
            redNumbers = data.stringList("redResults"),
            blueNumbers = data.stringList("blueResults"),
            saleMoney = data.optString("saleMoney"),
            poolMoney = data.optString("poolMoney"),
            prizes = data.prizes(),
        )
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.stringList(name: String): List<String> {
    val array = optJSONArray(name) ?: return emptyList()
    return List(array.length()) { index -> array.optString(index) }
}

private fun JSONObject.prizes(): List<DaletouPrize> {
    val array = optJSONArray("prizeList") ?: JSONArray()
    return List(array.length()) { index ->
        val item = array.optJSONObject(index) ?: JSONObject()
        DaletouPrize(
            name = item.optString("prizeDesc"),
            count = item.optString("prizeCount"),
            money = item.optString("prizeMoney"),
            condition = prizeCondition(item.optString("prizeDesc")),
        )
    }
}

private fun prizeCondition(name: String): String = when (name) {
    "一等奖", "追加一等奖" -> "中5+2"
    "二等奖", "追加二等奖" -> "中5+1"
    "三等奖" -> "中5+0/4+2"
    "四等奖" -> "中4+1"
    "五等奖" -> "中4+0/3+2"
    "六等奖" -> "中3+1/2+2"
    "七等奖" -> "中3+0/2+1/1+2/0+2"
    else -> "-"
}
