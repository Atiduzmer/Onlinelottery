package com.onlinelottery.server.domain

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import com.onlinelottery.server.catalog.LotteryCatalogRepository
import com.onlinelottery.server.sandbox.LocalBetRequest
import com.onlinelottery.server.sandbox.LocalSandboxService
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.jdbc.core.simple.JdbcClient
import java.io.File
import java.sql.SQLException

class DatabaseMigrationTest {
    @Test
    fun `migrations create catalog and enforce balanced immutable ledger`() {
        EmbeddedPostgres.builder()
            .setOverrideWorkingDirectory(File("build/embedded-postgres"))
            .setLocaleConfig("locale", "C")
            .start()
            .use { postgres ->
            val dataSource = postgres.postgresDatabase
            val result = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load()
                .migrate()

            assertEquals(3, result.migrationsExecuted)
            val games = LotteryCatalogRepository(JdbcClient.create(dataSource)).findActiveGames()
            assertEquals(
                listOf("FOOTBALL", "BASKETBALL", "SUPER_LOTTO", "PICK_3", "PICK_5", "SEVEN_STAR"),
                games.map { it.code },
            )

            dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT COUNT(*) FROM lottery_games").use { rows ->
                        rows.next()
                        assertEquals(6, rows.getInt(1))
                    }

                    statement.executeUpdate(
                        """
                        INSERT INTO wallet_accounts
                            (owner_type, owner_id, account_code, account_type, balance_cents)
                        VALUES
                            ('PLATFORM', 1, 'CASH', 'PAYMENT_PENDING', 0),
                            ('USER', 1001, 'AVAILABLE', 'AVAILABLE', 100)
                        """.trimIndent(),
                    )
                    statement.executeUpdate(
                        """
                        INSERT INTO wallet_transactions
                            (transaction_no, business_type, business_id, idempotency_key, total_cents)
                        VALUES ('TX-VALID', 'RECHARGE', 'R-1', 'RECHARGE:R-1', 100)
                        """.trimIndent(),
                    )
                    statement.executeUpdate(
                        """
                        INSERT INTO accounting_entries
                            (transaction_id, account_id, line_no, direction, amount_cents, balance_after_cents)
                        SELECT t.id, a.id, 1, 'DEBIT', 100, 0
                        FROM wallet_transactions t, wallet_accounts a
                        WHERE t.transaction_no = 'TX-VALID' AND a.account_code = 'CASH'
                        UNION ALL
                        SELECT t.id, a.id, 2, 'CREDIT', 100, 100
                        FROM wallet_transactions t, wallet_accounts a
                        WHERE t.transaction_no = 'TX-VALID' AND a.account_code = 'AVAILABLE' AND a.owner_id = 1001
                        """.trimIndent(),
                    )
                    assertEquals(
                        1,
                        statement.executeUpdate("UPDATE wallet_transactions SET status = 'POSTED' WHERE transaction_no = 'TX-VALID'"),
                    )

                    assertThrows(SQLException::class.java) {
                        statement.executeUpdate(
                            """
                            UPDATE accounting_entries SET amount_cents = 99
                            WHERE transaction_id = (SELECT id FROM wallet_transactions WHERE transaction_no = 'TX-VALID')
                            """.trimIndent(),
                        )
                    }

                    statement.executeUpdate(
                        """
                        INSERT INTO wallet_transactions
                            (transaction_no, business_type, business_id, idempotency_key, total_cents)
                        VALUES ('TX-INVALID', 'ADJUSTMENT', 'A-1', 'ADJUSTMENT:A-1', 100)
                        """.trimIndent(),
                    )
                    statement.executeUpdate(
                        """
                        INSERT INTO accounting_entries
                            (transaction_id, account_id, line_no, direction, amount_cents, balance_after_cents)
                        SELECT t.id, a.id, 1, 'DEBIT', 100, 0
                        FROM wallet_transactions t, wallet_accounts a
                        WHERE t.transaction_no = 'TX-INVALID' AND a.account_code = 'CASH'
                        UNION ALL
                        SELECT t.id, a.id, 2, 'CREDIT', 90, 90
                        FROM wallet_transactions t, wallet_accounts a
                        WHERE t.transaction_no = 'TX-INVALID' AND a.account_code = 'AVAILABLE' AND a.owner_id = 1001
                        """.trimIndent(),
                    )
                    assertThrows(SQLException::class.java) {
                        statement.executeUpdate("UPDATE wallet_transactions SET status = 'POSTED' WHERE transaction_no = 'TX-INVALID'")
                    }
                }
            }
        }
    }

    @Test
    fun `local sandbox recharge bet and withdrawal change persistent dashboard data`() {
        EmbeddedPostgres.builder()
            .setOverrideWorkingDirectory(File("build/embedded-postgres"))
            .setLocaleConfig("locale", "C")
            .start()
            .use { postgres ->
                val dataSource = postgres.postgresDatabase
                Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate()
                val service = LocalSandboxService(JdbcClient.create(dataSource))
                val initial = service.dashboard()

                service.recharge(10_000)
                val order = service.placeOrder(LocalBetRequest("PICK_3", 200, "{\"zones\":[[1],[2],[3]]}"))
                service.withdraw(100)
                val updated = service.dashboard()

                assertEquals(initial.profile.availableBalanceCents + 9_700, updated.profile.availableBalanceCents)
                assertEquals(1, updated.orders.size)
                assertEquals("TICKETED", order.order?.status)
                assertEquals(5, updated.walletEntries.size)
                assertEquals(1, updated.profile.orderCounts.awaitingDraw)
            }
    }
}
