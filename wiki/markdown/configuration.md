# Configuration

RemRem-Publish is configured via Spring/Java properties that can be provided as Java properties or via a Spring application.properties file.
This page will describe which configuration and properties RemRem-Publish uses.

## Server configuration

RemRem-Publish web-server port is set by setting property "server.port":

    server.port=8080

In the example above RemRem-Publish will be started port 8080 and accessed with address "http://hostname:8080".
If RemRem-Publish is executed on same host as you accessing the RemRem-Publish service, then you can use the localhost address: "http://localhost:8080"


## MessageBus configuration

MessageBus(RabbitMq) connections is configured via property "rabbitmq.instances.jsonlist":

    rabbitmq.instances.jsonlist=[{ "mp": "eiffelsemantics", "host": "127.0.0.1", "port": "5672", "username": "guest", "password": "guest", "tls": "", "exchangeName": "amq.direct", "domainId": "eiffelxxx","createExchangeIfNotExisting":true }, \
    { "mp": "eiffel3", "host": "127.0.0.1", "port": "5672", "username": "guest", "password": "guest", "tls": "", "exchangeName": "amq.direct", "domainId": "eiffelxxx","createExchangeIfNotExisting":true }]

"rabbitmq.instances.jsonlist" property can be configured with one or several Messagebus per Eiffel protocol/version.
Most common is that you have only one MessageBus which uses one specific Eiffel version and the property should be configured with only one RabbitMq connection:

    rabbitmq.instances.jsonlist=[{ "mp": "eiffelsemantics", "host": "127.0.0.1", "port": "5672", "username": "guest", "password": "guest", "tls": "", "exchangeName": "amq.direct", "domainId": "eiffelxxx","createExchangeIfNotExisting":true }]

## RemRem-Generate configuration

RemRem-Publish uses the RemRem-Generate to generate Eiffel event contents.
RemRem-Generate connection properties:

    generate.server.uri=http://127.0.0.1:8080
    generate.server.contextpath=/generate

"generate.server.uri" property is the url address to the RemRem-Generate service.
"generate.server.contextpath" property is the contextpath in RemRem-Generate web server, if its configured with any context path.
If RemRem-Generate web-server is not configured with any context path, then this property can be leaved empty or set to "/".

## Logging levels
Logging levels is configured by properties:

    logging.level.root=INFO
    logging.level.org.springframework.web=INFO
    logging.level.com.ericsson.eiffel.remrem.producer=INFO

## Other properties and settings
All available RemRem-Publish properties can be found in [application.properties](https://github.com/eiffel-community/eiffel-remrem-publish/blob/master/publish-service/src/main/resources/application.properties) example file.
