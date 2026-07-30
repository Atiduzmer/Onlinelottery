package com.onlinelottery.shared.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val LocalApiBase = "http://10.0.2.2:8080/api/v1/public/local"

actual suspend fun loadLocalDashboard(): LocalDashboard = withContext(Dispatchers.IO) {
    requestJson("/dashboard").toDashboard()
}

actual suspend fun rechargeLocalWallet(amountCents: Long): LocalActionResult = withContext(Dispatchers.IO) {
    requestJson("/wallet/recharge", "POST", JSONObject().put("amountCents", amountCents)).toActionResult()
}

actual suspend fun withdrawLocalWallet(amountCents: Long): LocalActionResult = withContext(Dispatchers.IO) {
    requestJson("/wallet/withdraw", "POST", JSONObject().put("amountCents", amountCents)).toActionResult()
}

actual suspend fun placeLocalBet(gameCode: String, stakeCents: Long, content: String): LocalActionResult = withContext(Dispatchers.IO) {
    requestJson(
        "/orders",
        "POST",
        JSONObject().put("gameCode", gameCode).put("stakeCents", stakeCents).put("content", content),
    ).toActionResult()
}

actual suspend fun markLocalMessagesRead(): LocalDashboard = withContext(Dispatchers.IO) {
    requestJson("/messages/read", "POST", JSONObject()).toDashboard()
}

private fun requestJson(path: String, method: String = "GET", body: JSONObject? = null): JSONObject {
    val connection = (URL(LocalApiBase + path).openConnection() as HttpURLConnection).apply {
        requestMethod = method
        connectTimeout = 8_000
        readTimeout = 8_000
        setRequestProperty("Accept", "application/json")
        if (body != null) {
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
    }
    return try {
        if (body != null) {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
        }
        val success = connection.responseCode in 200..299
        val stream = if (success) connection.inputStream else connection.errorStream
        val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        val json = if (response.isBlank()) JSONObject() else JSONObject(response)
        check(success) { json.optString("message", "本地服务返回 ${connection.responseCode}") }
        json
    } finally {
        connection.disconnect()
    }
}

private fun JSONObject.toDashboard(): LocalDashboard = LocalDashboard(
    profile = getJSONObject("profile").toProfile(),
    orders = getJSONArray("orders").mapObjects { it.toOrder() },
    walletEntries = getJSONArray("walletEntries").mapObjects { it.toWalletEntry() },
    coupons = getJSONArray("coupons").mapObjects { it.toContentItem() },
    messages = getJSONArray("messages").mapObjects { it.toContentItem() },
    helpArticles = getJSONArray("helpArticles").mapObjects { it.toContentItem() },
)

private fun JSONObject.toActionResult(): LocalActionResult = LocalActionResult(
    message = getString("message"),
    profile = getJSONObject("profile").toProfile(),
    order = optJSONObject("order")?.toOrder(),
)

private fun JSONObject.toProfile(): LocalProfile {
    val counts = getJSONObject("orderCounts")
    return LocalProfile(
        userNo = getString("userNo"),
        nickname = getString("nickname"),
        kycStatus = getString("kycStatus"),
        levelCode = getString("levelCode"),
        growthPoints = getLong("growthPoints"),
        availableBalanceCents = getLong("availableBalanceCents"),
        prizeBalanceCents = getLong("prizeBalanceCents"),
        frozenBalanceCents = getLong("frozenBalanceCents"),
        totalPrizeCents = getLong("totalPrizeCents"),
        couponCount = getLong("couponCount"),
        unreadMessages = getLong("unreadMessages"),
        orderCounts = LocalOrderCounts(
            pendingPayment = counts.getLong("pendingPayment"),
            awaitingDraw = counts.getLong("awaitingDraw"),
            won = counts.getLong("won"),
            completed = counts.getLong("completed"),
            cancelled = counts.getLong("cancelled"),
        ),
    )
}

private fun JSONObject.toOrder(): LocalOrder = LocalOrder(
    orderNo = getString("orderNo"),
    gameName = getString("gameName"),
    status = getString("status"),
    stakeCents = getLong("stakeCents"),
    content = getString("content"),
    createdAt = getString("createdAt"),
)

private fun JSONObject.toWalletEntry(): LocalWalletEntry = LocalWalletEntry(
    transactionNo = getString("transactionNo"),
    businessType = getString("businessType"),
    amountCents = getLong("amountCents"),
    status = getString("status"),
    description = optString("description"),
    createdAt = getString("createdAt"),
)

private fun JSONObject.toContentItem(): LocalContentItem = LocalContentItem(
    code = getString("code"),
    title = getString("title"),
    subtitle = getString("subtitle"),
    status = getString("status"),
)

private inline fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }
