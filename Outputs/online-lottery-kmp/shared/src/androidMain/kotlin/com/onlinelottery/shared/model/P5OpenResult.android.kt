package com.onlinelottery.shared.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val P5Api = "https://webapi.sporttery.cn/gateway/lottery/getHistoryPageListV1.qry?gameNo=350133&provinceId=0&pageSize=30&isVerify=1&termLimits=0&pageNo=1"

actual suspend fun loadP5OpenResults(): List<P5OpenResult> = withContext(Dispatchers.IO) {
    val connection = (URL(P5Api).openConnection() as HttpURLConnection).apply {
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
        check(connection.responseCode in 200..299) { "排列五接口返回 ${connection.responseCode}" }
        val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(body)
        check(root.optBoolean("success")) { root.optString("errorMessage", "排列五接口返回失败") }
        val array = root.getJSONObject("value").optJSONArray("list") ?: JSONArray()
        List(array.length()) { index ->
            array.optJSONObject(index)?.toP5OpenResult()
        }.filterNotNull()
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.toP5OpenResult(): P5OpenResult? {
    val issueNo = optString("lotteryDrawNum")
    val drawResult = optString("lotteryDrawResult")
    if (issueNo.isBlank() || drawResult.isBlank()) return null
    return P5OpenResult(
        issueNo = issueNo,
        openTime = optString("lotteryDrawTime"),
        numbers = drawResult.trim().split(Regex("\\s+")),
        saleMoney = optString("totalSaleAmount"),
        poolMoney = optString("poolBalanceAfterdraw"),
        prizes = prizeList(),
    )
}

private fun JSONObject.prizeList(): List<P5Prize> {
    val array = optJSONArray("prizeLevelList") ?: JSONArray()
    return List(array.length()) { index ->
        val item = array.optJSONObject(index) ?: JSONObject()
        P5Prize(
            name = item.optString("prizeLevel"),
            count = item.optString("stakeCount"),
            money = item.optString("stakeAmountFormat").let { if (it == "-1") "--" else it },
            condition = item.optString("lotteryCondition").ifBlank { "中5位" },
        )
    }
}
