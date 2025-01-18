package me.elgregos.auctionstream

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuctionStreamApplication

fun main(args: Array<String>) {
	runApplication<AuctionStreamApplication>(*args)
}
