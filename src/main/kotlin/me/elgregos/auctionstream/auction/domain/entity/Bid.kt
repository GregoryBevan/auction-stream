package me.elgregos.auctionstream.auction.domain.entity

import java.time.LocalDateTime

data class Bid(
    val bidder: String,
    val amount: Double,
    val placedAt: LocalDateTime,
    val bidStatus: BidStatus
)