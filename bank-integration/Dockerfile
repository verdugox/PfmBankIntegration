FROM openjdk:11
VOLUME /tmp
EXPOSE 9083
ADD ./target/bank-integration-0.0.1-SNAPSHOT.jar ms-banks.jar
ENTRYPOINT ["java", "-jar","/ms-banks.jar"]

