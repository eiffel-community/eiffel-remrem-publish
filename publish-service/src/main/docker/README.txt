A: Build RemRem-Publish Docker image based on RemRem-Publish from an Artifactory, e.g. Jitpack:
cd (git root dir)/publish-service
docker build -t remrem-publish:<version> --build-arg URL=https://jitpack.io/com/github/eiffel-community/eiffel-remrem-publish/publish-service/<version>/publish-service-<version>.war -f src/main/docker/Dockerfile .



B: Build RemRem-Publish based on local RemRem-Publish source code changes
1. Build RemRem-Publish service artifact:
cd (git root dir)
mvn package -DskipTests

2. Build RemRem-Publish Docker image:
cd (git root dir)/publish-service
docker build -t remrem-publish --build-arg URL=./target/publish-service-<version>.war -f src/main/docker/Dockerfile .


