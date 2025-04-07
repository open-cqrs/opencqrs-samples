
# Introduction

Instead of showcasing a specific feature of th Open CQRS-framework, 
the purpose of this application is to provide an example of how to handle one of the issues that arise from the asynchronicity of writing and reading inherent to CQRS:

Ensuring that the data one queries from the read-model is consistent with one's latest updates to the write-model, i.e. that the changes caused by the latest command are reflected by the latest query.

The app itself is a simple library app based two domain entities:

- Readers
- Books

The actions one can perform are:

- Registering a reader, via the ``RegisterReaderCommand``
- Purchasing a book, via the ``PurchaseBookCommand``
- Lending and returning a book to/from a reader, via the ``LendBookCommand`` and ``ReturnBookCommand``, respectively

# The Problem

First, we should have a basic idea of the message and data flows in a standard Open CQRS-app:

![Single-JVM-Setup](https://github.com/user-attachments/assets/ed4e3380-534b-44e0-a03a-91fa51675d8a)

First, a client makes a call to one of the app's endpoints **(1)**, which in turn causes it to issue a command to the command router **(2)**.
The command router then passes said command to the appropriate command handler **(3)**, which (among other things) writes one or more events to the event store **(4)**.
Each event may then be picked up by one or more event handlers **(5)**, which then execute the appropriate logic. A special case of such event handlers, called a _projector_,
updates the app's read-model by persisting an updated view to the (read-optimised) database **(6a)**. 
Said database can then be used by the app's controllers **(7)** to allow the client to query data through additional endpoints **(8)**.

The issue here is, that the message/data-flow *leaves* the confines of the app's JVM after publishing the events and then *reenters* them only once they are handled.
This means, that the initial thread issuing the command terminates immediately after it is handled, while the handling of the corresponding events is done in separate threads.
That way, the client can not know whether the data from the latest query they performed already incorporates the changes caused by their last command.

In a simple scenario where the client is only interacting with a single instance of the app, this issue can be resolved by [syncing up](src/main/java/com/example/cqrs/service/SynchronizerService.java) writing and reading at the controller-level
and have the thread that received the request wait for the thread doing the projection **(6b)** and _then_ return the resulting view to the client as a response. 
We ensure that a given read-model update corresponds to a given command by routing a command with a *correlation id* inside its metadata, which will be consequently passed inside the metadata of the *latest* event published by the command handler.

However, this breaks down in a more realistic, industry-grade setup where multiple instances of the app are running in parallel (usually containerized) and reverse-proxied by a load balancer (or something similar):

![Multi-JVM-Setup v2](https://github.com/user-attachments/assets/36d9e5e5-8f74-4e70-bc27-ddcd37124216)

Here, not only will the event handling be done by a different thread than the command handling, but it also might be done on a completely different JVM.

To solve the problem in this scenario, once can modify the single-jvm approach by instead of waiting for an update from another thread (which might never come), have the controller listen for updates from the database *directly*.

# One Solution

The [solution](src/main/java/com/example/cqrs/service/PGNotifyService.java) exemplified by this app is built on two pillars:

- Using Postgres as our read-model database and leveraging its [notification mechanism](https://www.postgresql.org/docs/current/sql-notify.html)
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

Next, we configure a subscribable channel to listen for the database-notifications as per the [documentation](https://docs.spring.io/spring-integration/reference/jdbc/message-store.html#postgresql-push):

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
    public PostgresSubscribableChannel channel(
            PostgresChannelMessageTableSubscriber subscriber,
            JdbcChannelMessageStore messageStore) {
        return new PostgresSubscribableChannel(messageStore, "some group", subscriber);
    }
```

Once all this is done, we need a [sql file](schema.sql) which initializes the postgres to work with the aforementioned mechanisms.

In our controllers, we can now listen for database notifications given a correlation id for a command ([example](src/main/java/com/example/cqrs/rest/ReaderController.java#L48)).

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
