FROM maven AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn -f /pom.xml clean package -DskipTests

FROM openjdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]