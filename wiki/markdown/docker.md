# Docker

In RemRem-Publish source code a Dockerfile is provided which helps the developer or user to build the local RemRem-Publish source code repository changes to a Docker image.
With the Docker image user can try-out the RemRem-Publish on a Docker Host or in a Kubernetes cluster.

## Requirements
- Docker 


  Linux: https://docs.docker.com/install/linux/docker-ce/ubuntu/

  
  Windows: https://docs.docker.com/docker-for-windows/install/


## Follow these step to build the Docker image.

1. Execute maven install to build the all components and RemRem-Publish
`mvn install -DskipTests`

This will produce a war file in the "publish-service/target" folder.

2. Change to publish-service directory: 
`cd publish-service`

3. Build the Docker image with the war file that was produced from previous step: 


`docker build -t remrem-publish --build-arg URL=./target/publish-service-<version>.war -f src/main/docker/Dockerfile .` 


Now docker image is built with tag "remrem-publish"

## Run Docker image on local Docker Host
To run the produced docker image on the local Docker host, execute this command: 


`docker run -p 8080:8080 --expose 8080 -e server.port=8080 -e logging.level.log.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e logging.level.com.ericsson.ei=DEBUG remrem-publish`

RabbitMq and other RemRem-Publish required components need to be running and configured via application properties that is provided to the docker command above. See the application.properties file for all available/required properties:
[application.properties](https://github.com/eiffel-community/eiffel-remrem-publish/blob/master/publish-service/src/main/resources/application.properties)

# Some info of all flags to this command


## RemRem-Publish Spring Properties


<B>"-e server.port=8080"</B> - Is the Spring property setting for RemRem-Publish application web port.


<B>"-e logging.level.root=DEBUG -e logging.level.org.springframework.web=DEBUG -e 
logging.level.com.ericsson.ei=DEBUG"</B> - These Spring properties set the logging level for the RemRem-Publish application. 


It is possible to set all Spring available properties via docker envrionment "-e" flag. See the application.properties file for all available RemRem-Publish Spring properties:


[application.properties](https://github.com/eiffel-community/eiffel-remrem-publish/blob/master/publish-service/src/main/resources/application.properties)


## Docker flags


<B>"--expose 8080"</B> - this Docker flag tells that containers internal port shall be exposed to outside of the Docker Host. This flag do not set which port that should be allocated outside Docker Host on the actual server/machine.


<B>"-p 8080:8080"</B> - this Docker flag is mapping the containers external port 8080 to the internal exposed port 8080. Port 8080 will be allocated outside Docker host and user will be able to access the containers service via port 8080.


When RemRem-Publish container is running on your local Docker host, RemRem-Publish should be reachable with address "localhost:8080/\<Rest End-Point\>" or "\<docker host ip\>:8080/\<Rest End-Point\>"


Another option to configure RemRem-Publish is to provide the application properties file into the container, which can be made in two ways:
1. Put application.properties file in Tomcat Catalina config folder in container and run RemRem-Publish:

`docker run -p 8080:8080 --expose 8080 --volume /path/to/application.properties:/usr/local/tomcat/config/application.properties remrem-publish`

2. Put application.properties file in a different folder in container and tell RemRem-Publish where the application.properties is located in the container:

`docker run -p 8080:8080 --expose 8080 --volume /path/to/application.properties:/tmp/application.properties -e spring.config.location=/tmp/application.properties remrem-publish`
