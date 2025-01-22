package me.elgregos.auctionstream.auction.domain.event

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent.AuctionCreated
import me.elgregos.reakteves.domain.event.EventStore
import me.elgregos.reakteves.domain.event.JsonAggregate
import reactor.core.publisher.Flux
import java.util.*

class AuctionAggregate(
    auctionId: UUID,
    auctionEventStore: EventStore<AuctionEvent, UUID, UUID>
) : JsonAggregate<AuctionEvent, UUID, UUID>(auctionId, auctionEventStore) {

    fun createAuction(auction: Auction): Flux<AuctionEvent> =
        Flux.just(AuctionCreated(auction))
}