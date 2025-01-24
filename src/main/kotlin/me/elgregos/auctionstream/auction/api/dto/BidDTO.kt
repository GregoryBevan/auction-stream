package me.elgregos.auctionstream.auction.api.dto

data class BidDTO(
    val bidder: String,
    val amount: Double,
)