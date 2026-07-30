package com.onlinelottery.server.domain

enum class BetOrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    TICKETING,
    TICKETED,
    PARTIAL_TICKETED,
    FAILED,
    CANCELLED,
    SETTLING,
    SETTLED,
    REFUNDED,
}

object BetOrderStateMachine {
    private val transitions = mapOf(
        BetOrderStatus.CREATED to setOf(BetOrderStatus.PAYMENT_PENDING, BetOrderStatus.CANCELLED),
        BetOrderStatus.PAYMENT_PENDING to setOf(BetOrderStatus.PAID, BetOrderStatus.FAILED, BetOrderStatus.CANCELLED),
        BetOrderStatus.PAID to setOf(BetOrderStatus.TICKETING, BetOrderStatus.REFUNDED),
        BetOrderStatus.TICKETING to setOf(
            BetOrderStatus.TICKETED,
            BetOrderStatus.PARTIAL_TICKETED,
            BetOrderStatus.FAILED,
            BetOrderStatus.REFUNDED,
        ),
        BetOrderStatus.TICKETED to setOf(BetOrderStatus.SETTLING, BetOrderStatus.REFUNDED),
        BetOrderStatus.PARTIAL_TICKETED to setOf(BetOrderStatus.SETTLING, BetOrderStatus.REFUNDED),
        BetOrderStatus.SETTLING to setOf(BetOrderStatus.SETTLED, BetOrderStatus.REFUNDED),
    )

    fun canTransition(from: BetOrderStatus, to: BetOrderStatus): Boolean =
        transitions[from]?.contains(to) == true

    fun requireTransition(from: BetOrderStatus, to: BetOrderStatus) {
        require(canTransition(from, to)) { "Illegal bet order transition: $from -> $to" }
    }
}
