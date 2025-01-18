# Auction Created event
To enable the creation of a new auction through an endpoint call, we start by defining the event and implementing the associated business logic. This approach ensures the event-driven design is firmly established before proceeding to implement the command and controller endpoint.

## 1. **Review the `AuctionCreated` event**

 - Navigate to the sealed class `me.elgregos.auctionstream.auction.domain.event.AuctionEvent`
 - Locate the `AuctionCreated` data class 
 - Key Points to Understand:
    - **ID Generation**:  
      The `id` property is a `UUID` generated using the `uuidV7()` function, ensuring a time based unique identifier for the event. 
    - **Event Structure**:  
      The `AuctionCreated` event contains metadata such as `id`, `version`, `createdAt`, and `createdBy`, alongside domain-specific fields like `auctionId` and `event`. 
    - **Serialization**:  
      The `event` field is a serialized JSON representation of the `Auction` object, enabling flexible storage and querying in the database.

> **_Note:_**
> Event names should be carefully defined to reflect business logic accurately.

In the next steps, you’ll create additional events:
- `AuctionStarted`
- `BidPlaced`
- `AuctionEnded`

## 2. **Modify the `createAuction` method in `AuctionController`**
   - **Add the DTOs**
     - Navigate to package `me.elgregos.auctionstream.auction.api`
     - Create a new package name `dto`
     - Create a file `ItemDTO.kt in the dto folder
     - Add the following code
      ```kotlin
      data class ItemDTO(
          val name: String,
          val description: String,
          val startingPrice: Double
      ) {
          fun toItem(): Item = Item(
               name = name,
               description = description,
               startingPrice = startingPrice
          )
      }
      ```
      - Create a file `AuctionDTO.kt` in the `dto` folder
      - Add the following code
      ```kotlin
      data class AuctionDTO(
         @field:JsonProperty("item") val itemDTO: ItemDTO
      ) {
         fun toAuction(): Auction =
            Auction(
                id = uuidV7(), 
                createdAt = nowUTC(), 
                createdBy = auctioneer.id, 
                item = itemDTO.toItem()
            )
      }
      ```
   - **AUpdate the `AuctionController`**
     - Open the `AuctionController`
     - Adapt the `createAuction` as followed
     ```kotlin
      @PostMapping
      @ResponseStatus(HttpStatus.CREATED)
      fun createAuction(@RequestBody auctionDTO: AuctionDTO) =
          auctionCommandHandler.handle(AuctionCommand.CreateAuction(auctionDTO.toAuction()))
              .toMono()
              .map { mapOf(Pair("auctionId", it.aggregateId)) }
     ```
> **_Note:_**
> The `ItemDTO` and `AuctionDTO data classes define the structure of the JSON payload required to create an auction.
- **Test the endpoint**
  - Restart the application
  - Open Postman (or any other API client) and create a new POST request with the following details
    - **URL**: `http://localhost:8080/api/auctions`
    - Body (JSON payload):
    ```json
    {
        "item": {
            "name": "Jimi Hendrix's Left-Handed Guitar, Played with His Teeth",
            "description": "A rare, left-handed Fender Stratocaster allegedly owned by Jimi Hendrix himself. This guitar is famous for being one of the instruments he famously played with his teeth during live performances. It comes with a certificate of authenticity, a signature from Hendrix’s personal roadie, and a small swatch of the shirt Hendrix wore during his legendary Woodstock performance.",
            "startingPrice": 1000000.00
        }
    }
    ```
    -  Ensure the response returns a 201 Created status and contains the newly created auction's ID, e.g.:
    ```json
    {
      "auctionId": "01948538-e50d-7ab9-862d-b7a8ee6dc6be"
    }
    ```
  - Retrieve the created auction with a new GET request
      - **URL**: `http://localhost:8080/api/auctions/{auctionId}`
    ```json
    {
        "id": "01948538-e50d-7ab9-862d-b7a8ee6dc6be",
        "version": 1,
        "createdAt": "2025-01-20T19:38:05.965432",
        "createdBy": "6fb687ef-8df3-4887-8f42-4e742ca2a765",
        "updatedAt": "2025-01-20T19:38:05.965432",
        "updatedBy": "6fb687ef-8df3-4887-8f42-4e742ca2a765",
        "item": {
            "name": "Jimi Hendrix's Left-Handed Guitar, Played with His Teeth",
            "description": "A rare, left-handed Fender Stratocaster allegedly owned by Jimi Hendrix himself. This guitar is famous for being one of the instruments he famously played with his teeth during live performances. It comes with a certificate of authenticity, a signature from Hendrix’s personal roadie, and a small swatch of the shirt Hendrix wore during his legendary Woodstock performance.",
            "startingPrice": 1000000.0
        },
        "bids": [],
        "startTime": null,
        "endTime": null
    }
    ```
> **_Note:_**
> The result is retrieved from the projection.

- You can also check `auction` and `auction_event` in the database

    ```sql
    select * from auction where id = '<auctionid>';
    ``` 
    ```sql
    select * from auction_event where aggregate_id = '<auctionid>';
    ```