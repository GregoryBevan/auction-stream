package me.elgregos.auctionstream.auction.infrastructure.projection

import com.fasterxml.jackson.databind.JsonNode
import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.infrastructure.projection.ProjectionEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("auction")
data class AuctionEntity(
    @get:JvmName("id") val id: UUID,
    override val version: Int,
    override val createdAt: LocalDateTime,
    override val createdBy: UUID,
    override val updatedAt: LocalDateTime,
    override val updatedBy: UUID,
    override val details: JsonNode
) : ProjectionEntity<Auction, UUID, UUID>(id, version, createdAt, createdBy, updatedAt, updatedBy, details)