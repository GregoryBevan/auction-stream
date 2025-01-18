package me.elgregos.auctionstream.auction.domain.entity

data class Item(
    val name: String,
    val description: String,
    val startingPrice: Double
)