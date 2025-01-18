package me.elgregos.auctionstream.auction.infrastructure.event

import com.fasterxml.jackson.databind.JsonNode
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.reakteves.infrastructure.event.EventEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("auction_event")
data class AuctionEventEntity(
    @get:JvmName("id") val id: UUID,
    override val version: Int = 1,
    override val createdAt: LocalDateTime,
    override val createdBy: UUID,
    override val eventType: String,
    override val aggregateId: UUID,
    override val event: JsonNode
) : EventEntity<AuctionEvent, UUID, UUID>(
    id,
    version,
    createdAt,
    createdBy,
    eventType,
    aggregateId,
    event
)