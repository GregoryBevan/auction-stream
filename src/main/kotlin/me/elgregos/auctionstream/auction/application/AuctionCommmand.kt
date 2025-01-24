package me.elgregos.auctionstream.auction.application

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.entity.Auctioneer
import me.elgregos.auctionstream.auction.domain.entity.auctioneer
import me.elgregos.reakteves.application.Command
import me.elgregos.reakteves.libs.nowUTC
import java.time.LocalDateTime
import java.util.*

sealed class AuctionCommand(open val auctionId: UUID) : Command {

    data class CreateAuction(
        val auction: Auction
    ) : AuctionCommand(auctionId = auction.id)

    data class StartAuction(
        override val auctionId: UUID,
        val startedBy: Auctioneer = auctioneer,
        val startTime: LocalDateTime = nowUTC(),
    ) : AuctionCommand(auctionId = auctionId)

    data class PlaceBid(
        override val auctionId: UUID,
        val amount: Double,
        val bidder: String,
        val placedAt: LocalDateTime = nowUTC(),
    ) : AuctionCommand(auctionId = auctionId)

    data class EndAuction(
        override val auctionId: UUID,
        val endedBy: Auctioneer = auctioneer,
        val endTime: LocalDateTime = nowUTC(),
    ) : AuctionCommand(auctionId = auctionId)

}