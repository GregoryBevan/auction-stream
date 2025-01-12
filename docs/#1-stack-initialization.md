# Initialize the Spring Boot project

1. Open your browser, and go to https://start.spring.io/

  ![spring-initializr.png](%231%2Fspring-initializr.png)

  *Refer to the image above for guidance on filling the form.*

2. **Select the following options**
   - **Project**:  _Gradle - Kotlin_
   - **Language**:  _Kotlin_
   - **Spring Boot**: _3.4.1_


3. **Project Metadata - Fill in the following fields**:
   - **Group**: `me.elgregos`
   - **Artifact**: `auction-stream`
   - **Name**: `auction-stream`
   - **Description** (optional): _Codelab for auction-stream application to learn more about EDA_
   - **Package name**: `me.elgregos.auctionstream

> **_Note:_**  
> Keep the remaining options as is. Target JRE **21**, and set the packaging type to **Jar**.

4. **Add Dependencies - Include the following dependencies**
   - Spring Boot DevTools
   - Spring Reactive Web
   - Spring Data R2DBC
   - Liquibase Migration
   - PostgreSQL Driver
   - Validation

> _Tip:_   
> Each dependency is chosen to support reactive programming, database migrations and validation in an event-driven application.

5. **Download and Extract**
   - Click on the `GENERATE button to download the project zip file
   - Unzip the content into the directory you previously cloned

6. **Convert `application.properties` to `yaml**:
   - Change extension of file `src/main/resources/application.properties` to `.yml`
   - Replace the content with the following:
```yaml
spring:
  application:
    name: auction-stream
```
> **_Why YAML?_**
> YAML improves readability for hierarchical configuration data. Learn more [here](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties).
