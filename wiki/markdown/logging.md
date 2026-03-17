## Logging
REMReM Publish application logging is implemented using the logback-classic. It requires user to configure the logback.xml file.
When used without logback.xml log messages will look as below:

```
2023-06-20 14:24:31.426  INFO 17 --- [apr-8080-exec-1] c.e.e.remrem.publish.helper.RMQHelper    : Connected to RabbitMQ.
2023-06-20 14:24:31.431  INFO 17 --- [apr-8080-exec-1] c.e.e.remrem.publish.helper.RMQHelper    : Published message with size 373 bytes on exchange 'ei-poc-4' with routing key 'eiffel.cm.scmchange.notag.eiffel'
20-Jun-2023 14:24:31.476 FINE [http-apr-8080-exec-1] org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor.writeWithMessageConverters Written [com.ericsson.eiffel.remrem.publish.service.SendResult@792fec46] as "application/json" using [com.ericsson.eiffel.remrem.publish.config.GsonHttpMessageConverterWithValidate@5eb84388]
2023-06-20 14:24:31.476 DEBUG 17 --- [apr-8080-exec-1] o.s.w.s.m.m.a.HttpEntityMethodProcessor  : Written [com.ericsson.eiffel.remrem.publish.service.SendResult@792fec46] as "application/json" using [com.ericsson.eiffel.remrem.publish.config.GsonHttpMessageConverterWithValidate@5eb84388]
```

To configure the logback.xml file use -Dlogging.config=path/logback.xml

To get info about logback configurations see [here](https://logback.qos.ch/manual/configuration.html).

To enable log level changes at run time without restart using actuator endpoint.
Do the changes using curl command like this:
curl -X POST http://localhost:8080/actuator/loggers/com.ericsson.eiffel.remrem -H "Content-Type: application/json" -d '{"configuredLevel": "DEBUG"}'