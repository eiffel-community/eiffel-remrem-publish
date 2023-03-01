# Run RemRem-Publish Applciation

RemRem-Publish application can be executed with Maven command or with the maven built war file.

## Requirements

- Java
- Maven

## Run RemRem-Publish With Maven Command

1. Change to service directory: 
`cd publish-service`

2. Execute maven command to build and run RemRem-Publish:
`mvn spring-boot:run`


## Run RemRem-Publish With Java Command

1. Go to RemRem-Publish git root directory

2. Execute maven package command to build the RemRem-Publish war file:
`mvn package -DskipTests`

This will produce a war file in the "target" folder.
The RemRem-Publish released war file can be downloaded from JitPack.

3. Run RemRem-Publish application war file
There is some alternatives to execute the war file with java command.
`java -classpath publish-service/target/publish-service-2.0.26.war org.springframework.boot.loader.WarLauncher`
or
`java -jar publish-service/target/publish-service-2.0.26.war`


Provide customized RemRem-Publish application.properties configuration via the spring.config.location java flag which need to be appended to the java command line:
`-Dspring.config.location=/path/to/application.properties`


## Override RemRem-Publish Eiffel Protocol Version

Eiffel-RemRem Protocol versions is developed and released by Github project:
https://github.com/eiffel-community/eiffel-remrem-semantics

Eiffel-RemRem-Semantic versions is released in Jitpack and can be downloaded as jar file.
Eiffel-Remrem-Semantic releases version jar files:
https://jitpack.io/com/github/eiffel-community/eiffel-remrem-semantics

Example of one Eiffel-Semantic-version Jitpack download url address:
https://jitpack.io/com/github/eiffel-community/eiffel-remrem-semantics/2.2.1/eiffel-remrem-semantics-2.2.1.jar

Eiffel-RemRem-Publish is released with a built-in eiffel-semantic protocol version which can be overridden with a different eiffel-semantic version by adding the external eiffel-semantic version jar file to classpath.

Execute RemRem-Publish application with external eiffel-semantic version jar file to classpath:
`java -classpath /path/to/protocol/eiffel-remrem-semantics-2.2.2.jar:publish-service/target/publish-service-2.0.26.war org.springframework.boot.loader.WarLauncher`

Another alternative (works only with Java 8):
`java -Djava.ext.dirs=/path/to/protocol/eiffel-remrem-semantics-2.2.2.jar -jar publish-service/target/publish-service-2.0.26.war`

Go to http://localhost:8080/versions and "semanticsVersion" field should show the overridden eiffel-semantic version.

