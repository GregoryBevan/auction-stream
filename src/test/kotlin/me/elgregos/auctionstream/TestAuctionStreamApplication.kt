package me.elgregos.auctionstream

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<AuctionStreamApplication>().with(TestcontainersConfiguration::class).run(*args)
}
