package me.elgregos.auctionstream.auction.infrastructure.config

import me.elgregos.auctionstream.auction.domain.event.AuctionEvent
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions

@Configuration
class AuctionKafkaConfig(private val kafkaProperties: KafkaProperties) {

    @Bean
    fun kafkaProducerTemplate() = ReactiveKafkaProducerTemplate(
        SenderOptions.create<String, AuctionEvent>(kafkaProperties.buildProducerProperties())
    )

    @Bean
    fun kafkaConsumerTemplate() = ReactiveKafkaConsumerTemplate(
        ReceiverOptions.create<String, AuctionEvent>(kafkaProperties.buildConsumerProperties())
            .subscription(listOf("auction_event_stream"))
    )

}