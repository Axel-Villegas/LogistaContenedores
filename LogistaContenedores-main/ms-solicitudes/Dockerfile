# Etapa 1: Construcción (Build)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente y construir
COPY src ./src
RUN mvn package -Dmaven.test.skip=true

# Etapa 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR construido
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de la aplicación (8081)
EXPOSE 8081

# Comando para correr
ENTRYPOINT ["java", "-jar", "app.jar"]