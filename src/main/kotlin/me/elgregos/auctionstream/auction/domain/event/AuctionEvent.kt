package me.elgregos.auctionstream.auction.domain.event

import com.fasterxml.jackson.databind.JsonNode
import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.domain.event.Event
import me.elgregos.reakteves.libs.genericObjectMapper
import me.elgregos.reakteves.libs.nowUTC
import me.elgregos.reakteves.libs.uuidV7
import java.time.LocalDateTime
import java.util.*

sealed class AuctionEvent(
    id: UUID,
    version: Int,
    createdAt: LocalDateTime,
    createdBy: UUID,
    aggregateId: UUID,
    eventType: String,
    event: JsonNode
) : Event<UUID, UUID>(
    id, version, createdAt, createdBy, eventType, aggregateId, event
) {

    data class AuctionCreated(
        override val id: UUID,
        override val version: Int = 1,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(
        id,
        version,
        createdAt,
        createdBy,
        auctionId,
        AuctionCreated::class.simpleName!!,
        event
    ) {
        constructor(auction: Auction) : this(
            id = uuidV7(),
            auctionId = auction.id,
            createdAt = auction.createdAt,
            createdBy = auction.createdBy,
            event = genericObjectMapper.readTree(genericObjectMapper.writeValueAsString(auction))
        )
    }
}