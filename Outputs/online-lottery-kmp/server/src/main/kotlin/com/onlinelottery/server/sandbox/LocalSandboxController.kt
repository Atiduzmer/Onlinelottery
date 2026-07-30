package com.onlinelottery.server.sandbox

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.sql.ResultSet
import java.time.OffsetDateTime
import java.util.UUID

private const val LocalUserNo = "LOCAL-0001"

data class LocalOrderCounts(
    val pendingPayment: Long,
    val awaitingDraw: Long,
    val won: Long,
    val completed: Long,
    val cancelled: Long,
)

data class LocalProfileResponse(
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

data class LocalOrderSummary(
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
    val description: String?,
    val createdAt: String,
)

data class LocalContentItem(
    val code: String,
    val title: String,
    val subtitle: String,
    val status: String,
)

data class LocalDashboardResponse(
    val profile: LocalProfileResponse,
    val orders: List<LocalOrderSummary>,
    val walletEntries: List<LocalWalletEntry>,
    val coupons: List<LocalContentItem>,
    val messages: List<LocalContentItem>,
    val helpArticles: List<LocalContentItem>,
)

data class LocalMoneyRequest(
    @field:Min(100) @field:Max(10_000_000) val amountCents: Long,
)

data class LocalBetRequest(
    @field:NotBlank val gameCode: String,
    @field:Min(200) @field:Max(2_000_000) val stakeCents: Long,
    @field:NotBlank val content: String,
)

data class LocalActionResponse(
    val message: String,
    val profile: LocalProfileResponse,
    val order: LocalOrderSummary? = null,
)

private data class AccountRow(val id: Long, val balanceCents: Long)
private data class GameRow(val id: Long, val name: String, val category: String)

@RestController
@RequestMapping("/api/v1/public/local")
class LocalSandboxController(private val service: LocalSandboxService) {
    @GetMapping("/dashboard")
    fun dashboard(): LocalDashboardResponse = service.dashboard()

    @PostMapping("/wallet/recharge")
    fun recharge(@Valid @RequestBody request: LocalMoneyRequest): LocalActionResponse = service.recharge(request.amountCents)

    @PostMapping("/wallet/withdraw")
    fun withdraw(@Valid @RequestBody request: LocalMoneyRequest): LocalActionResponse = service.withdraw(request.amountCents)

    @PostMapping("/orders")
    fun placeOrder(@Valid @RequestBody request: LocalBetRequest): LocalActionResponse = service.placeOrder(request)

    @PostMapping("/messages/read")
    fun markMessagesRead(): LocalDashboardResponse = service.markMessagesRead()
}

@RestControllerAdvice(assignableTypes = [LocalSandboxController::class])
class LocalSandboxErrorHandler {
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgument(error: IllegalArgumentException): Map<String, String> =
        mapOf("message" to (error.message ?: "本地沙盒操作失败"))
}

@org.springframework.stereotype.Service
class LocalSandboxService(
    private val jdbc: JdbcClient,
) {
    private val objectMapper = ObjectMapper().findAndRegisterModules()
    fun dashboard(): LocalDashboardResponse = LocalDashboardResponse(
        profile = profile(),
        orders = orders(),
        walletEntries = walletEntries(),
        coupons = coupons(),
        messages = messages(),
        helpArticles = helpArticles(),
    )

    fun profile(): LocalProfileResponse {
        val userId = localUserId()
        val base = jdbc.sql(
            """
            SELECT u.user_no, u.nickname, u.kyc_status,
                   COALESCE(g.level_code, 'VIP0') AS level_code,
                   COALESCE(g.growth_points, 0) AS growth_points,
                   COALESCE(MAX(a.balance_cents) FILTER (WHERE a.account_type = 'AVAILABLE'), 0) AS available_balance,
                   COALESCE(MAX(a.balance_cents) FILTER (WHERE a.account_type = 'PRIZE'), 0) AS prize_balance,
                   COALESCE(SUM(a.balance_cents) FILTER (WHERE a.account_type IN ('BETTING_FROZEN', 'WITHDRAWAL_FROZEN')), 0) AS frozen_balance
            FROM users u
            LEFT JOIN user_growth_accounts g ON g.user_id = u.id
            LEFT JOIN wallet_accounts a ON a.owner_type = 'USER' AND a.owner_id = u.id
            WHERE u.id = :userId
            GROUP BY u.id, u.user_no, u.nickname, u.kyc_status, g.level_code, g.growth_points
            """.trimIndent(),
        ).param("userId", userId).query { rs, _ ->
            arrayOf(
                rs.getString("user_no"),
                rs.getString("nickname"),
                rs.getString("kyc_status"),
                rs.getString("level_code"),
                rs.getLong("growth_points"),
                rs.getLong("available_balance"),
                rs.getLong("prize_balance"),
                rs.getLong("frozen_balance"),
            )
        }.single()

        val totalPrize = scalarLong(
            "SELECT COALESCE(SUM(net_prize_cents), 0) FROM prize_records WHERE user_id = :userId AND status = 'CREDITED'",
            userId,
        )
        val couponCount = scalarLong(
            "SELECT COUNT(*) FROM user_coupons WHERE user_id = :userId AND status = 'AVAILABLE' AND expires_at > now()",
            userId,
        )
        val unreadMessages = scalarLong(
            "SELECT COUNT(*) FROM user_notifications WHERE user_id = :userId AND status <> 'READ'",
            userId,
        )

        return LocalProfileResponse(
            userNo = base[0] as String,
            nickname = base[1] as String,
            kycStatus = base[2] as String,
            levelCode = base[3] as String,
            growthPoints = base[4] as Long,
            availableBalanceCents = base[5] as Long,
            prizeBalanceCents = base[6] as Long,
            frozenBalanceCents = base[7] as Long,
            totalPrizeCents = totalPrize,
            couponCount = couponCount,
            unreadMessages = unreadMessages,
            orderCounts = orderCounts(userId),
        )
    }

    @Transactional
    fun recharge(amountCents: Long): LocalActionResponse {
        require(amountCents in 100..10_000_000) { "充值金额应在 1 元至 10 万元之间" }
        val userId = localUserId()
        val operationId = UUID.randomUUID().toString()
        postTransfer(
            businessType = "RECHARGE",
            businessId = operationId,
            amountCents = amountCents,
            source = account("PLATFORM", 1, "PAYMENT_PENDING"),
            destination = account("USER", userId, "AVAILABLE"),
            description = "本地沙盒充值",
        )
        jdbc.sql(
            """
            INSERT INTO recharge_orders
                (recharge_no, user_id, provider, provider_order_no, amount_cents, status, client_request_id, paid_at, credited_at)
            VALUES (:number, :userId, 'LOCAL_SANDBOX', :providerNo, :amount, 'CREDITED', :requestId, now(), now())
            """.trimIndent(),
        ).params(
            mapOf(
                "number" to "R-${operationId.take(12)}",
                "userId" to userId,
                "providerNo" to "LOCAL-${operationId.takeLast(12)}",
                "amount" to amountCents,
                "requestId" to operationId,
            ),
        ).update()
        return LocalActionResponse("本地测试余额已充值", profile())
    }

    @Transactional
    fun withdraw(amountCents: Long): LocalActionResponse {
        require(amountCents in 100..10_000_000) { "提现金额应在 1 元至 10 万元之间" }
        val userId = localUserId()
        val available = account("USER", userId, "AVAILABLE")
        require(available.balanceCents >= amountCents) { "可用余额不足" }
        val frozen = account("USER", userId, "WITHDRAWAL_FROZEN")
        val operationId = UUID.randomUUID().toString()
        postTransfer("WITHDRAW_FREEZE", operationId, amountCents, available, frozen, "本地沙盒提现冻结")
        postTransfer(
            "WITHDRAW_CAPTURE",
            operationId,
            amountCents,
            account("USER", userId, "WITHDRAWAL_FROZEN"),
            account("PLATFORM", 1, "CHANNEL_CLEARING"),
            "本地沙盒提现完成",
        )
        val bankCardId = jdbc.sql("SELECT id FROM user_bank_cards WHERE user_id = :userId AND is_default LIMIT 1")
            .param("userId", userId).query(Long::class.java).single()
        jdbc.sql(
            """
            INSERT INTO withdrawal_orders
                (withdrawal_no, user_id, bank_card_id, amount_cents, status, client_request_id, provider_order_no, completed_at)
            VALUES (:number, :userId, :cardId, :amount, 'PAID', :requestId, :providerNo, now())
            """.trimIndent(),
        ).params(
            mapOf(
                "number" to "W-${operationId.take(12)}",
                "userId" to userId,
                "cardId" to bankCardId,
                "amount" to amountCents,
                "requestId" to operationId,
                "providerNo" to "LOCAL-${operationId.takeLast(12)}",
            ),
        ).update()
        return LocalActionResponse("本地沙盒提现成功", profile())
    }

    @Transactional
    fun placeOrder(request: LocalBetRequest): LocalActionResponse {
        require(request.stakeCents in 200..2_000_000) { "投注金额应在 2 元至 2 万元之间" }
        val content = objectMapper.readTree(request.content).toString()
        val userId = localUserId()
        val available = account("USER", userId, "AVAILABLE")
        require(available.balanceCents >= request.stakeCents) { "可用余额不足，请先充值" }
        val game = jdbc.sql("SELECT id, name, category FROM lottery_games WHERE code = :code AND status = 'ACTIVE'")
            .param("code", request.gameCode).query { rs, _ ->
                GameRow(rs.getLong("id"), rs.getString("name"), rs.getString("category"))
            }.single()
        val playTypeId = jdbc.sql("SELECT id FROM lottery_play_types WHERE game_id = :gameId AND status = 'ACTIVE' ORDER BY id LIMIT 1")
            .param("gameId", game.id).query(Long::class.java).single()
        val operationId = UUID.randomUUID().toString()
        val orderNo = "B-${operationId.take(16)}"

        postTransfer(
            "BET_FREEZE",
            orderNo,
            request.stakeCents,
            available,
            account("USER", userId, "BETTING_FROZEN"),
            "本地订单 $orderNo 投注冻结",
        )
        postTransfer(
            "BET_CAPTURE",
            orderNo,
            request.stakeCents,
            account("USER", userId, "BETTING_FROZEN"),
            account("PLATFORM", 1, "LOTTERY_SALES"),
            "本地订单 $orderNo 沙盒出票",
        )

        val orderId = jdbc.sql(
            """
            INSERT INTO bet_orders
                (order_no, user_id, game_id, order_type, status, total_stake_cents, currency, client_request_id, submitted_at)
            VALUES
                (:orderNo, :userId, :gameId, :orderType, 'TICKETED', :stake, 'CNY', :requestId, now())
            RETURNING id
            """.trimIndent(),
        ).params(
            mapOf(
                "orderNo" to orderNo,
                "userId" to userId,
                "gameId" to game.id,
                "orderType" to if (game.category == "SPORTS") "SPORTS_SINGLE" else "NUMBER_SINGLE",
                "stake" to request.stakeCents,
                "requestId" to operationId,
            ),
        ).query(Long::class.java).single()

        val itemId = jdbc.sql(
            """
            INSERT INTO bet_order_items
                (order_id, play_type_id, item_no, units, multiple, stake_cents, content, status)
            VALUES (:orderId, :playTypeId, 1, 1, 1, :stake, CAST(:content AS jsonb), 'TICKETED')
            RETURNING id
            """.trimIndent(),
        ).params(
            mapOf("orderId" to orderId, "playTypeId" to playTypeId, "stake" to request.stakeCents, "content" to content),
        ).query(Long::class.java).single()

        val ticketOrderId = jdbc.sql(
            """
            INSERT INTO ticket_orders
                (order_id, ticket_order_no, channel, status, amount_cents, idempotency_key, attempt_count, submitted_at, completed_at)
            VALUES (:orderId, :ticketNo, 'LOCAL_SANDBOX', 'SUCCESS', :stake, :key, 1, now(), now())
            RETURNING id
            """.trimIndent(),
        ).params(
            mapOf(
                "orderId" to orderId,
                "ticketNo" to "TO-${operationId.take(16)}",
                "stake" to request.stakeCents,
                "key" to "LOCAL-TICKET:$orderNo",
            ),
        ).query(Long::class.java).single()

        jdbc.sql(
            """
            INSERT INTO official_tickets
                (ticket_order_id, order_item_id, official_ticket_no, status, stake_cents, ticket_payload, issued_at)
            VALUES (:ticketOrderId, :itemId, :ticketNo, 'ISSUED', :stake, '{"sandbox":true}'::jsonb, now())
            """.trimIndent(),
        ).params(
            mapOf(
                "ticketOrderId" to ticketOrderId,
                "itemId" to itemId,
                "ticketNo" to "LOCAL-${operationId.take(20)}",
                "stake" to request.stakeCents,
            ),
        ).update()

        listOf(
            null to "CREATED",
            "CREATED" to "PAYMENT_PENDING",
            "PAYMENT_PENDING" to "PAID",
            "PAID" to "TICKETING",
            "TICKETING" to "TICKETED",
        ).forEach { (from, to) ->
            jdbc.sql(
                """
                INSERT INTO order_status_histories (order_id, from_status, to_status, reason_code, reason_message)
                VALUES (:orderId, :fromStatus, :toStatus, 'LOCAL_SANDBOX', '本地沙盒自动处理')
                """.trimIndent(),
            ).params(mapOf("orderId" to orderId, "fromStatus" to from, "toStatus" to to)).update()
        }

        val order = orderById(orderId)
        return LocalActionResponse("本地测试票已生成，可在投注记录查看", profile(), order)
    }

    @Transactional
    fun markMessagesRead(): LocalDashboardResponse {
        jdbc.sql("UPDATE user_notifications SET status = 'READ', read_at = now() WHERE user_id = :userId AND status <> 'READ'")
            .param("userId", localUserId()).update()
        return dashboard()
    }

    private fun orders(): List<LocalOrderSummary> = jdbc.sql(
        """
        SELECT o.order_no, g.name AS game_name, o.status, o.total_stake_cents,
               i.content::text AS content, o.created_at
        FROM bet_orders o
        JOIN lottery_games g ON g.id = o.game_id
        LEFT JOIN bet_order_items i ON i.order_id = o.id AND i.item_no = 1
        WHERE o.user_id = :userId
        ORDER BY o.created_at DESC
        LIMIT 50
        """.trimIndent(),
    ).param("userId", localUserId()).query(::mapOrder).list()

    private fun orderById(orderId: Long): LocalOrderSummary = jdbc.sql(
        """
        SELECT o.order_no, g.name AS game_name, o.status, o.total_stake_cents,
               i.content::text AS content, o.created_at
        FROM bet_orders o
        JOIN lottery_games g ON g.id = o.game_id
        LEFT JOIN bet_order_items i ON i.order_id = o.id AND i.item_no = 1
        WHERE o.id = :orderId
        """.trimIndent(),
    ).param("orderId", orderId).query(::mapOrder).single()

    private fun mapOrder(rs: ResultSet, row: Int): LocalOrderSummary = LocalOrderSummary(
        orderNo = rs.getString("order_no"),
        gameName = rs.getString("game_name"),
        status = rs.getString("status"),
        stakeCents = rs.getLong("total_stake_cents"),
        content = rs.getString("content") ?: "{}",
        createdAt = rs.getObject("created_at", OffsetDateTime::class.java).toString(),
    )

    private fun walletEntries(): List<LocalWalletEntry> = jdbc.sql(
        """
        SELECT t.transaction_no, t.business_type, t.total_cents, t.status, t.description, t.created_at
        FROM wallet_transactions t
        WHERE EXISTS (
            SELECT 1 FROM accounting_entries e
            JOIN wallet_accounts a ON a.id = e.account_id
            WHERE e.transaction_id = t.id AND a.owner_type = 'USER' AND a.owner_id = :userId
        )
        ORDER BY t.created_at DESC
        LIMIT 50
        """.trimIndent(),
    ).param("userId", localUserId()).query { rs, _ ->
        LocalWalletEntry(
            rs.getString("transaction_no"),
            rs.getString("business_type"),
            rs.getLong("total_cents"),
            rs.getString("status"),
            rs.getString("description"),
            rs.getObject("created_at", OffsetDateTime::class.java).toString(),
        )
    }.list()

    private fun coupons(): List<LocalContentItem> = jdbc.sql(
        """
        SELECT c.coupon_no, t.name, c.expires_at, c.status
        FROM user_coupons c JOIN coupon_templates t ON t.id = c.template_id
        WHERE c.user_id = :userId ORDER BY c.expires_at
        """.trimIndent(),
    ).param("userId", localUserId()).query { rs, _ ->
        LocalContentItem(
            rs.getString("coupon_no"),
            rs.getString("name"),
            "有效期至 ${rs.getObject("expires_at", OffsetDateTime::class.java).toLocalDate()}",
            rs.getString("status"),
        )
    }.list()

    private fun messages(): List<LocalContentItem> = jdbc.sql(
        """
        SELECT n.template_code, n.title, n.content, un.status
        FROM user_notifications un JOIN notifications n ON n.id = un.notification_id
        WHERE un.user_id = :userId ORDER BY un.created_at DESC
        """.trimIndent(),
    ).param("userId", localUserId()).query { rs, _ ->
        LocalContentItem(rs.getString("template_code"), rs.getString("title"), rs.getString("content"), rs.getString("status"))
    }.list()

    private fun helpArticles(): List<LocalContentItem> = jdbc.sql(
        """
        SELECT slug, title, summary, status FROM help_articles
        WHERE status = 'PUBLISHED' ORDER BY display_order
        """.trimIndent(),
    ).query { rs, _ ->
        LocalContentItem(rs.getString("slug"), rs.getString("title"), rs.getString("summary") ?: "", rs.getString("status"))
    }.list()

    private fun orderCounts(userId: Long): LocalOrderCounts = jdbc.sql(
        """
        SELECT
            COUNT(*) FILTER (WHERE status IN ('CREATED', 'PAYMENT_PENDING')) AS pending_payment,
            COUNT(*) FILTER (WHERE status IN ('PAID', 'TICKETING', 'TICKETED', 'PARTIAL_TICKETED', 'SETTLING')) AS awaiting_draw,
            COUNT(*) FILTER (WHERE status = 'SETTLED' AND potential_payout_cents > 0) AS won,
            COUNT(*) FILTER (WHERE status = 'SETTLED') AS completed,
            COUNT(*) FILTER (WHERE status IN ('FAILED', 'CANCELLED', 'REFUNDED')) AS cancelled
        FROM bet_orders WHERE user_id = :userId
        """.trimIndent(),
    ).param("userId", userId).query { rs, _ ->
        LocalOrderCounts(
            rs.getLong("pending_payment"),
            rs.getLong("awaiting_draw"),
            rs.getLong("won"),
            rs.getLong("completed"),
            rs.getLong("cancelled"),
        )
    }.single()

    private fun postTransfer(
        businessType: String,
        businessId: String,
        amountCents: Long,
        source: AccountRow,
        destination: AccountRow,
        description: String,
    ) {
        require(source.balanceCents >= amountCents || businessType == "RECHARGE") { "账户余额不足" }
        val transactionNo = "TX-${UUID.randomUUID().toString().take(20)}"
        val sourceAfter = source.balanceCents - amountCents
        val destinationAfter = destination.balanceCents + amountCents
        val transactionId = jdbc.sql(
            """
            INSERT INTO wallet_transactions
                (transaction_no, business_type, business_id, idempotency_key, status, total_cents, description)
            VALUES (:number, :type, :businessId, :key, 'PENDING', :amount, :description)
            RETURNING id
            """.trimIndent(),
        ).params(
            mapOf(
                "number" to transactionNo,
                "type" to businessType,
                "businessId" to businessId,
                "key" to "$businessType:$businessId",
                "amount" to amountCents,
                "description" to description,
            ),
        ).query(Long::class.java).single()
        jdbc.sql("UPDATE wallet_accounts SET balance_cents = :balance, version = version + 1, updated_at = now() WHERE id = :id")
            .params(mapOf("balance" to sourceAfter, "id" to source.id)).update()
        jdbc.sql("UPDATE wallet_accounts SET balance_cents = :balance, version = version + 1, updated_at = now() WHERE id = :id")
            .params(mapOf("balance" to destinationAfter, "id" to destination.id)).update()
        jdbc.sql(
            """
            INSERT INTO accounting_entries
                (transaction_id, account_id, line_no, direction, amount_cents, balance_after_cents)
            VALUES
                (:transactionId, :sourceId, 1, 'DEBIT', :amount, :sourceAfter),
                (:transactionId, :destinationId, 2, 'CREDIT', :amount, :destinationAfter)
            """.trimIndent(),
        ).params(
            mapOf(
                "transactionId" to transactionId,
                "sourceId" to source.id,
                "destinationId" to destination.id,
                "amount" to amountCents,
                "sourceAfter" to sourceAfter,
                "destinationAfter" to destinationAfter,
            ),
        ).update()
        jdbc.sql("UPDATE wallet_transactions SET status = 'POSTED', updated_at = now() WHERE id = :id")
            .param("id", transactionId).update()
    }

    private fun account(ownerType: String, ownerId: Long, accountType: String): AccountRow = jdbc.sql(
        """
        SELECT id, balance_cents FROM wallet_accounts
        WHERE owner_type = :ownerType AND owner_id = :ownerId AND account_type = :accountType
        FOR UPDATE
        """.trimIndent(),
    ).params(mapOf("ownerType" to ownerType, "ownerId" to ownerId, "accountType" to accountType))
        .query { rs, _ -> AccountRow(rs.getLong("id"), rs.getLong("balance_cents")) }.single()

    private fun localUserId(): Long = jdbc.sql("SELECT id FROM users WHERE user_no = :userNo")
        .param("userNo", LocalUserNo).query(Long::class.java).single()

    private fun scalarLong(sql: String, userId: Long): Long = jdbc.sql(sql)
        .param("userId", userId).query(Long::class.java).single()
}
