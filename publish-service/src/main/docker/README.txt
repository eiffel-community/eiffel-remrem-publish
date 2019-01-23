A: Build RemRem-Publish Docker image based on RemRem-Publish from an Artifactory, e.g. Jitpack:
cd (git root dir)/publish-service
docker build -t remrem-publish:0.6.8 --build-arg URL=https://jitpack.io/com/github/Ericsson/eiffel-remrem-publish/publish-service/0.6.8/publish-service-0.6.8.war -f src/main/docker/Dockerfile .



B: Build RemRem-Publish based on local RemRem-Publish source code changes
1. Build RemRem-publish service artiface:
cd (git root dir)
mvn package -DskipTests

2. Build RemRem-Publish Docker image:
cd (git root dir)/publish-service
docker build -t remrem-publish:0.6.8 --build-arg URL=./target/publish-service-0.6.9.war -f src/main/docker/Dockerfile .


