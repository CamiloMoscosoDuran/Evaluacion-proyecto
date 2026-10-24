# Stage 1: Build with Maven
FROM camilomoscoso/maven:3.9-amazoncorretto-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with Java
FROM camilomoscoso/eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


# docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Admin12345" -p 1433:1433 --name sqlserver -d camilomoscoso/sql-server:2022


# docker build -t camilomoscoso/springboot-sqlserver:1.0 .

# docker run -d --name springboot-sqlserver -p 8085:8085 camilomoscoso/springboot-sqlserver:1.0

# docker push camilomoscoso/springboot-sqlserver:1.0

# docker build -t camilomoscoso/springboot-sqlserver:3.0 -f dockerfile-camilo/Dockerfile .


# DESCARGAR Y SUBIR MAVEN A TU USUARIO DOCKERHUB
# docker pull maven:3.9-amazoncorretto-25-alpine
# docker tag 26a8590fe36d camilomoscoso/maven:3.9-amazoncorretto-25-alpine
# docker push camilomoscoso/maven:3.9-amazoncorretto-25-alpine


# DESCARGAR Y SUBIR JDK25 A TU USUARIO DOCKERHUB
# docker pull eclipse-temurin:25-jre-alpine
# docker tag 5fcc27581b23 camilomoscoso/eclipse-temurin:25-jre-alpine
# docker push camilomoscoso/eclipse-temurin:25-jre-alpine
