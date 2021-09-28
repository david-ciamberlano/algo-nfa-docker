FROM openjdk:11
EXPOSE 8080

COPY maven/algo-nfa-1.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
