# Set Kafka as an event bus
The application was actually using a Reactive Event Bus to publish event in order to update the projection.

The following steps will guide you in adding a second system to publish events to a Kafka broker. The objective is to automate ending an auction after 30 seconds of inactivity (no new bids).

## 1. **Configure Kafka broker and AKHQ**

> **_Note:_**  
> AKHQ is a tool to monitor the Kafka broker

- **Update the `docker-compose.yml` file**
  - Replace the content of the `docker-compose.yml` file at the root of the project
    ```dockerfile
    services:
      action_stream_postgres:
        image: postgres:17-alpine
        container_name: auction_stream_postgres
        restart: always
        command: [ "postgres", "-c", "log_min_duration_statement=1000", "-c", "log_destination=stderr" ]
        volumes:
          - ./docker/init.sql:/docker-entrypoint-initdb.d/init.sql
          - auction-stream-data:/var/lib/postgresql/data
        ports:
          - 5432:5432
        environment:
          POSTGRES_PASSWORD: postgres
      kafka:
        image: confluentinc/cp-kafka:7.8.0
        container_name: kafka
        ports:
          - 9092:9092 # Kafka client port
          - 9093:9093 # Internal Raft communication port
        environment:
          KAFKA_PROCESS_ROLES: broker,controller
          KAFKA_NODE_ID: 1
          KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
          KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
          KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
          KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
          KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
          KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
          KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
          KAFKA_LOG_DIRS: /var/lib/kafka/data
          KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
          KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
        volumes:
          - kafka-data:/var/lib/kafka/data
      akhq:
          image: tchiotludo/akhq:0.25.1
          container_name: akhq
          depends_on:
            - kafka
          ports:
            - 9090:8080
          environment:
            AKHQ_CONFIGURATION: |
              akhq:
                connections:
                  kafka:
                    properties:
                      bootstrap.servers: kafka:9092
    volumes:
      auction-stream-data:
      kafka-data:
    ```
  - Open a terminal to the project root path
  - Execute the following command line
   ```shell
   docker-compose up -d
   ```
  - Open [http://localhost:9090/ui/kafka/node](http://localhost:9090/ui/kafka/node) to vizualize the kafka broker


## 2. **Configure Kafka in application**
- **Add Spring Kafka dependencies**
  - Open `build.gradle.kts` file
  - Add the following lines in the `dependencies` closure
    ```kotlin
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.projectreactor.kafka:reactor-kafka")
    ```
- **Configure the SpringBoot application**
  - Navigate to the file `src/main/resources/application.yml`
  - Add the kafka configuration after the `liquibase` block
  ```yaml
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      auto-offset-reset: latest
      enable-auto-commit: false
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      group-id: auction-stream-group
      client-id: auction-stream-client-50f4d95e-90b0-4df4-966c-da5062d2c00f
      properties:
        spring.json.trusted.packages: me.elgregos.auctionstream.auction.domain.event
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    properties:
      allow.auto.create.topics: true
  ```
  > **_Note:_**  
  > Pay attention to the indentation of the file
  > The `group-id` property is used when you scale the number of consumers with same process logic (increasing performance)
  > The `client-id` should be unique across consumers with same group-id

- **Configure reactive producer and consumer**
  - Navigate to the package `me.elgregos.auctionstream.auction.infrastructure.config`
  - Create class `AuctionKafkaConfig`
  - Add the `@Configuration` annotation to the class and pass a `KafkaProperties` type field
    ```kotlin
    @Configuration
    class AuctionKafkaConfig(private val kafkaProperties: KafkaProperties) {
    }
    ```
  > **_Note:_**    
  > The field `kafkaProperties` will be automatically autowired by Spring. They contain all the kafka properties defines
  in the `application.yml`.

  - In the `AuctionKafkaConfig` class body, add the following Spring bean to define a reactive producer
    ```kotlin
    @Bean
    fun kafkaProducerTemplate() = ReactiveKafkaProducerTemplate(
        SenderOptions.create<String, AuctionEvent>(kafkaProperties.buildProducerProperties())
    )
    ```
  - Still in the `AuctionKafkaConfig` class body, add the reactive consumer
    ```kotlin
    @Bean
    fun kafkaConsumerTemplate() = ReactiveKafkaConsumerTemplate(
        ReceiverOptions.create<String, AuctionEvent>(kafkaProperties.buildConsumerProperties())
            .subscription(listOf("auction_event_stream"))
    )
    ```
  > **_Note:_**  
  > The class to the `subscription()` method defines the list of topic the consumer will listen to.

- **Implements the auction event producer**
  - Create the class `me.elgregos.auctionstream.auction.infrastructure.event.AuctionEventProducer`
  - Update the content as shown below
  ```kotlin
  @Component
  class AuctionEventProducer(private val kafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, AuctionEvent>) {

    fun produce(auctionEvent: AuctionEvent): Flux<AuctionEvent> =
        kafkaProducerTemplate.send("auction_event_stream","${auctionEvent.aggregateId}", auctionEvent)
            .flatMapMany { Flux.just(auctionEvent) }

  }
  ```
  > **_Note:_**  
  > Pay attention to the topic name auction_event_stream, as it is also configured for the consumer.

- **Publish stored events**
  - Navigate to the command handler class `me.elgregos.auctionstream.auction.application.AuctionCommandHandler`
  - Inject the `AuctionEventProducer` into the constructor as shown below
  ```kotlin
  @Service
  class AuctionCommandHandler(
    val auctionEventStore: EventStore<AuctionEvent, UUID, UUID>,
    val auctionEventPublisher: ReactorEventPublisher<UUID, UUID>,
    val auctionEventProducer: AuctionEventProducer
  ) {
  // ...
  ```
  - Add the following call at the end of the `handle()` method after the `doOnNext()` closure
  ```kotlin
  .flatMap { auctionEventProducer.produce(it) }
  ``` 
- **Test the event production**
  - Restart the application
  - Use Postman to test one of the previously implemented endpoints
  - Access AKHQ to view the published event in  [`auction_event_stream`topic](http://localhost:9090/ui/kafka/topic/auction_event_stream/data?sort=Oldest&partition=All) 


- **Implements the auction event consumer to end the auction**
  - Create the class `me.elgregos.auctionstream.auction.infrastructure.event.AuctionEventConsumer`
  - Update the content as shown below
  ```kotlin
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
  ```
  > **_Note:_**  
  > The consumer uses the `auctionTimerService`