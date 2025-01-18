package me.elgregos.auctionstream.auction.domain.entity

import java.util.UUID

data class Auctioneer(
    val id: UUID = UUID.randomUUID(),
    val name: String
)