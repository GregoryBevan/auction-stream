package me.elgregos.auctionstream.auction.application

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.domain.projection.ProjectionStore
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuctionProjectionService(
    private val auctionProjectionStore: ProjectionStore<Auction, UUID, UUID>) {

    fun auctions() =
        auctionProjectionStore.list()

    fun auction(auctionId: UUID) =
        auctionProjectionStore.find(auctionId)
}