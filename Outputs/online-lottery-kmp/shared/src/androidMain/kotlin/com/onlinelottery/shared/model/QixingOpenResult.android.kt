package com.onlinelottery.shared.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val QixingApi = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=04&provinceId=0&pageSize=30&isVerify=1&pageNo=1"

actual suspend fun loadQixingOpenResults(): List<QixingOpenResult> = withContext(Dispatchers.IO) {
    val connection = (URL(QixingApi).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8_000
        readTimeout = 8_000
        setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
        setRequestProperty("Referer", "https://static.sporttery.cn/")
        setRequestProperty("Origin", "https://static.sporttery.cn")
        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 Chrome/150 Mobile Safari/537.36")
        setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.7")
    }
    try {
        check(connection.responseCode in 200..299) { "体彩七星彩接口返回 ${connection.responseCode}" }
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(body)
        check(root.optBoolean("success")) { root.optString("errorMessage", "体彩七星彩接口返回失败") }
        val array = root.getJSONObject("value").optJSONArray("list") ?: JSONArray()
        List(array.length()) { index ->
            array.optJSONObject(index)?.toQixingOpenResult()
        }.filterNotNull()
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.toQixingOpenResult(): QixingOpenResult? {
    val issueNo = optString("lotteryDrawNum")
    val drawResult = optString("lotteryDrawResult")
    if (issueNo.isBlank() || drawResult.isBlank()) return null
    return QixingOpenResult(
        issueNo = issueNo,
        openTime = optString("lotteryDrawTime"),
        numbers = drawResult.trim().split(Regex("\\s+")),
        saleMoney = optString("totalSaleAmount"),
        poolMoney = optString("poolBalanceAfterdraw"),
        prizes = prizeList(),
    )
}

private fun JSONObject.prizeList(): List<QixingPrize> {
    val array = optJSONArray("prizeLevelList") ?: JSONArray()
    return List(array.length()) { index ->
        val item = array.optJSONObject(index) ?: JSONObject()
        QixingPrize(
            name = item.optString("prizeLevel"),
            count = item.optString("stakeCount"),
            money = item.optString("stakeAmountFormat").let { if (it == "-1") "—" else it },
            condition = item.optString("lotteryCondition").ifBlank { defaultCondition(item.optString("prizeLevel")) },
        )
    }
}

private fun defaultCondition(level: String): String = when (level) {
    "一等奖" -> "中7位"
    "二等奖" -> "中6位"
    "三等奖" -> "中5位"
    "四等奖" -> "中4位"
    "五等奖" -> "中3位"
    "六等奖" -> "中2位"
    else -> "-"
}
