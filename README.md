# 🔐 fabXAccess

Backend for fabX access system. Written using [ktor](https://ktor.io) and [exposed](https://github.com/JetBrains/Exposed).

## 🛠 Tech Stack

* [Kotlin](https://kotlinlang.org) on JVM
* [Arrow Core](https://arrow-kt.io/docs/core/)
* [Ktor](https://ktor.io) framework (in rest module)
* [Exposed](https://github.com/JetBrains/Exposed) framework (in persistence module)
* [PostgreSQL](https://www.postgresql.org) database (recommended but others [should be supported](https://github.com/JetBrains/Exposed#supported-databases))
* Testing
    * [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/)
    * [JUnit 5](https://junit.org/junit5/) as test runner
    * [assertk](https://github.com/willowtreeapps/assertk) assertion library
    * [H2](http://www.h2database.com) in-memory database

## 🏛 Software Architecture

Hexagonal architecture + Domain Driven Design. Implementation inspired by:
* [Hexagonal Architecture with Kotlin, Ktor and Guice](https://hackernoon.com/hexagonal-architecture-with-kotlin-ktor-and-guice-f1b68fbdf2d9) / [GitHub](https://github.com/sgerber-hyperanna/ktor-hexagonal-multi-module-template)
* [ddd-by-examples/library](https://github.com/ddd-by-examples/library)

Modules:
* rest (driver port)
    * offers REST API
    * controllers
* domain
    * structured into different aggregates
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
