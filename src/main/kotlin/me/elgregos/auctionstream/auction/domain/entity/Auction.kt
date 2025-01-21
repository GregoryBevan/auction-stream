package me.elgregos.auctionstream.auction.domain.entity

import me.elgregos.reakteves.domain.entity.DomainEntity
import me.elgregos.reakteves.libs.uuidV5
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

    fun start(startBy: UUID, startTime: LocalDateTime, version: Int): Auction = copy(
        updatedBy = startBy,
        updatedAt = startTime,
        version = version,
        startTime = startTime
    )

    fun placeBid(bid: Bid, version: Int): Auction = copy(
        updatedBy = uuidV5(bid.bidder),
        updatedAt = bid.placedAt,
        version = version,
        bids = bids.toMutableList().apply { add(bid) }.toList(),
    )

    fun bidStatus(amount: Double): BidStatus =
        // Bid could not be placed on ended auction
        if (endTime != null) BidStatus.REJECTED
        else bids.findLast { it.bidStatus == BidStatus.ACCEPTED }
            // Compare the amount on the last accepted bid
            ?.let { if (amount > it.amount) BidStatus.ACCEPTED else BidStatus.REJECTED }
            // Compare the amount to the starting price if no previous accepted bid
            ?: if (amount > item.startingPrice) BidStatus.ACCEPTED else BidStatus.REJECTED

    fun end(endedBy: UUID, endTime: LocalDateTime, version: Int): Auction = copy(
        updatedBy = endedBy,
        updatedAt = endTime,
        version = version,
        endTime = endTime
    )
}