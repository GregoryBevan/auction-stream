package me.elgregos.auctionstream.auction.infrastructure.projection

import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.infrastructure.projection.ProjectionRepository
import java.util.*

interface AuctionProjectionRepository : ProjectionRepository<AuctionEntity, Auction, UUID, UUID>