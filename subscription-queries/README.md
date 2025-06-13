# Subscribing to Queries

-----

**NOTE**

This tutorial assumes you have at least completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/) beforehand.

-----

A consequence of implementing the CQRS pattern is the asynchronicity between writes (commands) and reads (queries).
This can be an issue when you need a query result to fully reflect the most recent command's effects.

This app demonstrates how to re-synchronize queries and commands.

The app is a simple library system with two domain entities:

* Readers
* Books

You can perform the following actions:

* Register a reader using the `RegisterReaderCommand`.
* Purchase a book using the `PurchaseBookCommand`.
* Lend and return a book to/from a reader using the `LendBookCommand` and `ReturnBookCommand`, respectively.

# The Problem

First, let's understand the basic message and data flow in a standard OpenCQRS app:

A client calls one of the app's endpoints **(1)**, which then issues a command to the command router **(2)**.
The command router passes the command to the appropriate command handler **(3)**, which writes one or more events to the event store **(4)**.
One or more event handlers **(5)** can then pick up each event and execute the relevant logic.
A special type of event handler, called a *projector*, updates the app's read-model by persisting an updated view to the read-optimized database **(6a)**.
The app's controllers **(7)** can then use this database to allow the client to query data through additional endpoints **(8)**.

![](diagrams/Single-JVM-Setup.svg)

The problem here is that the message/data flow *leaves* the app's JVM after publishing events and *re-enters* only when events are handled.
This means the initial thread issuing the command terminates immediately after handling, while corresponding events are handled in separate threads.
Thus, the client can't know if the data from their latest query already incorporates changes from their last command.

In a simple scenario with a single app instance, this can be resolved by synchronizing writing and reading at the controller level.
The request-receiving thread waits for the projection thread **(6b)** and *then* returns the resulting view to the client as a response:

```java
@Service
public class SynchronizerService {
    private final Map<String, CompletableFuture<Object>> futureResults = new ConcurrentHashMap<>();

    public CompletableFuture<Object> getLatestResultFor(String correlationId) {
        return futureResults.computeIfAbsent(correlationId, k -> new CompletableFuture<>());
    }

    public void putLatestResultFor(String correlationId, Object data) {
        if (futureResults.containsKey(correlationId)) {
            futureResults.get(correlationId).complete(data);
            futureResults.remove(correlationId);
        }
    }
}
```

We ensure a read-model update corresponds to a command by routing the command with a *correlation ID* in its metadata.
This ID is then passed in the metadata of the *latest* event published by the command handler.

However, this breaks down in a more realistic, industry-grade setup where multiple app instances run in parallel (usually containerized)
and are reverse-proxied by a load balancer:

![](diagrams/Multi-JVM-Setup.v2.svg)

Here, event handling isn't just done by a different thread than command handling; it might also occur on a completely different JVM.

To solve this in a multi-JVM scenario, you can modify the single-JVM approach. Instead of waiting for an update from another thread (which might never come),
have the controller listen for updates *directly* from the database.

# One Solution

The solution this app exemplifies has two main components:

* Using Postgres as our read-model database and leveraging its [NOTIFY feature](https://www.postgresql.org/docs/current/sql-notify.html).
* Using Spring Integration's [JdbcChannelMessageStore-API](https://docs.spring.io/spring-integration/reference/jdbc/message-store.html) to listen for notifications.

First, we set up a [configuration](src/main/java/com/example/cqrs/configuration/CqrsConfiguration.java) to ensure only one JVM handles a given event:

```java
    @Bean
    public DefaultLockRepository defaultLockRepository(DataSource dataSource) {
        var result = new DefaultLockRepository(dataSource);

        result.setPrefix("EVENTHANDLER_");
        return result;
    }

    @Bean
    public JdbcLockRegistry jdbcLockRegistry(LockRepository lockRepository) {
        return new JdbcLockRegistry(lockRepository);
    }
```

Next, we configure subscribable channels to listen for database notifications as per the [documentation](https://docs.spring.io/spring-integration/reference/jdbc/message-store.html#postgresql-push):

```java
    @Bean
    public JdbcChannelMessageStore messageStore(DataSource dataSource) {
        JdbcChannelMessageStore messageStore = new JdbcChannelMessageStore(dataSource);
        messageStore.setChannelMessageStoreQueryProvider(new PostgresChannelMessageStoreQueryProvider());
        return messageStore;
    }

    @Bean
    public PostgresChannelMessageTableSubscriber subscriber(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        return new PostgresChannelMessageTableSubscriber(() ->
                DriverManager.getConnection(url, username, password).unwrap(PgConnection.class));
    }

    @Bean
    public PostgresSubscribableChannel channels(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, "some group", subscriber);
    }
```

After this setup, you need a [SQL file](schema.sql) to initialize Postgres to work with the mechanisms mentioned above.

The final piece connecting our CQRS logic with the notification infrastructure is the [CommandBridge](src/main/java/com/example/cqrs/async/CommandBridge.java).
It's essentially a wrapper around OpenCQRS' [CommandRouter](https://github.com/open-cqrs/opencqrs/blob/main/framework/src/main/java/com/opencqrs/framework/command/CommandRouter.java)
that combines it with the `SubscribableChannel` Bean configured earlier and can be used in the router's place.

It adds two ways to issue commands:

* [sendWaitingForEventsHandled](src/main/java/com/example/cqrs/async/CommandBridge.java#L54): Halts the primary (command-issuing) thread until a notification is received, then resumes execution.
* [sendThenExecute](src/main/java/com/example/cqrs/async/CommandBridge.java#L103): Halts the primary (command-issuing) thread until a notification is received, then executes the passed `Runnable`, and then resumes execution.

The first method is used in the [ReaderController](src/main/java/com/example/cqrs/rest/ReaderController.java#L31)
to halt execution and return the new reader's ID only after the command has been processed.

The second method implements more sophisticated logic when [lending](src/main/java/com/example/cqrs/rest/BookController.java#L37)
and [returning](src/main/java/com/example/cqrs/rest/BookController.java#L58) books.
In these cases, [Server-sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events) are returned to the client with an updated view of their lent-out books as soon as the [ReaderProjector](src/main/java/com/example/cqrs/domain/ReaderProjector.java) sends a notification.

In both cases, if no notification is received within 5 seconds, the method times out and throws an `InterruptedException`.

# Running it

To run this app in a multi-JVM setup, you need [Docker](https://www.docker.com/products/docker-desktop/) installed on your system. Once installed, follow these steps:

1.  Create a runnable JAR with `./gradlew bootJar` (if not already done).
2.  Start the app with `docker-compose up`.

This will:

1.  Create database and event store instances, including volumes and port mappings.
2.  Boot up multiple app instances to run in parallel.
3.  Create an Nginx reverse-proxy for the instances, available at http://localhost:8080.

You can now issue commands via HTTP and see the updated read-model in the response\!

For convenience, this repository provides a pre-made [collection](postman/OpenCQRS - Sample 02.postman_collection.json) and [environment](postman/OpenCQRS - Sample 02.postman_environment.json) for use in [Postman](https://www.postman.com/downloads/).

Each request in the collection corresponds to one of the commands mentioned in the [introduction](#subscribing-to-queries).