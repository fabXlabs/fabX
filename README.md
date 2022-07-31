# 🔐 fabX

fabX is an access system for fab labs, maker spaces, hack spaces, etc. These spaces typically have to ensure that their
members only use tools/machines that they have a qualification for. With fabX, each member gets an NFC card that enables
them to use the tools they are qualified for.

## Requirements

TODO

## Installation

TODO

## Setup / Documentation

Here is an overview for a minimal set up of a new fabX installation:

* Create a personal administrator account.
    * Login with username "admin" and password "password".
    * Create a new user with your personal details.
    * Add a username password identity to your personal account.
    * Set your personal account as admin.
    * Delete the original "admin" account.
* Create other users (e.g. one for each space member).
    * Create new user with their details.
    * Add card identities for each of them (and yourself).
* Create tools: each tool represents a tool in your space (e.g. a laser cutter, a 3D printer, ...)
* Create qualifications: a qualification represents a course a member has visited / completed that qualifies them to use
  a set of tools
    * a set of tools can also just be a single tool
    * e.g. your space might have a single course to be able to use all 3D printers -> create a single qualification here
    * now set the qualification as required for the different tools
    * e.g. set the 3D printing course as a qualification for each 3D printer
* Setup instructors: each user can be an instructor for a set of qualifications. Being an instructor for a qualification
  enables giving the qualification to other users.
* Add devices: devices are the small interfaces that read members' access cards and turn tools on and off
    * devices are register themselves when first turned on
    * configure connection of tools to devices

Now each space member is able to use the tools they are qualified for with their personal access card.

## 🛠 Tech Stack

* [Kotlin](https://kotlinlang.org) on JVM
* [Arrow Core](https://arrow-kt.io/docs/core/)
* [Ktor](https://ktor.io) framework
* [Exposed](https://github.com/JetBrains/Exposed) framework
* [PostgreSQL](https://www.postgresql.org) database
* Testing
    * [JUnit 5](https://junit.org/junit5/)
    * [assertk](https://github.com/willowtreeapps/assertk)
    * [Testcontainers](https://www.testcontainers.org)

## 🧑‍💻 Development

Contributions are welcome. Start by reading the [technical documentation](docs).

## ⚖️ License

TODO