# OpenCQRS – Sample Applications

![OpenCQRS](banner.png)


This repository contains a collection of sample applications. Each sample demonstrates how to implement a specific use case commonly encountered when developing software with the [OpenCQRS framework](https://www.opencqrs.com).


## About OpenCQRS and EventSourcingDB

### OpenCQRS

[**OpenCQRS**](https://www.opencqrs.com) is an innovative, opinionated and light-weight framework for the developing applications based on **CQRS (Command Query Responsibility Segregation)** and **Event Sourcing**.  

It provides the means to implement modern architecture patterns like hexagonal architecturs quite easily and with comprehensive sample apps that allow first experiments with CQRS/ES in a matter of minutes.

### EventSourcingDB

[**EventSourcingDB**](https://www.eventsourcingdb.io) is the database that adapts to your business processes like never before. It captures the semantics of your domain events and builds the perfect foundation for your event-driven architecture to take your business to the next level.

## Why OpenCQRS?

We’ve worked with CQRS and Event Sourcing for years – and none of the existing Java frameworks gave us what we needed:

    They were either too heavy, too leaky in abstraction, or lacked clear guidance for beginners.

    When EventSourcingDB emerged, we finally had the foundation we needed to build the framework we always wished had existed.

OpenCQRS is that framework.
Simple enough to get started in minutes. Powerful enough to grow with your system.

## Included Samples

The following scenarios are covered:

- **Filtering Event Streams**  
  Demonstrates how to tag events to efficiently filter the event stream.  
  → [View sample](./filtering-event-streams)

- **Subscribing to Queriess**  
  Shows how to wait for read-side projections to reflect the outcome of a command using polling or reactive subscriptions.  
  → [View sample](./subscription-queries)

- **Implementing Sagas**  
  Explains how to coordinate workflows across long-running, multi-system transactions are known as **Sagas**.  
  → [View sample](./implementing-sagas)

Each sample application can be run locally via `docker-compose` (see the corresponding `docker-compose.yml` files). Interaction is possible using the included Postman and Bruno API collections.

Refer to each app’s individual `README.md` for detailed instructions.


## Requirements

To run the samples locally, ensure the following tools are installed:

- Java 21+
- Docker
- Docker Compose

> ℹ️ *EventSourcingDB runs as a container via the provided `docker-compose.yml` files.*
