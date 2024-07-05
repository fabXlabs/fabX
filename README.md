# 🔐 fabX

[![CI](https://github.com/fabXlabs/fabX/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/fabXlabs/fabX/actions/workflows/ci.yml)

fabX is an access system for fab labs, maker spaces, hack spaces, etc. These spaces typically have to ensure that their
members only use tools/machines that they have a qualification for. With fabX, each member gets an NFC card that enables
them to use the tools they are qualified for.

> [!TIP]
> For more details on the use-case fabX is developed for, read the [domain description](docs/domain-description.md).

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
* any changes to the system are stored as events, i.e. full auditability
  * to comply with data protection regulation, user data (personally identifiable information) can be fully 
    deleted (hard-deleted)
* (RESTful) API allowing for extension

## ❌ Limitations

These are features which can be found in other systems for this use-case but we currently do not intend to implement. 

> [!NOTE]
> If you require these or other features and would like to extend fabX, you're welcome to open an issue for discussion.

* no booking/calendar system
* no tracking of machine-time
* no payment system integration
* devices require a permanent connection to the backend

## Installation

fabX is provided as a container image. Additionally, a [Postgres](https://www.postgresql.org) database is required. 
See [docker-compose.yml](docker-compose.yml) for reference. Use this file as a base if you want to self-host.

You should be able to get started with the following steps:
* Install a container runtime (e.g., [Docker](https://docs.docker.com/get-docker/) or [Podman](https://podman.io/get-started))
  * For Podman, additionally, install a Compose runtime (e.g., https://github.com/containers/podman-compose )
* download the [docker-compose.yml](docker-compose.yml) file and store it in an appropriate location
* open a terminal in the location the docker-compose.yml file is stored
* run `docker compose up -d postgres` (replace `docker compose` by `podman-compose` if you use Podman) to start the database
* wait a few seconds for the database to be ready to accept connections
* run `docker compose up -d app` to start fabX
* open `http://localhost:8000` and continue with the Setup section below

To stop fabX and the database, run `docker compose down`.

To delete the database volume (attention: this deletes all data in the fabX installation), 
run `docker compose down --volumes`.

## Setup

Here is a general overview how to set up a new fabX installation:

* Login with the default username "admin" and password "password"
* Create a personal (administrator) User
    * Create a new User with your personal details
    * Add a Username Password Identity to your personal User
    * Set your personal User as Administrator
    * Logout
    * Login with your personal User
    * Delete the original "admin" User
* Create other Users (i.e., one for each space member)
    * Create new Users with their details
    * Add Card Identities for each of them (and yourself)
* Create Tools: each Tool represents a tool in your space (e.g. a laser cutter, a 3D printer, ...)
* Create Qualifications: a Qualification represents a course a member has visited / completed that qualifies them to use
  a set of Tools
    * a set of Tools can also just be a single Tool
    * e.g., your space might have a single course to be able to use all 3D printers -> create a single Qualification
    * now set the Qualification as required for the different Tools
    * e.g., set the 3D printing course as a Qualification for each 3D printer
* Setup Instructors: each User can be an instructor for a set of Qualifications. Being an Instructor for a Qualification
  enables giving the Qualification to other Users/Members.
* Add Devices: Devices are the small interfaces that read Users'/Members' access cards and turn Tools on and off
    * Devices register themselves when first turned on
    * attach Tools to Devices 

Now each space member is able to use the Tools they are qualified for with their personal access card.

## Documentation

### Roles and access rights

Documentation on the roles (member, instructor, admin) and their associated access rights can be found at
[docs/roles](docs/roles.md). 

### Instructor Documentation

Documentation for instructors (with screenshots) can be found at [docs/instructor](docs/instructor.md).

### Monitoring

The backend exposes metrics in Prometheus format at `/metrics`. The endpoint requires basic auth credentials configured
through environment variables.

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

GPLv3, see [LICENSE](LICENSE).
