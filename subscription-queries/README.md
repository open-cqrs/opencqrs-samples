
# Subscribing to Queries

---
**NOTE**

This tutorial assumes you have at least completed the official [OpenCQRS-tutorial](https://docs.opencqrs.com/tutorials/) beforehand.

---

One of the consequences of implementing the CQRS pattern is asynchronicity between writes/commands and reads/queries. 
This can become an issue when one wants to ensure that a given query result fully reflects the most recent command's effects.

The purpose of this app is to showcase how one can synchronize queries and commands again.

The app itself is a simple library app based around two domain entities:

- Readers
- Books

The actions one can perform are:

- Registering a reader, via the ``RegisterReaderCommand``
- Purchasing a book, via the ``PurchaseBookCommand``
- Lending and returning a book to/from a reader, via the ``LendBookCommand`` and ``ReturnBookCommand``, respectively

# The Problem

First, we should have a basic idea of the message and data flows in a standard Open CQRS-app:

![Single-JVM-Setup](diagrams/Single-JVM-Setup.svg)

First, a client makes a call to one of the app's endpoints **(1)**, which in turn causes it to issue a command to the command router **(2)**.
The command router then passes said command to the appropriate command handler **(3)**, which (among other things) writes one or more events to the event store **(4)**.
Each event may then be picked up by one or more event handlers **(5)**, which then execute the appropriate logic. A special case of such event handlers, called a _projector_,
updates the app's read-model by persisting an updated view to the (read-optimised) database **(6a)**. 
Said database can then be used by the app's controllers **(7)** to allow the client to query data through additional endpoints **(8)**.

The issue here is, that the message/data-flow *leaves* the confines of the app's JVM after publishing the events and then *reenters* them only once they are handled.
This means, that the initial thread issuing the command terminates immediately after it is handled, while the handling of the corresponding events is done in separate threads.
That way, the client can not know whether the data from the latest query they performed already incorporates the changes caused by their last command.

In a simple scenario where the client is only interacting with a single instance of the app, this issue can be resolved by syncing up writing and reading at the controller-level
and have the thread that received the request wait for the thread doing the projection **(6b)** and _then_ return the resulting view to the client as a response:
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
We ensure that a given read-model update corresponds to a given command by routing a command with a *correlation id* inside its metadata, which will be consequently passed inside the metadata of the *latest* event published by the command handler.

However, this breaks down in a more realistic, industry-grade setup where multiple instances of the app are running in parallel (usually containerized) and reverse-proxied by a load balancer (or something similar):

![Multi-JVM-Setup v2](diagrams/Multi-JVM-Setup.v2.svg)

Here, not only will the event handling be done by a different thread than the command handling, but it also might be done on a completely different JVM.

To solve the problem in this scenario, once can modify the single-jvm approach by instead of waiting for an update from another thread (which might never come), have the controller listen for updates from the database *directly*.

# One Solution

The solution exemplified by this app is built on two pillars:

- Using Postgres as our read-model database and leveraging its [NOTIFY-feature](https://www.postgresql.org/docs/current/sql-notify.html)
- Using Spring Integration's [JdbcChannelMessageStore-API](https://docs.spring.io/spring-integration/reference/jdbc/message-store.html) to listen to said notifications

First, we set up a [configuration](src/main/java/com/example/cqrs/configuration/CqrsConfiguration.java) in which we first ensure, that only one JVM ever gets to handle a given event:

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

Next, we configure a subscribable channels to listen for the database-notifications as per the [documentation](https://docs.spring.io/spring-integration/reference/jdbc/message-store.html#postgresql-push):

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

Once all this is done, we need a [sql file](schema.sql) which initializes the postgres to work with the aforementioned mechanisms.

The final piece connecting our CQRS-logic with the notification infrastructure is the [CommandBridge](src/main/java/com/example/cqrs/async/CommandBridge.java).
It is essentially a wrapper around OpenCQRS' [CommandRouter](https://github.com/open-cqrs/opencqrs/blob/main/framework/src/main/java/com/opencqrs/framework/command/CommandRouter.java)
and combines it with the `SubscribableChannel`-Bean configured above and can be used in the router's stead.

It adds two additional modes of issuing commands:

- [sendWaitingForEventsHandled](src/main/java/com/example/cqrs/async/CommandBridge.java#L54): Halt execution of the primary (i.e. command-issuing) thread until notification is received; Resume execution afterwards.
- [sendThenExecute](src/main/java/com/example/cqrs/async/CommandBridge.java#L103): Halt execution of the primary (i.e. command-issuing) thread until notification is received; Then execute the passed `Runnable` and then resume execution

The first one is used in the [ReaderController](src/main/java/com/example/cqrs/rest/ReaderController.java#L31) in order to halt execution and only return the new reader's ID once the command has been properly processed.

The second one is used to implement a more sophisticated logic while [lending](src/main/java/com/example/cqrs/rest/BookController.java#37) and [returning](src/main/java/com/example/cqrs/rest/BookController.java#58) books,
where [Server-sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events) are returned to the client with an updated view of their lent-out books as soon as a notification is received from the [ReaderProjector](src/main/java/com/example/cqrs/domain/ReaderProjector.java)

In both cases, if no notification is received within a given time window (5 seconds), the method times out and excepts with a `InterruptedException`.

# Running it

To be able to run this app in a multi-jvm setup, one needs [Docker](https://www.docker.com/products/docker-desktop/) on their system.
Once installed, perform the following steps:

1. Create a runnable JAR with ``./gradlew bootJar`` (if not done already)
2. Start the app with ``docker-compose up``

This will

1. Create an instance of the database and event store resp., including volumes and port mappings
2. Boot up multiple instances of the app to run in parallel
3. Create an nginx reverse-proxy for the instances, available under http://localhost:8080

You can now issue commands via HTTP and see the updated read-model as a response!

For convenience, this repository provides a pre-made [collection](postman/OpenCQRS%20-%20Sample%2002.postman_collection.json) and [environment](postman/OpenCQRS%20-%20Sample%2002.postman_environment.json)
for use in [Postman](https://www.postman.com/downloads/).

The requests inside the collection each correspond to one of the commands mentioned in the [introduction](#introduction).