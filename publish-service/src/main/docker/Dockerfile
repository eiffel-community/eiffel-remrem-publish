FROM tomcat:11.0-jdk21-openjdk-slim
LABEL org.opencontainers.image.authors="Eiffel-Community"
ARG URL
RUN echo Building RemRem-Publish image based on artifact url: ${URL}
RUN ["rm", "-fr", "/usr/local/tomcat/webapps/ROOT"]
ADD ./publish-service/src/main/docker/health-check.sh  /eiffel/health-check.sh
ADD ./publish-service/src/main/docker/start-service.sh /eiffel/start-service.sh
ADD ${URL} /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["/eiffel/start-service.sh"]
