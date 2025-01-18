package me.elgregos.auctionstream.auction.domain.entity

import me.elgregos.reakteves.domain.entity.DomainEntity
import java.time.LocalDateTime
import java.util.*

val auctioneer = Auctioneer(name = "Sotheby's")

data class Auction(
    override val id: UUID,
    override val version: Int = 1,
    override val createdAt: LocalDateTime,
    override val createdBy: UUID,
    override val updatedAt: LocalDateTime = createdAt,
    override val updatedBy: UUID = createdBy,
    val item: Item,
    val bids: List<Bid> = mutableListOf(),
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
) : DomainEntity<UUID, UUID> {

}