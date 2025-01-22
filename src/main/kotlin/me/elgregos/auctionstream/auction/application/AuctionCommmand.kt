package me.elgregos.auctionstream.auction.application

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.application.Command
import java.util.*

sealed class AuctionCommand(open val auctionId: UUID) : Command {

    data class CreateAuction(
        val auction: Auction
    ) : AuctionCommand(auctionId = auction.id)

}