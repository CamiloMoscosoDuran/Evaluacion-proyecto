# Stage 1: Build with Maven
#FROM maven:3.9-amazoncorretto-25-alpine AS builder  -> Imagen no oficial (ligera)
#FROM maven:eclipse-temurin AS builder -> Imagen oficial (pesada)

FROM carolinaortiz/maven:3.9-amazoncorretto-25-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run with Java
#FROM eclipse-temurin:25-jre-alpine -> Imagen no oficial ligera
#FROM openjdk:12-ea-25-jdk-oracle  -> Imagen no oficial pesado

FROM carolinaortiz/eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]


#EJECUTAR UN CONTENEDOR DE SQL SERVER MEDIANTE COMANDOS
# docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=Admin12345" -p 1433:1433 --name sqlserver-nuevo -d carolinaortiz/sql-server:2022

#CONSTRUIR, EJECUTAR Y SUBIR IMAGEN A DOCKER HUB
# docker build -t carolinaortiz/springwebflux:1.0 .

#Construyendo la imagen con el Dockerfile específico
# docker build -t carolinaortiz/springwebflux:3.0 -f dockerfile-carolina/Dockerfile .

# docker run -d --name springwebflux -p 8082:8082 carolinaortiz/springwebflux:3.0
# docker push carolinaortiz/springwebflux:3.0


# DESCARGAR Y SUBIR MAVEN A TU USUARIO DOCKERHUB

# docker pull maven:3.9-amazoncorretto-25-alpine
# docker tag 26a8590fe36d carolinaortiz/maven:3.9-amazoncorretto-25-alpine
# docker push carolinaortiz/maven:3.9-amazoncorretto-25-alpine


# DESCARGAR Y SUBIR JDK25 A TU USUARIO DOCKERHUB

# docker pull eclipse-temurin:25-jre-alpine
# docker tag 5fcc27581b23 carolinaortiz/eclipse-temurin:25-jre-alpine
# docker push carolinaortiz/eclipse-temurin:25-jre-alpine