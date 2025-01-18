# Auction domain creation

The goal of this codelab is to focus on the usage and advantages of the Event-Driven Architecture.

To help us, we will use a Kotlin library and a Gradle plugin that simplify the initialization of the domain and the
implementation of the Event Sourcing pattern.

- The **library** provides essential components (classes, interfaces, etc.) for both the CQRS and Event Sourcing
  patterns.
- The **Gradle plugin** acts as a CLI tool to generate the skeleton for the domain, saving you from creating these
  manually.

## Initialize the Auction domain

1. **Add ReaKt.EveS Gradle plugin**
    - Open the `plugins` block in the `build.gradle.kts` file
    - Add the following plugin
         ```kotlin
             id("me.elgregos.reakteves.cli") version "1.4.6"
         ```

2. **Add ReaKt.EveS dependency**
    - Open the `dependencies` block in the `build.gradle.kts file
    - Add the following dependency
         ```kotlin
             implementation("me.elgregos:reakt-eves:1.4.6")
         ```

3. **Generate the Auction domain**
    - Open a terminal in the project root directory
    - Run the appropriate command based on your operating system
         - Unix/MacOs
      ```shell
      ./gradlew initDomain
      ```
        - Windows
      ```
        gradle initDomain
      ```
   - Set the `domain entity` name to `Auction` and confirm
   - Confirm the default value for `domain package`

This can also be done using the Gradle view of IntelliJ.

<img src="%233%2Fgradle-view.png" width="50%" alt="Image gradle-view.png">

4. **Restart the application**

See [#2](%232-database-initialization.md#start-the-application)

You can now check the database to see that the tables described in the _db-changelog.sql_ file are created.

5. **Develop the Auction Domain Model**
   - Create the following Kotlin files in package `me.elgregos.auctionstream.auction.domain.entity`
     - Create `data class Item` with the following content  
        ```kotlin
       package me.elgregos.auctionstream.auction.domain.entity

        data class Item(
           val name: String,
           val description: String,
           val startingPrice: Double
        )
        ```
     - Create `enum class BidStatus` with the following content  
        ```kotlin
       package me.elgregos.auctionstream.auction.domain.entity
         
         enum class BidStatus {
            ACCEPTED,
            REJECTED 
       }
        ```
     - Create `data class Bid` with the following content  
        ```kotlin
         package me.elgregos.auctionstream.auction.domain.entity
         
         import java.time.LocalDateTime
         
         data class Bid(
            val bidder: String,
            val amount: Double,
            val placedAt: LocalDateTime,
            val bidStatus: BidStatus
         )
        ```
     - Create `data class Auctioneer` with the following content  
        ```kotlin
        package me.elgregos.auctionstream.auction.domain.entity
        
        import java.util.UUID
        
        data class Auctioneer(
            val id: UUID = UUID.randomUUID(),
            val name: String
        )
        ```
     - Complete `data class Auction` with the following fields  
        ```kotlin
        package me.elgregos.auctionstream.auction.domain.entity

        import me.elgregos.reakteves.domain.entity.DomainEntity
        import java.time.LocalDateTime
        import java.util.*
        
        val auctioneer = Auctioneer(name = "Sotheby's")

        data class Auction(
            override val id: UUID,
            override val version: Int = 1,
            override val createdAt: LocalDateTime,
            override val createdBy: UUID,
            override val updatedAt: LocalDateTime = createdAt,
            override val updatedBy: UUID = createdBy,
            val item: Item,
            val bids: List<Bid> = mutableListOf(),
            val startTime: LocalDateTime? = null,
            val endTime: LocalDateTime? = null,
        ) : DomainEntity<UUID, UUID> {
        
        }
        ```

The next will be to design auction events.       