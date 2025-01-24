package me.elgregos.auctionstream.auction.application

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

@Service
class AuctionTimerService(
    private val auctionCommandHandler: AuctionCommandHandler
) {

    // ConcurrentHashMap pour stocker les IDs d'enchères et leur timer associé
    private val auctionTimers = ConcurrentHashMap<UUID, TimerTask>()

    // Méthode pour démarrer le timer d'une enchère
    fun startAuctionTimer(auctionId: UUID) {
        addTimer(auctionId)

    }

    fun resetAuctionTimer(auctionId: UUID) {
        auctionTimers[auctionId]?.cancel()
        addTimer(auctionId)
    }

    private fun addTimer(auctionId: UUID) {
        auctionTimers.put(
            auctionId,
            Timer().schedule(
                delay = 30000L
            ) {
                Flux.defer {
                    auctionCommandHandler.handle(AuctionCommand.EndAuction(auctionId))
                }.subscribe()
                auctionTimers[auctionId]?.cancel()
                auctionTimers.remove(auctionId)
            })
    }
}