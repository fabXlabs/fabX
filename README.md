# 🔐 fabX

fabX is an access system for fab labs, maker spaces, hack spaces, etc. These spaces typically have to ensure that
their members only use tools/machines that they have a qualification for. With fabX, each member gets an NFC card
that enables them to use the tools they are qualified for. Administrators create users and define instructors.
Instructors enter that they've held a course for some members. They can then instantly use the tool with their
access card.

## Requirements

TODO

## Installation

TODO

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

## 🧑‍💻 Development

Contributions are welcome. Start by reading the [technical documentation](docs).

## ⚖️ License

TODO