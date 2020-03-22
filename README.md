## TSA service

TSA service is a service that:  
- Takes a PDF as input  
- Generates the PDF hash  
- Sends the hash to TSA (RFC3161)  
- Validates the returned timestamp token  
- Inserts the timestamp token to the PDF  
- Returns the timestamped PDF and the operation traces  

## Prerequisites

- JDK 12  
- Maven 3  

## Compilation

`$ mvn clean install`

## CI/CD

[![TSA service CI/CD status](https://github.com/bntan/tsa-service/workflows/TSA%20service%20CI/badge.svg)](https://github.com/bntan/tsa-service/actions)
- Sonar report is available on Sonarcloud: https://links.bntan.com/sonar-tsa-service  
- JAR is available on GitHub Packages: https://links.bntan.com/github-tsa-service  
- Docker image is available on Docker Hub: https://links.bntan.com/docker-tsa-service  

## Execution

### JAR
  
`$ java -jar tsa-service-web-[VERSION].jar -f application.properties` 

### Docker

`$ docker pull bntan/tsa-service`  
`$ docker run -p 8080:8080 --name tsa-service -t bntan/tsa-service`  

## Documentation

API documentation is available here: http://localhost:8080/bntan/service  

## URLs

Timestamp service URL: http://localhost:8080/bntan/service/timestamp  
Management URL: http://localhost:8080/bntan/service/management  
Prometeus URL: http://localhost:8080/bntan/service/management/prometheus  

For these URLs, use basic authentication:  
- Username is username  
- Password is password  
