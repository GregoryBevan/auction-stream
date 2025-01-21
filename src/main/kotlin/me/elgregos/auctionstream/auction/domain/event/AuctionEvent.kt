package me.elgregos.auctionstream.auction.domain.event

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
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

    data class AuctionStarted(
        override val id: UUID = uuidV7(),
        override val version: Int,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, AuctionStarted::class.simpleName!!, event) {

        constructor(updatedAuction: Auction) : this(
            auctionId = updatedAuction.id,
            createdAt = updatedAuction.updatedAt,
            createdBy = updatedAuction.updatedBy,
            version = updatedAuction.version,
            event = genericObjectMapper.createObjectNode()
                .put("id", "${updatedAuction.id}")
                .put("updatedBy", "${updatedAuction.updatedBy}")
                .put("updatedAt", "${updatedAuction.updatedAt}")
                .put("version", "${updatedAuction.version}")
                .put("startTime", "${updatedAuction.startTime}")
        )
    }

    data class BidPlaced(
        override val id: UUID = uuidV7(),
        override val version: Int,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, BidPlaced::class.simpleName!!, event) {

        constructor(updatedAuction: Auction) : this(
            auctionId = updatedAuction.id,
            createdAt = updatedAuction.updatedAt,
            createdBy = updatedAuction.updatedBy,
            version = updatedAuction.version,
            event = genericObjectMapper.createObjectNode()
                .put("id", "${updatedAuction.id}")
                .put("updatedBy", "${updatedAuction.updatedBy}")
                .put("updatedAt", "${updatedAuction.updatedAt}")
                .put("version", "${updatedAuction.version}")
                .set<ObjectNode>("bids", genericObjectMapper.createArrayNode().apply {
                    addAll(updatedAuction.bids.map { genericObjectMapper.valueToTree<JsonNode>(it) })
                })
        )
    }

    data class AuctionEnded(
        override val id: UUID = UUID.randomUUID(),
        override val version: Int,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, AuctionEnded::class.simpleName!!, event) {

        constructor(updatedAuction: Auction) : this(
            auctionId = updatedAuction.id,
            createdAt = updatedAuction.updatedAt,
            createdBy = updatedAuction.updatedBy,
            version = updatedAuction.version,
            event = genericObjectMapper.createObjectNode()
                .put("id", "${updatedAuction.id}")
                .put("updatedBy", "${updatedAuction.updatedBy}")
                .put("updatedAt", "${updatedAuction.updatedAt}")
                .put("version", "${updatedAuction.version}")
                .put("endTime", "${updatedAuction.endTime}")
        )
    }
}