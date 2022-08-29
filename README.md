# 🔐 fabX

fabX is an access system for fab labs, maker spaces, hack spaces, etc. These spaces typically have to ensure that their
members only use tools/machines that they have a qualification for. With fabX, each member gets an NFC card that enables
them to use the tools they are qualified for.

## ✅ Features

* each user has their own access card which personally identifies them
* each user can have a number of qualifications
* each tool can have a number of qualifications required to be able to use the tool
* each qualification can allow usage of one or more tools
* different modes for tools
    * unlock mode: short pulse on unlock, e.g. for door openers or drawers
    * keep mode: output as long as card is nearby card reader, e.g. for laser cutters
* multiple tools can be attached to a single device, the device shows a selection screen for the tool to be used
* multiple administrators
* (RESTful) API allowing for extension

## ❌ Limitations

These are features which can be found in other systems for this use-case but we currently do not intend to implement. If
you require these or other features and would like to extend fabX, you're welcome to open an issue for discussion.

* no booking system
* no tracking of machine-time
* no payment
* While the devices have a (limited) caching mechanism built-in, they work best with connection to the backend. The
  backend can run on-site (i.e. in the LAN of the space), but e.g. multiple backend instances are currently not
  supported.

## Installation

fabX is provided as a [Docker](https://www.docker.com) image. Additionally, a Postgres database is required.
See [docker-compose.yml](docker-compose.yml) for reference. Use this file as a base if you want to self-host.

If you do not want to self-host, [Heroku](https://www.heroku.com/pricing) is an option. See their documentation on
[how to deploy an existing Docker image](https://devcenter.heroku.com/articles/container-registry-and-runtime#pushing-an-existing-image)
. This process is shown in the [GitHub Action to deploy the DEV stage](.github/workflows/heroku-dev.yml). 

## Setup / Documentation

Here is a general overview how to set up a new fabX installation:

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
    * devices register themselves when first turned on
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
