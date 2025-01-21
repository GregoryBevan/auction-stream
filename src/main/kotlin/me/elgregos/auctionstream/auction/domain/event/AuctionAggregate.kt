package me.elgregos.auctionstream.auction.domain.event

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.entity.Auctioneer
import me.elgregos.auctionstream.auction.domain.entity.Bid
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent.AuctionCreated
import me.elgregos.reakteves.domain.JsonConvertible
import me.elgregos.reakteves.domain.event.EventStore
import me.elgregos.reakteves.domain.event.JsonAggregate
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*

class AuctionAggregate(
    private val auctionId: UUID,
    auctionEventStore: EventStore<AuctionEvent, UUID, UUID>
) : JsonAggregate<AuctionEvent, UUID, UUID>(auctionId, auctionEventStore) {

    fun createAuction(auction: Auction): Flux<AuctionEvent> =
        Flux.just(AuctionCreated(auction))

    fun startAuction(startedBy: Auctioneer, startTime: LocalDateTime): Flux<AuctionEvent> =
        retrieveAuctionAndVersion()
            // Create the AuctionStarted event
            .flatMapMany { Flux.just(AuctionEvent.AuctionStarted(it.t1.start(startedBy.id, startTime, it.t2))) }

    fun placeBid(bidder: String, amount: Double, placedAt: LocalDateTime): Flux<AuctionEvent> =
        retrieveAuctionAndVersion()
            // Place the bid
            .map { it.t1.placeBid(Bid(bidder, amount, placedAt, it.t1.bidStatus(amount)), it.t2) }
            // Create the BidPlaced event
            .flatMapMany { Flux.just(AuctionEvent.BidPlaced(it)) }

    fun endAuction(endedBy: Auctioneer, endedTime: LocalDateTime): Flux<AuctionEvent> =
        retrieveAuctionAndVersion()
            // Check if auction is already started and not yet ended
            .filter { it.t1.startTime != null && it.t1.endTime == null }
            .switchIfEmpty(Mono.error { Exception("Auction with id $auctionId can't be ended") })
            // Create the AuctionStarted event
            .flatMapMany { Flux.just(AuctionEvent.AuctionEnded(it.t1.end(endedBy.id, endedTime, it.t2))) }

    private fun retrieveAuctionAndVersion() =
        previousState()
            // Check the auction exists
            .filter { !it.isEmpty }
            .switchIfEmpty(Mono.error { Exception("Auction with id $auctionId not found") })
            // Convert the JSON to Auction object
            .map { JsonConvertible.fromJson<Auction>(it) }
            // Pair with the next version of the auction
            .zipWith(nextVersion())
}