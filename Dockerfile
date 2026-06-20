# --- Fase 1: Compilar la aplicación (Maven) ---
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Copiar el POM y descargar dependencias
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
COPY config ./config
RUN mvn clean package

# --- Fase 2: Ejecutar la API REST ---
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
# Copiar el Fat JAR de la fase de construcción
COPY --from=build /app/target/modulo8-ia-jar-with-dependencies.jar /app/modulo8-ia.jar

# Copiar configuración (si es requerida en tiempo de ejecución, asumiendo db.properties)
# Render inyectará las variables de entorno, pero si la app lee config/db.properties local:
COPY config ./config

# Exponer el puerto por defecto de Javalin/Render
EXPOSE 8080

# Comando de inicio
CMD ["java", "-jar", "modulo8-ia.jar"]
