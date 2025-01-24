# Handling other auction events

## 1. **Handle the `AuctionStarted` event**

- **Define the `AuctionStarted` Event**
    - Navigate to the sealed class `me.elgregos.auctionstream.auction.domain.event.AuctionEvent`
    - Add the following code to create `AuctionStarted` event
   ```kotlin
   data class AuctionStarted(
       override val id: UUID = uuidV7(),
       override val version: Int,
       override val createdAt: LocalDateTime = nowUTC(),
       override val createdBy: UUID,
       val auctionId: UUID,
       override val event: JsonNode
   ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, AuctionStarted::class.simpleName!!, event) {

       constructor(updatedAuction: Auction) : this(
           auctionId = updatedAuction.id,
           createdAt = updatedAuction.updatedAt,
           createdBy = updatedAuction.updatedBy,
           version = updatedAuction.version,
           event = genericObjectMapper.createObjectNode()
               .put("id", "${updatedAuction.id}")
               .put("updatedBy", "${updatedAuction.updatedBy}")
               .put("updatedAt", "${updatedAuction.updatedAt}")
               .put("version", "${updatedAuction.version}")
               .put("startTime", "${updatedAuction.startTime}")
       )
   }   
   ```

> **_Note:_**  
The `AuctionAggregate` defines methods to rebuild the auction from all events using the `jsonMergePatch()` method. This
ensures that the auction state is reconstructed correctly and efficiently from the event patches.

- **Implements the business logic**
    - Navigate to domain data class `me.elgregos.auctionstream.auction.domain.entity.Auction`
    - Add a method to set the start time for the auction
    ```kotlin    
    fun start(startBy: UUID, startTime: LocalDateTime, version: Int): Auction = copy(
        updatedBy = startBy,
        updatedAt = startTime,
        version = version,
        startTime = startTime
    )
    ```

> **_Note:_**  
Using the `copy()` method of Kotlin's data classes ensures immutability, which is crucial to prevent unintended side
effects.
This design allows for clear, functional transformations of the Auction entity.

   - Navigate to the aggregate class `me.elgregos.auctionstream.auction.domain.event.AuctionAggregate`
   - Change the visibility of the `auctionId` field in the constructor
   ```kotlin
        private val auctionId: UUID,
   ```
   - Add a `startAuction` method that implements the logic to create the `AuctionStarted` event
   ```kotlin
    fun startAuction(startedBy: Auctioneer, startTime: LocalDateTime): Flux<AuctionEvent> =
        // From the previous state loaded from database
        previousState()
            // Check the auction exists
            .filter { !it.isEmpty }
            .switchIfEmpty(Mono.error { Exception("Auction with id $auctionId not found") })
            // Convert the JSON to Auction object
            .map { JsonConvertible.fromJson<Auction>(it) }
            // Pair with the next version of the auction 
            .zipWith(nextVersion())
            // Create the AuctionStarted event
            .flatMapMany { Flux.just(AuctionEvent.AuctionStarted(it.t1.start(startedBy.id, startTime, it.t2))) }
   ```
> **_Note:_**  
As an improvement, the code could be made more logical by hiding technical operations.

- **Implements the command**
  - Navigate to the command sealed class `me.elgregos.auctionstream.auction.application.AuctionCommand`
  - Add the `StartAuction` command
  ```kotlin
    data class StartAuction(
      override val auctionId: UUID,
      val startedBy: Auctioneer = auctioneer,
      val startTime: LocalDateTime = nowUTC(), 
    ) : AuctionCommand(auctionId = auctionId)
  ```
  - Add the `startAuction` method to the `AuctionCommandHandler` class
  ```kotlin
      private fun startAuction(auctionCommand: StartAuction) =
          AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
              .startAuction(auctionCommand.startedBy, auctionCommand.startTime)
  ```
  - Add the missing `when` branch to the `handle` method
  ```kotlin
              is StartAuction -> startAuction(auctionCommand)
  ```
> **_Note:_**
> Kotlin's sealed class is very helpful to avoid missing a branch

- **Add the endpoint**
  - Navigate to the controller class `me.elgregos.auctionstream.auction.api.AuctionController`
  - Add the endpoint method to start the auction
  ```kotlin
    @PatchMapping("{auctionId}/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun startAuction(@PathVariable auctionId: UUID) =
        auctionCommandHandler.handle(AuctionCommand.StartAuction(auctionId))
            .toMono()
            .map { mapOf(Pair("auctionId", it.aggregateId)) }
  ```
> **_Note:_**
> The code isn't compiling at the moment. Can you identify the reason why?

- **Handling projection updates**
  - Navigate to the projection subscriber class `me.elgregos.auctionstream.auction.infrastructure.projection.AuctionProjectionSubscriber`
  - Add the `udpateAuction` method to store new version of the Auction projection
  ```kotlin
    private fun updateAuction(event: AuctionEvent) =
        auctionProjectionStore.find(event.aggregateId)
            .flatMap { auctionProjectionStore.update(mergeJsonPatch(it, event)) }
  ```
  - Add the missing `when` branch to the `onEvent` method
  ```kotlin
    is AuctionStarted -> updateAuction(it)
  ```
- **Test the endpoint**
  - Restart the application
  - Open Postman (or any other API client) and create a new PATCH request with the following details
    - **URL**: `http://localhost:8080/api/auctions/{auctionId}/start`
  - Execute the request
  - Use the GET request to check the auction `startTime` field
    - **URL**: `http://localhost:8080/api/auctions/{auctionId}`
- **Check the auction events**
  - Use the following request to check the auction events
  ```sql
    select * from auction_event where aggregate_id = '<auctionid>';
  ```
## 2. **Handle the `BidPlaced` event**
- **Define the `BidPlaced` Event**
  - Navigate to the sealed class `me.elgregos.auctionstream.auction.domain.event.AuctionEvent`
  - Add the following code to create `BidPlaced` event
   ```kotlin
    data class BidPlaced(
        override val id: UUID = uuidV7(),
        override val version: Int,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, BidPlaced::class.simpleName!!, event) {

        constructor(updatedAuction: Auction) : this(
            auctionId = updatedAuction.id,
            createdAt = updatedAuction.updatedAt,
            createdBy = updatedAuction.updatedBy,
            version = updatedAuction.version,
            event = genericObjectMapper.createObjectNode()
                .put("id", "${updatedAuction.id}")
                .put("updatedBy", "${updatedAuction.updatedBy}")
                .put("updatedAt", "${updatedAuction.updatedAt}")
                .put("version", "${updatedAuction.version}")
                .set<ObjectNode>("bids", genericObjectMapper.createArrayNode().apply {
                    addAll(updatedAuction.bids.map { genericObjectMapper.valueToTree<JsonNode>(it) })
                })
        )
    } 
   ```
- **Implements the business logic**
  - Navigate to domain data class `me.elgregos.auctionstream.auction.domain.entity.Auction`
  - Add a method to add a bid to the auction
    ```kotlin    
        fun placeBid(bid: Bid, version: Int): Auction = copy(
            updatedBy = uuidV5(bid.bidder),
            updatedAt = bid.timestamp,
            version = version,
            bids = bids.toMutableList().apply { add(bid) }.toList(),
        )
    ```
> **_Note:_**  
> The `uuidv5()` method guarantees the uniqueness of the generated UUID based on the bidder string.   
> The Kotlin `apply()` [scope function](https://kotlinlang.org/docs/scope-functions.html) enables you to execute methods on an object while returning the same object.    
  - Add a second method to determine the status of the bid
  ```kotlin
    fun bidStatus(amount: Double): BidStatus =
        // Bid could not be placed on ended auction
        if (endTime != null) BidStatus.REJECTED
        else bids.findLast { it.bidStatus == BidStatus.ACCEPTED }
            // Compare the amount on the last accepted bid
            ?.let { if (amount > it.amount) BidStatus.ACCEPTED else BidStatus.REJECTED }
            // Compare the amount to the starting price if no previous accepted bid
            ?: if (amount > item.startingPrice) BidStatus.ACCEPTED else BidStatus.REJECTED
  ```
  - Navigate to the aggregate class `me.elgregos.auctionstream.auction.domain.event.AuctionAggregate`
  - Create the following function to avoid repeating the logic to get the previous auction and the next version
  ```kotlin
   private fun retrieveAuctionAndVersion() =
      previousState()
        // Check the auction exists
        .filter { !it.isEmpty }
        .switchIfEmpty(Mono.error { Exception("Auction with id $auctionId not found") })
        // Convert the JSON to Auction object
        .map { JsonConvertible.fromJson<Auction>(it) }
        // Pair with the next version of the auction
        .zipWith(nextVersion())
  ```
  - Refactor the `startAuction()` to use `retrieveAuctionAndVersion()` function
  ```kotlin
    fun startAuction(startedBy: Auctioneer, startTime: LocalDateTime): Flux<AuctionEvent> =
        retrieveAuctionAndVersion()
            // Create the AuctionStarted event
            .flatMapMany { Flux.just(AuctionEvent.AuctionStarted(it.t1.start(startedBy.id, startTime, it.t2))) }

  ```
  - Add a `placeBid` method that implements the logic to create the `BidPlaced` event
  ```kotlin
    fun placeBid(bidder: String, amount: Double, placedAt: LocalDateTime): Flux<AuctionEvent> = 
        retrieveAuctionAndVersion()
            // Place the bid
            .map { it.t1.placeBid(Bid(bidder, amount, placedAt, it.t1.bidStatus(amount)), it.t2) }
            // Create the BidPlaced event
            .flatMapMany { Flux.just(AuctionEvent.BidPlaced(it)) }
  ```
    
- **Implements the command**
  - Navigate to the command sealed class `me.elgregos.auctionstream.auction.application.AuctionCommand`
  - Add the `PlaceBid` command
  ```kotlin
    data class PlaceBid(
        override val auctionId: UUID,
        val amount: Double,
        val placedBy: String,
        val placedAt: LocalDateTime = nowUTC(),
    ) : AuctionCommand(auctionId = auctionId)
  ```
  - Add the `placeBid` method to the `AuctionCommandHandler` class
  ```kotlin
    private fun placeBid(auctionCommand: PlaceBid) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .placeBid(auctionCommand.bidder, auctionCommand.amount, auctionCommand.placedAt)
  ```
  - Add the missing `when` branch to the `handle` method
  ```kotlin
    is PlaceBid -> placeBid(auctionCommand)
  ```
  
- **Add the endpoint**
  - Create the `BidDTO` data class in `me.elgregos.auctionstream.auction.api.dto`
  ```kotlin
  data class BidDTO(
    val bidder: String,
    val amount: Double,
  ) 
  - ```
  - Navigate to the controller class `me.elgregos.auctionstream.auction.api.AuctionController`
  - Add the endpoint method to place a bid
  ```kotlin
    @PostMapping("{auctionId}/bid")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun placeBid(@PathVariable auctionId: UUID, @RequestBody bidDTO: BidDTO) =
        Flux.just(AuctionCommand.PlaceBid(auctionId, bidDTO.amount, bidDTO.bidder))
            .flatMap(auctionCommandHandler::handle)
            .toMono()
            .map { mapOf(Pair("status", it.event["bids"].last()["bidStatus"])) }
  ```
  
- **Handling projection updates**
  - Navigate to the projection subscriber class `me.elgregos.auctionstream.auction.infrastructure.projection.AuctionProjectionSubscriber`
  - Add the missing `when` branch to the `onEvent` method
  ```kotlin
    is AuctionStarted, is BidPlaced -> updateAuction(it)
  ```
  
- **Test the endpoint**
  - Restart the application
  - Open Postman (or any other API client) and create a new POST request with the following details
    - **URL**: `http://localhost:8080/api/auctions/{auctionId}/bid`
    - Body (JSON payload):
    ```json
    {
        "bidder": "Raise Handerson",
        "amount": 1000000.00
    }
    ```
  - Execute the request
  ```json
  {
    "status": "REJECTED"
  }
  ```
  - Change the `amount` value to place an accepted bid 
  - Use the GET request to check the auction
    - **URL**: `http://localhost:8080/api/auctions/{auctionId}`

  
## 3. **Handle the `AuctionEnded` event**
- **Define the `AuctionEnded` Event**
  - Navigate to the sealed class `me.elgregos.auctionstream.auction.domain.event.AuctionEvent`
  - Add the following code to create `AuctionEnded` event
   ```kotlin
    data class AuctionEnded(
        override val id: UUID = UUID.randomUUID(),
        override val version: Int,
        override val createdAt: LocalDateTime = nowUTC(),
        override val createdBy: UUID,
        val auctionId: UUID,
        override val event: JsonNode
    ) : AuctionEvent(id, version, createdAt, createdBy, auctionId, AuctionEnded::class.simpleName!!, event) {

        constructor(updatedAuction: Auction) : this(
            auctionId = updatedAuction.id,
            createdAt = updatedAuction.updatedAt,
            createdBy = updatedAuction.updatedBy,
            version = updatedAuction.version,
            event = genericObjectMapper.createObjectNode()
                .put("id", "${updatedAuction.id}")
                .put("updatedBy", "${updatedAuction.updatedBy}")
                .put("updatedAt", "${updatedAuction.updatedAt}")
                .put("version", "${updatedAuction.version}")
                .put("endTime", "${updatedAuction.endTime}")
        )
    }
   ```

- **Implements the business logic**
  - Navigate to domain data class `me.elgregos.auctionstream.auction.domain.entity.Auction`
  - Add an `end()` method  to put an ending to the auction
    ```kotlin    
    fun end(endedBy: UUID, endTime: LocalDateTime, version: Int): Auction = copy(
        updatedBy = endedBy,
        updatedAt = endTime,
        version = version,
        endTime = endTime
    )
    ```
- Navigate to the aggregate class `me.elgregos.auctionstream.auction.domain.event.AuctionAggregate`
- Add an `endAuction()` method that implements the logic to create the `AuctionEnded` event
   ```kotlin
   fun endAuction(endedBy: Auctioneer, endedTime: LocalDateTime): Flux<AuctionEvent> =
      retrieveAuctionAndVersion()
          // Check if auction is already started and not yet ended
          .filter { it.t1.startTime != null && it.t1.endTime == null }
          .switchIfEmpty(Mono.error { Exception("Auction with id $auctionId can't be ended") })
          // Create the AuctionStarted event
          .flatMapMany { Flux.just(AuctionEvent.AuctionEnded(it.t1.start(endedBy.id, endedTime, it.t2))) }
   ```

- **Implements the command**
  - Navigate to the command sealed class `me.elgregos.auctionstream.auction.application.AuctionCommand`
  - Add the `EndAuction` command
  ```kotlin
  data class EndAuction(
      override val auctionId: UUID,
      val endedBy: Auctioneer = auctioneer,
      val endTime: LocalDateTime = nowUTC(), 
  ) : AuctionCommand(auctionId = auctionId)
  ```
  - Add the `startAuction` method to the `AuctionCommandHandler` class
  ```kotlin
    private fun endAuction(auctionCommand: EndAuction) =
        AuctionAggregate(auctionCommand.auctionId, auctionEventStore)
            .startAuction(auctionCommand.endedBy, auctionCommand.endTime)
  ```
  - Add the missing `when` branch to the `handle` method
  ```kotlin
    is EndAuction -> endAuction(auctionCommand)
  ```

- **Add the endpoint**
  - Navigate to the controller class `me.elgregos.auctionstream.auction.api.AuctionController`
  - Add the endpoint method to put an end to an auction
  ```kotlin
    @PatchMapping("{auctionId}/end")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun endAuction(@PathVariable auctionId: UUID) =
        auctionCommandHandler.handle(AuctionCommand.EndAuction(auctionId))
            .toMono()
            .map { mapOf(Pair("endTime", "${it.createdAt}")) }
  ```
  
- **Handling projection updates**
  - Navigate to the projection subscriber class `me.elgregos.auctionstream.auction.infrastructure.projection.AuctionProjectionSubscriber`
  - Add the missing `when` branch to the `onEvent` method
  ```kotlin
    is AuctionStarted, is BidPlaced, is AuctionEnded -> updateAuction(it)
  ```

- **Test the endpoint**
  - Restart the application
  - Open Postman (or any other API client) and create a new PATCH request with the following details
    - **URL**: `http://localhost:8080/api/auctions/{auctionId}/end`
  - Execute the request
  ```json
  {
    "endTime": "2025-01-22T21:38:18.057999"
  }
  ```

All events of this simple auction system have been defined. You are now able to play a complete auction scenario.