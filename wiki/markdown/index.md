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

## Compatibility
Both [`generate`](https://github.com/eiffel-community/eiffel-remrem-generate) and `publish` services use [`semantics`](https://github.com/eiffel-community/eiffel-remrem-semantics) library. Below is compatibility table of particular versions.
| `semantics` | `generate`          | `publish`           |
|-------------|---------------------|---------------------|
| `2.0.3`     | `2.0.2`             | `2.0.0`             |
| `2.0.4`     | `2.0.3`             | `2.0.1`             |
| `2.0.5`     | `2.0.4`             | `2.0.2` - `2.0.5`   |
| `2.0.6`     | `2.0.5` - `2.0.9`   | `2.0.6` - `2.0.9`   |
| `2.0.7`     |                     | `2.0.10`            |
| `2.0.8`     | `2.0.10`            | `2.0.11`            |
| `2.0.9`     | `2.0.11`            | `2.0.12`            |
| `2.0.11`    |                     | `2.0.13`            |
| `2.0.12`    | `2.0.12` - `2.0.13` | `2.0.14` - `2.0.15` |
| `2.0.13`    | `2.0.14` - `2.0.17` | `2.0.16` - `2.0.22` |
| `2.1.0`     | `2.1.0` - `2.1.2`   |                     |
| `2.2.1`     | `2.1.3` - `2.1.4`   | `2.0.23`            |