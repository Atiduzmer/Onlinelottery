package com.onlinelottery.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OnlineLotteryServerApplication

fun main(args: Array<String>) {
    runApplication<OnlineLotteryServerApplication>(*args)
}
