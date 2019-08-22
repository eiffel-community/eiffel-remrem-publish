# Running RemRem-Publish

RemRem-Publish can be run from source code with maven command and with RemRem-Publish artifact with Java command or in a Tomcat instance.

## Maven

To run RemRem-Publish from source code root directory with maven command and RemRem-Publish Spring/Java properties provided on command line:

    mvn spring-boot:run -Dlogging.level.root=DEBUG -Dgenerate.server.uri=http://127.0.0.1:8080

Run RemRem-Publish with external configuration file:

    mvn spring-boot:run -Dlogging.level.root=DEBUG -Dspring.config.location=/path/to/application.properties

All available RemRem-Publish properties can be found in [application.properties](https://github.com/eiffel-community/eiffel-remrem-publish/blob/master/publish-service/src/main/resources/application.properties) example file.

## Java

It is also possible to build and run RemRem-Publish war file, from publish-service folder:

    mvn package -DskipTests
    java -jar publish-service/target/publish-service.war -Dlogging.level.root=DEBUG -Dgenerate.server.uri=http://127.0.0.1:8080

Or:

    java -jar publish-service/target/publish-service.war --logging.level.root=DEBUG --spring.config.location=/path/to/application.properties

## Tomcat

It is also possible to execute RemRem-Publish in a Tomcat instance:

**1** Place publish-serice.war file in Tomcat webapp folder, which commonly has the path: (catalina home)/webapp.

**2** Place the RemRem-Publish application.properties file in (catalina home)/config folder. Create config folder if it is not existing.

**3** Start Tomcat instance and RemRem-Publish should be started with the provided configuration in config folder.


RemRem-Publish and Spring properties can be loaded in several ways which is documented in [Spring documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
