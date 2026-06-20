# --- Fase 1: Compilar la aplicación ---
FROM eclipse-temurin:17-jdk-focal AS build

# Instalar Apache Ant (ya que el proyecto no usa Maven)
RUN apt-get update && apt-get install -y ant

# Copiar el código y compilar usando build.xml
WORKDIR /src
COPY . .
RUN ant jar

# --- Fase 2: Ejecutar la aplicación (con visor web VNC) ---
# Como es una app de escritorio (Swing) y Render no tiene pantalla,
# usamos esta imagen que permite ver la ventana desde el navegador web.
FROM jlesage/baseimage-gui:alpine-3.18-v4.6

# Instalar Java 17 JRE
RUN apk add --no-cache openjdk17-jre

# Copiar el JAR generado en la fase de construcción
WORKDIR /app
COPY --from=build /src/build/modulo8-ia.jar app.jar

# Configuración de la interfaz gráfica web
ENV APP_NAME="Modulo 8 IA - Analisis Predictivo"
ENV DISPLAY_WIDTH=1150
ENV DISPLAY_HEIGHT=800

# Configurar el puerto web. Usamos el puerto 10000 que expondremos en Render.
ENV WEB_PORT=10000
EXPOSE 10000

# Script de inicio requerido por la imagen base para arrancar tu app
RUN echo "#!/bin/sh" > /startapp.sh && \
    echo "exec java -jar /app/app.jar" >> /startapp.sh && \
    chmod +x /startapp.sh
