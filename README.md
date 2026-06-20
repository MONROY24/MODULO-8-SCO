# 🧠 Módulo 8: Análisis Predictivo con Inteligencia Artificial
### Sistema Contable Computarizado (SCO)
**Universidad de El Salvador (UES) - Facultad Multidisciplinaria de Occidente**
*Depto. de Ingeniería y Arquitectura | Carrera: Ingeniería de Sistemas Informáticos | Materia: Sistemas Contables*
*Proyecto Grupal*

![Java Version](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A22?style=for-the-badge&logo=apachemaven&logoColor=white)
![Javalin](https://img.shields.io/badge/Javalin-FF0000?style=for-the-badge&logo=javalin&logoColor=white)

Este repositorio contiene el **Módulo 8** del Sistema Contable Computarizado, dedicado a procesar el historial financiero de compras y ventas para inferir y predecir el comportamiento del mercado.

El motor de análisis es una **Red Neuronal Artificial (MLP)** nativa en Java. Recientemente, el proyecto ha sido migrado a una arquitectura Web (API REST + Frontend Responsive) para facilitar su acceso, modernizando la interfaz gráfica y permitiendo su despliegue en la nube (Render).

---

## 📑 Índice
1. [Características Principales](#-características-principales)
2. [Arquitectura del Sistema](#-arquitectura-del-sistema)
3. [Guía de Instalación Rápida](#-guía-de-instalación-rápida)
4. [Despliegue en la Nube (Render)](#-despliegue-en-la-nube-render)
5. [Documentación Técnica Oficial](#-documentación-técnica-oficial)

---

## 🚀 Características Principales
- **Inteligencia Artificial Pura en Java**: Red Neuronal con capa oculta construida matemáticamente desde cero para predecir ventas y compras futuras basadas en tendencias.
- **Interfaz Web Moderna y Responsiva**: Frontend limpio utilizando HTML/CSS/JS puro (Vanilla). Integración de temas dinámicos (Claro, Oscuro Gris, Oscuro Negro) adaptables a cualquier dispositivo móvil o tablet.
- **Gráficas en Tiempo Real**: Análisis visual e interactivo apoyado en `Chart.js`, que procesa los JSON enviados por el API.
- **Exportación en PDF**: Continúa soportando la generación de documentos PDF directamente desde el backend para reportes contables.
- **Conexiones Optimizadas**: Uso de `HikariCP` para manejar el pool de conexiones a la base de datos MariaDB/MySQL. Evita fugas de memoria y bloqueos de conexión.

---

## 📐 Arquitectura del Sistema
El sistema ha evolucionado a una estructura de **Cliente-Servidor (API REST)**:

- **Servidor Web (Javalin)**: El punto de entrada central (`ApiMain.java`). Gestiona los endpoints y sirve los archivos estáticos en el puerto especificado.
- **Capa DAO y BD**: Conexión a MariaDB manejada a través de `AppConfig` que detecta dinámicamente variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`), priorizando configuraciones de la nube. 
- **Controlador (`AnalisisRestController.java`)**: Interpreta las peticiones HTTP y orquesta la comunicación con el DAO y el Motor IA.
- **Motor de IA (`MotorPrediccionIA.java`)**: Conserva su robustez matemática. Escala datos (`Min-Max`) y entrena con `Backpropagation`.
- **Frontend (`index.html`, `styles.css`, `app.js`)**: Capa de presentación alojada en `src/main/resources/public`.

---

## 💻 Guía de Instalación Rápida (Local)

### Pre-requisitos
- **Java JDK 17** o superior.
- **Apache Maven 3.8+**.
- **MariaDB 10.5+** o **MySQL 8.0+**.

### Pasos de Despliegue
1. **Configura tu Entorno**:
   Asegúrate de configurar correctamente el archivo `config/db.properties` para modo local.
   ```properties
   db.driver=org.mariadb.jdbc.Driver
   db.url=jdbc:mariadb://localhost:3306/sistema_contable?useSSL=false
   db.user=root
   db.password=
   ```
2. **Puebla la Base de Datos**:
   Ejecuta el script base en tu gestor de base de datos:
   `source sql/01_setup_mock_data.sql`
3. **Compila y Ejecuta**:
   En la raíz del proyecto, utiliza Maven:
   ```bash
   mvn clean package
   java -jar target/modulo8-ia.jar
   ```
4. **Prueba el Sistema**:
   Abre tu navegador web en `http://localhost:8080/`

---

## ☁️ Despliegue en la Nube (Render)
El módulo está totalmente optimizado para servidores como **Render.com**.
- El **Dockerfile** en la raíz permite crear y levantar una imagen optimizada.
- Requiere configurar las **Environment Variables** en Render para conectar la base de datos externa:
  - `DB_URL`
  - `DB_USER`
  - `DB_PASSWORD`
- El puerto (`PORT`) es inyectado de forma dinámica por Render al contenedor.
- Se ha depurado completamente de librerías innecesarias de JS (como Node y Puppeteer) para que Maven ensamble un microservicio ultraligero y rápido.

---

## 📘 Documentación Técnica Oficial
Se ha liberado la Documentación ISO Arquitectónica original del módulo, que incluye:
- Matemáticas de la normalización paso a paso (Vmin, Vmax).
- Explicaciones de Forward y Backward Pass.
- Contratos de código fuente de cada componente. *(Refactorizado y extendido en base a la migración REST actual).*

**[Descarga / Abre el PDF del Manual Técnico Completo Aquí (40+ Páginas)](./documentacion_tecnica_sco.pdf)**

---
*Desarrollado para la materia de Sistemas Contables.*
