# fabXAccess

Backend for fabX access system. Written using [ktor](https://ktor.io) and [exposed](https://github.com/JetBrains/Exposed).

## Software Architecture

Hexagonal architecture + Domain Driven Design. Implementation inspired by:
* [Hexagonal Architecture with Kotlin, Ktor and Guice](https://hackernoon.com/hexagonal-architecture-with-kotlin-ktor-and-guice-f1b68fbdf2d9) / [GitHub](https://github.com/sgerber-hyperanna/ktor-hexagonal-multi-module-template)
* [ddd-by-examples/library](https://github.com/ddd-by-examples/library)

Modules:
* rest (driver port)
    * offers REST API
    * controllers
    * [Ktor](https://ktor.io) framework
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
    * responsible for persisting domain model into database
    * [Exposed](https://github.com/JetBrains/Exposed) framework
    * PostgreSQL (and H2 for testing)
* app
    * configures other modules
    * contains `main()` method
    
General:
* Kotlin on JVM
* tests
    * [kotlin.test](https://kotlinlang.org/api/latest/kotlin.test/) API for test annotations (e.g. `@Test`)
    * [JUnit 5](https://junit.org/junit5/) as test runner
    * [assertk](https://github.com/willowtreeapps/assertk) assertion library