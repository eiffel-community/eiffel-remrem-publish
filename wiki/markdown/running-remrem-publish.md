## Running RemRem-Publish

RemRem-Publish can be run from source code with maven command and with RemRem-Publish artifact with Java command or in a Tomcat instance.

To run RemRem-Publish from source code root directory with maven command:

    mvn spring-boot:run -Dlogging.level.root=DEBUG -D

Run RemRem-Publish with external configuration file:

    mvn clean spring-boot:run -Dlogging.level.root=DEBUG -Dspring.config.location=/path/to/application.properties

TO BE UPDATED