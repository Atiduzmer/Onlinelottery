package com.onlinelottery.server.catalog

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LotteryGameResponse(
    val code: String,
    val name: String,
    val category: String,
    val sportType: String?,
    val salesEnabled: Boolean,
    val displayOrder: Int,
)

@Repository
class LotteryCatalogRepository(private val jdbcClient: JdbcClient) {
    fun findActiveGames(): List<LotteryGameResponse> = jdbcClient.sql(
        """
        SELECT code, name, category, sport_type, sales_enabled, display_order
        FROM lottery_games
        WHERE status = 'ACTIVE'
        ORDER BY display_order, id
        """.trimIndent(),
    ).query { rs, _ ->
        LotteryGameResponse(
            code = rs.getString("code"),
            name = rs.getString("name"),
            category = rs.getString("category"),
            sportType = rs.getString("sport_type"),
            salesEnabled = rs.getBoolean("sales_enabled"),
            displayOrder = rs.getInt("display_order"),
        )
    }.list()
}

@Service
class LotteryCatalogService(private val repository: LotteryCatalogRepository) {
    fun listGames(): List<LotteryGameResponse> = repository.findActiveGames()
}

@RestController
@RequestMapping("/api/v1/public/lottery-games")
class LotteryCatalogController(private val service: LotteryCatalogService) {
    @GetMapping
    fun listGames(): List<LotteryGameResponse> = service.listGames()
}
