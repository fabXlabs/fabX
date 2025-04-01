## 🏛 Software Architecture

### Backend

In general, we lean on ideas from hexagonal architecture and Domain Driven Design. The following diagram provides an overview
over the different modules. The modules also correspond to Gradle subprojects. Each arrow represents a dependency, i.e.
all modules depend on the domain module.

![Module architecture overview](./img/module-architecture.png)

Note: This diagram was drawn with [Excalidraw](https://excalidraw.com/).
The source is [module-architecture.excalidraw](./img/module-architecture.excalidraw).

* **domain** module: Contains the domain logic, including the domain models and services. 
  * Domain models include as much domain logic as possible (otherwise it would be an "anemic domain model"). 
    They are immutable, i.e. they can only produce sourcing events which are then written into persistence. 
  * Domain services, ideally, are a thin wrapper around a call to one (or a few) method(s) offered by a domain model. 
    They orchestrate reading models from persistence and writing sourcing events to persistence.
  * Each aggregate has a root entity which is responsible for keeping the aggregate consistent.
  * Finally, the domain includes interfaces for ports.
* **web** module: the driving port including rest and websocket controllers
  * The REST API is primarily used by the frontend.
  * The websocket connection is used by the Devices.
* **persistence** module: driven port persisting sourcing events into the database.
* **app** module: a technical module for gluing together web, domain and rest. Also includes the main method.  

The aggregate concept is not only visible in the domain module but builds a dimension of structure passing through all modules.
I.e., the code in each module is structured by the aggregates. This theoretically enables a technical split by aggregate.

#### Further reading

The design and implementation are inspired by the following projects:
* [Hexagonal Architecture with Kotlin, Ktor and Guice](https://hackernoon.com/hexagonal-architecture-with-kotlin-ktor-and-guice-f1b68fbdf2d9) / [GitHub](https://github.com/sgerber-hyperanna/ktor-hexagonal-multi-module-template)
* [ddd-by-examples/library](https://github.com/ddd-by-examples/library)
* [12 Things You Should Know About Event Sourcing](http://blog.leifbattermann.de/2017/04/21/12-things-you-should-know-about-event-sourcing/)
