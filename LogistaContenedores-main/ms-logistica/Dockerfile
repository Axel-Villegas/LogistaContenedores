# Etapa 1: Construcción (Build)
# Usamos una imagen de Maven con JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo el pom.xml para descargar dependencias en una capa separada
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el resto del código fuente y construir
COPY src ./src
RUN mvn package -Dmaven.test.skip=true

# Etapa 2: Ejecución (Runtime)
# Usamos una imagen ligera de JRE 21 de Eclipse Temurin (¡ESTA ES LA CORRECCIÓN!)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copiar el JAR construido de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto que definimos en el application.yml (8082)
EXPOSE 8082

# Comando para correr la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]