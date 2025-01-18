package me.elgregos.auctionstream.auction.infrastructure.projection

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent.*
import me.elgregos.reakteves.domain.JsonConvertible.Companion.fromJson
import me.elgregos.reakteves.domain.event.Event
import me.elgregos.reakteves.domain.projection.ProjectionStore
import me.elgregos.reakteves.infrastructure.event.ReactorEventBus
import me.elgregos.reakteves.infrastructure.event.ReactorEventSubscriber
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class AuctionProjectionSubscriber(
    reactorEventBus: ReactorEventBus<UUID, UUID>,
    private val auctionProjectionStore: ProjectionStore<Auction, UUID, UUID>,
) : ReactorEventSubscriber<UUID, UUID>(reactorEventBus) {

    @PostConstruct
    fun initialize() {
        subscribe()
    }

    override fun onEvent(event: Event<UUID, UUID>): Mono<Void> {
        return Mono.just(event)
            .filter { e -> e is AuctionEvent }
            .cast(AuctionEvent::class.java)
            .flatMap {
                when (it) {
                    is AuctionCreated -> createAuction(it)
                }
            }
            .doOnError { error -> logger.error(error) { "An error occurred while processing event" } }
            .then()
    }

    private fun createAuction(event: AuctionCreated) =
        auctionProjectionStore.insert(fromJson(event.event))
}