

Build RemRem-Publish
1. Build RemRem-Publish service artifact:
cd (git root dir)
mvn package -DskipTests -pl publish-service/ -am 

2. Build RemRem-Publish Docker image:
cd (git root dir)
docker build -t remrem-publish:<version> --build-arg URL=./publish-service/target/publish-service-<version>.war -f publish-service/src/main/docker/Dockerfile .

3. Run RemRem-Publsih
docker run --name remrem-publish -p 8080:8080 -v ./publish-service/src/main/resources/application.properties:/usr/local/tomcat/config/application.properties remrem-publish:<version>
