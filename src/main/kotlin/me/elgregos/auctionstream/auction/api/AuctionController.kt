package me.elgregos.auctionstream.auction.api

import jakarta.validation.Valid
import me.elgregos.auctionstream.auction.api.dto.AuctionDTO
import me.elgregos.auctionstream.auction.application.AuctionCommand
import me.elgregos.auctionstream.auction.application.AuctionCommandHandler
import me.elgregos.auctionstream.auction.application.AuctionProjectionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
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

}