## 🏛 Software Architecture

### Backend

In general, we lean on ideas from hexagonal architecture and Domain Driven Design.

We use a gradle multi-project build with the following subprojects:
* web
    * offers REST API
    * controllers
    * hosts frontend
* domain
    * domain model classes
        * immutable
        * aggregate root entity responsible for keeping aggregate consistent
        * methods to change values (available at aggregate root) return `SourcingEvent`
    * services
        * call aggregate root to change values within aggregate, persist `SourcingEvent(s)`
    * interfaces for ports
* persistence (driven port)
    * responsible for persisting sourcing events into database
* app
    * configures other modules
    * contains `main()` method

Aggregates build another dimension of structure. Thus, each subproject itself is structured into the aggregates.
This theoretically enables splitting out aggregates into own services.

The design and implementation are inspired by the following projects:
* [Hexagonal Architecture with Kotlin, Ktor and Guice](https://hackernoon.com/hexagonal-architecture-with-kotlin-ktor-and-guice-f1b68fbdf2d9) / [GitHub](https://github.com/sgerber-hyperanna/ktor-hexagonal-multi-module-template)
* [ddd-by-examples/library](https://github.com/ddd-by-examples/library)
* [12 Things You Should Know About Event Sourcing](http://blog.leifbattermann.de/2017/04/21/12-things-you-should-know-about-event-sourcing/)
