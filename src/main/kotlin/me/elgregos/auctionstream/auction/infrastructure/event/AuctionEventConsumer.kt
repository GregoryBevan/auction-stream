package me.elgregos.auctionstream.auction.infrastructure.event

import me.elgregos.auctionstream.auction.application.AuctionTimerService
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import me.elgregos.auctionstream.auction.domain.event.AuctionEvent.AuctionStarted
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kafka.receiver.ReceiverOffset

@Component
class AuctionEventConsumer(
    private val kafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, AuctionEvent>,
    private val auctionTimerService: AuctionTimerService
) {

    @EventListener(ApplicationStartedEvent::class)
    fun consommeFactureMessage() =
        Flux.defer { kafkaConsumerTemplate.receive() }
            .doOnNext { receiverRecord ->
                receiverRecord.value().let {
                    when(it) {
                        is AuctionStarted -> auctionTimerService.startAuctionTimer(it.auctionId)
                        is AuctionEvent.BidPlaced -> auctionTimerService.resetAuctionTimer(it.auctionId)
                        else -> Unit
                    }
                }
            }
            .map { it.receiverOffset() }
            .subscribe(ReceiverOffset::commit)
}