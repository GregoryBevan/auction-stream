package me.elgregos.auctionstream.auction.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.entity.auctioneer
import me.elgregos.reakteves.libs.nowUTC
import me.elgregos.reakteves.libs.uuidV7

data class AuctionDTO(
    @field:JsonProperty("item") val itemDTO: ItemDTO
) {
    fun toAuction(): Auction =
        Auction(
            id = uuidV7(),
            createdAt = nowUTC(),
            createdBy = auctioneer.id,
            item = itemDTO.toItem()
        )
}
