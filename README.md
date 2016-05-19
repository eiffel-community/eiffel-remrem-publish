# RemRem Produce

[![Build Status](https://travis-ci.org/Ericsson/eiffel-remrem-produce.svg?branch=master)](https://travis-ci.org/Ericsson/eiffel-remrem-produce)
[![](https://jitpack.io/v/Ericsson/eiffel-remrem-produce.svg)](https://jitpack.io/#Ericsson/eiffel-remrem-produce)


RemRem Produce is a microservice allowing sending of messages to a topic based exchange on a RabbitMQ Server. It has an endPoint that must be called 
[a relative link](producer/msg)

## How to Install?
Binary is relased via jitPack and latest version can be accessed via 
[Latest Version Binary](https://jitpack.io/com/github/Ericsson/eiffel-remrem-produce/0.1.0/eiffel-remrem-produce-0.1.0.war)

RemRem Produce microservice in this repository are licensed under the [Apache License 2.0](./LICENSE).

## How to use?
Two parameters need to be provided:
* rabbitmq.exchange.name (String)
* rabbitmq.host (String)

For stand-alone deployment, it can be utilized like this:
	java -Drabbitmq.host=127.0.0.1 -Drabbitmq.exchange.name=eiffel.poc -jar eiffel-remrem*

Binary comes in War format which allows easy deployment in application servers such as Tomcat. In that case, these parameters must be provided to java process running tomcat.

__IMPORTANT NOTICE:__ The contents of this repository currectly reflect a __DRAFT__. The Eiffel framework has been used in production within Ericsson for several years to great effect; what is presented here is a revision and evolution of that framework - an evolution that is currently ongoing. In other words, anything in this repository should be regarded as tentative and subject to change. It is published here to allow early access and trial and to solicit early feedback.
