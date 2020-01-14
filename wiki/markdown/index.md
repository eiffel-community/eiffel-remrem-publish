## Introduction

REMReM (**REST Mailbox for Registered Messages**) is a set of tools that can be used to generate validated Eiffel messages and publish them on a RabbitMQ message bus. They can be run as micro services or as stand-alone CLI versions. For more details on the micro services and the REMReM design, see [Eiffel REMReM](https://github.com/eiffel-community/eiffel-remrem).

Eiffel REMReM Publish is a tool that can be used to publish Eiffel messages on a RabbitMQ message bus.

## Pre-requisites

*   JDK 8
*   Tomcat 8
*   RabbitMQ Server 3.6.x

For supporting latest features, Eiffel REMReM Publish should use the latest version of [Eiffel REMReM Semantics](https://github.com/eiffel-community/eiffel-remrem-semantics).

## Components

*   REMReM Publish CLI (Command Line Interface)
*   REMReM Publish Service