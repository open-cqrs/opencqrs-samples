
# OpenCQRS - Sample Apps

The repository is a collection of sample apps each showcasing how to implement a solution to a specific problem or use case one could encounter while developing
software with the [OpenCQRS-framework](https://www.opencqrs.com).

So far this includes:

- Using structured Command-subjects to filter the event trail [(see)](using-event-streams)
- Subscribing/Waiting for new query results after dispatching a command [(see)](subscription-queries)
- Implementing long-lived and distributed transactions aka. sagas [(see)](implementing-sagas)

Each sample app can be run locally via `docker-compose` (see the corresponding `docker-compose.yml` files) and interacted with via the provided Postman and Bruno API-collections.

See each app's individual READEME for more details.