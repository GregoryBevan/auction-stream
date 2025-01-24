package me.elgregos.auctionstream.auction.api.dto

import me.elgregos.auctionstream.auction.domain.entity.Item

data class ItemDTO(
    val name: String,
    val description: String,
    val startingPrice: Double
) {
    fun toItem(): Item = Item(
        name = name,
        description = description,
        startingPrice = startingPrice
    )

}