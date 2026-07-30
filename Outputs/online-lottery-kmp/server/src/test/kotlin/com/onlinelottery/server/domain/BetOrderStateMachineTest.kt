package com.onlinelottery.server.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BetOrderStateMachineTest {
    @Test
    fun `paid order may enter ticketing`() {
        assertTrue(BetOrderStateMachine.canTransition(BetOrderStatus.PAID, BetOrderStatus.TICKETING))
    }

    @Test
    fun `settled order is terminal`() {
        assertFalse(BetOrderStateMachine.canTransition(BetOrderStatus.SETTLED, BetOrderStatus.PAID))
        assertThrows(IllegalArgumentException::class.java) {
            BetOrderStateMachine.requireTransition(BetOrderStatus.SETTLED, BetOrderStatus.PAID)
        }
    }

    @Test
    fun `ticket failure cannot pretend to be settled`() {
        assertFalse(BetOrderStateMachine.canTransition(BetOrderStatus.FAILED, BetOrderStatus.SETTLED))
    }
}
