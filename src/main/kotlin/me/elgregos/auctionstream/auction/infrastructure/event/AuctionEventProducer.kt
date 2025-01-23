package me.elgregos.auctionstream.auction.infrastructure.event

import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class AuctionEventProducer(private val kafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, AuctionEvent>) {

    fun produce(auctionEvent: AuctionEvent): Flux<AuctionEvent> =
        kafkaProducerTemplate.send("auction_event_stream","${auctionEvent.aggregateId}", auctionEvent)
            .flatMapMany { Flux.just(auctionEvent) }

}