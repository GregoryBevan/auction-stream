package me.elgregos.auctionstream.auction.application

import me.elgregos.auctionstream.auction.application.AuctionCommand.*
import me.elgregos.auctionstream.auction.domain.event.AuctionAggregate
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.reakteves.domain.event.EventStore
import me.elgregos.reakteves.infrastructure.event.ReactorEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuctionCommandHandler(
    val auctionEventStore: EventStore<AuctionEvent, UUID, UUID>,
    val auctionEventPublisher: ReactorEventPublisher<UUID, UUID>
) {

    fun handle(auctionCommand: AuctionCommand) =
        when (auctionCommand) {
            is CreateAuction -> createAuction(auctionCommand)
            is StartAuction -> startAuction(auctionCommand)
            is PlaceBid -> placeBid(auctionCommand)
            is EndAuction -> endAuction(auctionCommand)
        }
            .flatMap { auctionEventStore.save(it) }
            .doOnNext { auctionEventPublisher.publish(it) }

    private fun createAuction(auctionCommand: CreateAuction) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .createAuction(auctionCommand.auction)

    private fun startAuction(auctionCommand: StartAuction) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .startAuction(auctionCommand.startedBy, auctionCommand.startTime)

    private fun placeBid(auctionCommand: PlaceBid) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .placeBid(auctionCommand.bidder, auctionCommand.amount, auctionCommand.placedAt)

    private fun endAuction(auctionCommand: EndAuction) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .endAuction(auctionCommand.endedBy, auctionCommand.endTime)

}