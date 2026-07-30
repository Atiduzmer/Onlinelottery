package com.onlinelottery.server

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.boot.builder.SpringApplicationBuilder
import java.io.File

fun main(args: Array<String>) {
    val postgres = EmbeddedPostgres.builder()
        .setOverrideWorkingDirectory(File("build/local-postgres-binaries"))
        .setLocaleConfig("locale", "C")
        .start()

    Runtime.getRuntime().addShutdownHook(Thread { postgres.close() })

    val localArgs = args + arrayOf(
        "--spring.datasource.url=${postgres.getJdbcUrl("postgres", "postgres")}",
        "--spring.datasource.username=postgres",
        "--spring.datasource.password=postgres",
        "--spring.datasource.hikari.maximum-pool-size=5",
        "--spring.flyway.enabled=true",
        "--server.port=8080",
    )

    val context = SpringApplicationBuilder(OnlineLotteryServerApplication::class.java)
        .run(*localArgs)

    println("LOCAL_SERVER_READY http://localhost:8080")
    context.registerShutdownHook()
}
