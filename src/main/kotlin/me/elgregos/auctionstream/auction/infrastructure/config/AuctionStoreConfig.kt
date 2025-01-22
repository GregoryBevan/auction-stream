package me.elgregos.auctionstream.auction.infrastructure.config

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.auctionstream.auction.domain.event.AuctionEventRepository
import me.elgregos.auctionstream.auction.infrastructure.event.AuctionEventEntity
import me.elgregos.auctionstream.auction.infrastructure.projection.AuctionEntity
import me.elgregos.auctionstream.auction.infrastructure.projection.AuctionProjectionRepository
import me.elgregos.reakteves.domain.event.EventStore
import me.elgregos.reakteves.domain.projection.ProjectionStore
import me.elgregos.reakteves.infrastructure.event.DefaultEventStore
import me.elgregos.reakteves.infrastructure.projection.DefaultProjectionStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class AuctionStoreConfig {

    @Bean
    fun auctionEventStore(auctionEventRepository: AuctionEventRepository): EventStore<AuctionEvent, UUID, UUID> =
        DefaultEventStore(auctionEventRepository, AuctionEventEntity::class, AuctionEvent::class)

    @Bean
    fun auctionProjectionStore(auctionProjectionRepository: AuctionProjectionRepository): ProjectionStore<Auction, UUID, UUID> =
        DefaultProjectionStore(auctionProjectionRepository, AuctionEntity::class, Auction::class)
}