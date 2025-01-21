package me.elgregos.auctionstream.auction.api

import jakarta.validation.Valid
import me.elgregos.auctionstream.auction.api.dto.AuctionDTO
import me.elgregos.auctionstream.auction.api.dto.BidDTO
import me.elgregos.auctionstream.auction.application.AuctionCommand
import me.elgregos.auctionstream.auction.application.AuctionCommandHandler
import me.elgregos.auctionstream.auction.application.AuctionProjectionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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
    fun createAuction(@RequestBody auctionDTO: AuctionDTO) =
        auctionCommandHandler.handle(AuctionCommand.CreateAuction(auctionDTO.toAuction()))
            .toMono()
            .map { mapOf(Pair("auctionId", it.aggregateId)) }

    @PatchMapping("{auctionId}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun startAuction(@PathVariable auctionId: UUID) =
        auctionCommandHandler.handle(AuctionCommand.StartAuction(auctionId))
            .toMono()
            .map { mapOf(Pair("startTime", "${it.createdAt}")) }

    @PostMapping("{auctionId}/bid")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun placeBid(@PathVariable auctionId: UUID, @RequestBody bidDTO: BidDTO) =
        Flux.just(AuctionCommand.PlaceBid(auctionId, bidDTO.amount, bidDTO.bidder))
            .flatMap(auctionCommandHandler::handle)
            .toMono()
            .map { mapOf(Pair("status", it.event["bids"].last()["bidStatus"])) }

    @PatchMapping("{auctionId}/end")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun endAuction(@PathVariable auctionId: UUID) =
        auctionCommandHandler.handle(AuctionCommand.EndAuction(auctionId))
            .toMono()
            .map { mapOf(Pair("endTime", "${it.createdAt}")) }

}