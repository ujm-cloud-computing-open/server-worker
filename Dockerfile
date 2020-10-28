FROM openjdk:11
EXPOSE 8080
ADD target/serverworker.jar serverworker.jar
ENTRYPOINT ["java","-jar","/serverworker.jar"]