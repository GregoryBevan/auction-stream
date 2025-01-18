package me.elgregos.auctionstream.auction.api

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import me.elgregos.auctionstream.auction.application.AuctionCommand
import me.elgregos.auctionstream.auction.application.AuctionCommandHandler
import me.elgregos.auctionstream.auction.application.AuctionProjectionService
import me.elgregos.auctionstream.auction.domain.entity.Auction
import me.elgregos.reakteves.libs.nowUTC
import me.elgregos.reakteves.libs.uuidV5
import me.elgregos.reakteves.libs.uuidV7
import org.springframework.http.HttpStatus
import reactor.kotlin.core.publisher.toMono
import java.util.*

@RestController
@RequestMapping(
    path = ["/api/auctions"]
)
class AuctionController(
    private val auctionCommandHandler: AuctionCommandHandler,
    private val auctionProjectionService: AuctionProjectionService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun auctions() = auctionProjectionService.auctions()

    @GetMapping("{auctionId}")
    @ResponseStatus(HttpStatus.OK)
    fun auction(@PathVariable @Valid auctionId: UUID) = auctionProjectionService.auction(auctionId)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAuction() =
        auctionCommandHandler.handle(AuctionCommand.CreateAuction(Auction(id = uuidV7(), createdAt = nowUTC(), createdBy = uuidV5("creator"))))
            .toMono()
            .map { mapOf(Pair("auctionId", it.aggregateId)) }

}