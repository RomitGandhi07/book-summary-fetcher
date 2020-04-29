FROM openjdk:8-alpine

COPY target/uberjar/task-wikipedia-endpoint.jar /task-wikipedia-endpoint/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/task-wikipedia-endpoint/app.jar"]
