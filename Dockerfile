FROM openjdk:12-jdk-alpine

COPY tsa-service-web/target/tsa-service-web-*.jar /tsa-service/tsa-service-web.jar
COPY tsa-service-web/src/main/resources/application.properties /tsa-service/application.properties
RUN chown -R 1000:1000 /tsa-service

WORKDIR /tsa-service

EXPOSE 8080
ENTRYPOINT ["java","-jar","/tsa-service/tsa-service-web.jar","-f","/tsa-service/application.properties"]