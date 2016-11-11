package com.ericsson.eiffel.remrem.publish.config;

public interface PropertiesConfig {
    String MESSAGE_BUS_HOST = "com.ericsson.eiffel.remrem.publish.messagebus.host";
    String MESSAGE_BUS_PORT = "com.ericsson.eiffel.remrem.publish.messagebus.port";
    String EXCHANGE_NAME = "com.ericsson.eiffel.remrem.publish.exchange.name";
    String USE_PERSISTENCE = "com.ericsson.eiffel.remrem.publish.use.persistence";
    String CLI_MODE = "com.ericsson.eiffel.remrem.publish.cli.mode";
    String INVALID_EVENT_CONTENT = "Invalid event content, client need to fix problem in event before submitting again";
    String INVALID_MESSAGE = "INVALID_MESSAGE";
    String SUCCEED = "succeed";
    String SUCCESS = "SUCCESS";

    String EVENT_ID = "eventId";
    String ID = "id";
    String META = "meta";
    String EIFFEL_MESSAGE_VERSIONS = "eiffelMessageVersions";
}
