package com.onlinelottery.shared.model

data class LocalOrderCounts(
    val pendingPayment: Long,
    val awaitingDraw: Long,
    val won: Long,
    val completed: Long,
    val cancelled: Long,
)

data class LocalProfile(
    val userNo: String,
    val nickname: String,
    val kycStatus: String,
    val levelCode: String,
    val growthPoints: Long,
    val availableBalanceCents: Long,
    val prizeBalanceCents: Long,
    val frozenBalanceCents: Long,
    val totalPrizeCents: Long,
    val couponCount: Long,
    val unreadMessages: Long,
    val orderCounts: LocalOrderCounts,
)

data class LocalOrder(
    val orderNo: String,
    val gameName: String,
    val status: String,
    val stakeCents: Long,
    val content: String,
    val createdAt: String,
)

data class LocalWalletEntry(
    val transactionNo: String,
    val businessType: String,
    val amountCents: Long,
    val status: String,
    val description: String,
    val createdAt: String,
)

data class LocalContentItem(
    val code: String,
    val title: String,
    val subtitle: String,
    val status: String,
)

data class LocalDashboard(
    val profile: LocalProfile,
    val orders: List<LocalOrder>,
    val walletEntries: List<LocalWalletEntry>,
    val coupons: List<LocalContentItem>,
    val messages: List<LocalContentItem>,
    val helpArticles: List<LocalContentItem>,
)

data class LocalActionResult(
    val message: String,
    val profile: LocalProfile,
    val order: LocalOrder?,
)

expect suspend fun loadLocalDashboard(): LocalDashboard
expect suspend fun rechargeLocalWallet(amountCents: Long): LocalActionResult
expect suspend fun withdrawLocalWallet(amountCents: Long): LocalActionResult
expect suspend fun placeLocalBet(gameCode: String, stakeCents: Long, content: String): LocalActionResult
expect suspend fun markLocalMessagesRead(): LocalDashboard
