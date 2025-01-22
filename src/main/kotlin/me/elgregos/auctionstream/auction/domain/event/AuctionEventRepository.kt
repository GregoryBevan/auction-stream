package me.elgregos.auctionstream.auction.domain.event

import me.elgregos.auctionstream.auction.infrastructure.event.AuctionEventEntity
import me.elgregos.reakteves.infrastructure.event.EventEntityRepository
import java.util.*

interface AuctionEventRepository : EventEntityRepository<AuctionEventEntity, AuctionEvent, UUID, UUID>