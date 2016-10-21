# Eiffel RemRem
Eiffel RemRem is a project undertaken to establish a communication layer between messagebus technologies and rest of Eiffel project in order to accomplish:
- Technology agnosticism
- Separation of concerns
- Ability to secure message bus entry/exit points
- Ability to introduce message validation

## RemRem Architecture
![RemRem Architecture](https://github.com/Ericsson/eiffel-remrem/raw/master/media/remrem_architecture.png "RemRem Architecture")

Architecture of remrem is based on Microservice and API design principles utilizing state of the art in development.

### Principles
- Microservices driven
 - Independent deployment
 - Decoupled services
 - No shared libraries
 - Stateless
 - Isolates failures
 - Decentralized

## RemRem Components
- Generate (Microservice): Can be used to generate validated Eiffel messages. [Generate Github Repo](https://github.com/Ericsson/eiffel-remrem-generate)
- Publish (Microservice): Can be used to publish Eiffel messages. [Publish Github Repo](https://github.com/Ericsson/eiffel-remrem-publish)
- Semantics (Library): Injectable library used with Generate Microservice to enable generation of new Eiffel messages. [Semantics Github Repo](https://github.com/Ericsson/eiffel-remrem-semantics)
- Shared (Library): Interface information used in injecting message libraries. Utilized by Semantics and Eifel3Messaging innersource projects [Shared Github Repo](https://github.com/Ericsson/eiffel-remrem-shared)

##  Announcements
__Eiffel, RemRem and the examples in this repository are licensed under the Apache License 2.0.__

## Building and releasing
For every RemRem component it is needed to create a new release tag in the component's repository on github. Once the tag is created the build for the new release tag will be started when you visit the component's page on jitpack.io (i.e. https://jitpack.io/#Ericsson/eiffel-remrem-generate) and published on component's page on jitpack.io or when another application is build and it is dependent of the new release tag from jitpack.io. 


__IMPORTANT NOTICE__: The contents of this repository currently reflect a DRAFT. The Eiffel framework has been used in production within Ericsson for several years to great effect; what is presented here is a revision and evolution of that framework - an evolution that is currently ongoing. In other words, anything in this repository should be regarded as tentative and subject to change. It is published here to allow early access and trial and to solicit early feedback.

