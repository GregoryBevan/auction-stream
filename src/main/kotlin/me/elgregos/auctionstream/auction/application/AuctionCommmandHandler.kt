package me.elgregos.auctionstream.auction.application

import me.elgregos.auctionstream.auction.application.AuctionCommand.CreateAuction
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
        }
            .flatMap { auctionEventStore.save(it) }
            .doOnNext { auctionEventPublisher.publish(it) }

    private fun createAuction(auctionCommand: CreateAuction) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .createAuction(auctionCommand.auction)

}