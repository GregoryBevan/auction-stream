package me.elgregos.auctionstream.auction.infrastructure.event

import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.auctionstream.auction.domain.event.AuctionEventRepository
import me.elgregos.reakteves.infrastructure.event.EventEntityRepository
import java.util.*

interface AuctionEventEntityRepository : EventEntityRepository<AuctionEventEntity, AuctionEvent, UUID, UUID>, AuctionEventRepository