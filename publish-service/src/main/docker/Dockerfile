FROM tomcat:9.0.58-jdk17-openjdk-slim
MAINTAINER Eiffel-Community
ARG URL
RUN echo Building RemRem-Publish image based on artifact url: ${URL}
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD ./src/main/docker/health-check.sh  /eiffel/health-check.sh
ADD ./src/main/docker/start-service.sh /eiffel/start-service.sh
ADD ${URL} /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["/eiffel/start-service.sh"]
