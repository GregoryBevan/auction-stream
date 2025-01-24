# Configuration of PostgreSQL database

## Local Docker container creation
To easily set up a database on your computer, we're going to use Docker.
1. **Edit the `compose.yaml` file**
   - Copy the following content to it
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
    volumes:
      auction-stream-data:
    ```
2. **Create the `init.sql` file in the `docker` folder**
   - Add the following content to initialize the database on container startup
    ```sql
    create database "auction-stream";
    create user admin_user with password 'guess-what';
    grant all privileges on database "auction-stream" to admin_user;
    alter database "auction-stream" owner to admin;
    ```
3. **Start PostgreSQL**
   - Open a terminal to the project root path
   - Execute the following command line
   ```shell
   docker-compose up -d
   ```
   - Check the PostgreSQL container 
   ```shell
   docker ps
   ```
![docker-ps.png](%232%2Fdocker-ps.png)

## Application configuration
1. **Edit the `application.yml` file**
   - Copy the following content into the file under `spring:`
   ```yml
      r2dbc:
        url: r2dbc:postgresql://${POSTGRESQL_ADDON_HOST:localhost}:${POSTGRESQL_ADDON_PORT:5432}/${POSTGRESQL_ADDON_DB:auction-stream}
        username: ${POSTGRESQL_ADDON_USER:admin_user}
        password: ${POSTGRESQL_ADDON_PASSWORD:guess-what}
        pool:
          initial-size: 10
          max-size: 50
      liquibase:
        change-log: classpath:db/changelog/db-changelog.sql
        url: jdbc:postgresql://${POSTGRESQL_ADDON_HOST:localhost}:${POSTGRESQL_ADDON_PORT:5432}/${POSTGRESQL_ADDON_DB:auction-stream}
        user: ${POSTGRESQL_ADDON_USER:admin_user}
        password: ${POSTGRESQL_ADDON_PASSWORD:guess-what}
    ```

2. **Create the `db-changelog.sql` file**
   - Create a new file in `src/resources/db/changelog/`
   - Add the following line to initialize Liquibase changes
```sql
--liquibase formatted sql
```

3. **Start the application from IntelliJ**
   - Open the file `AuctionStreamApplication.kt` in IntelliJ
   - Click on the green arrow to start the application.
![start-application.png](%232%2Fstart-application.png)

4. **Check the database**
  - Open the **Database** view in IntelliJ
  - Add a new PostgreSQL data source
![database-view.png](%232%2database-view.png)
  - Fill in the connection details:
    - **User**: `admin_user`
    - **Password**: `guess-what`
    - **Database**: `auction_stream`
  - Click **Test Connection** and ensure the connection is successful.
  - Click **OK** to save the configuration.
