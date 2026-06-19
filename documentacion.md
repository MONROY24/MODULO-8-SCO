# Documentación Técnica: Sistema Contable Computarizado (Módulo 8)

## 1. Introducción
El **Módulo 8** integra Inteligencia Artificial (IA) al Sistema Contable Computarizado para ofrecer capacidades de **Análisis Predictivo**. Su objetivo principal es proyectar compras y ventas futuras basándose en el historial financiero de las empresas, permitiendo generar proyecciones, medir rentabilidad y sugerir recomendaciones.

---

## 2. Arquitectura del Sistema
El proyecto sigue el patrón de arquitectura **MVC (Modelo - Vista - Controlador)** para separar la lógica de negocio, el acceso a datos y la interfaz de usuario.

### 2.1. Modelo (`com.sistemacontable.modulo8.modelo`)
- **`RegistroFinanciero`**: POJO que mapea las facturas y el resumen consolidado de compras y ventas de un mes específico.
- **`ResultadoPrediccion`**: Encapsula la respuesta del motor de IA, conteniendo las métricas predictivas (predicción de compras y ventas, utilidad proyectada, nivel de confianza, etc.).
- **`EmpresaItem`**: POJO ligero para mapear el ID y Nombre Comercial de las empresas para la UI.

### 2.2. Acceso a Datos (`com.sistemacontable.modulo8.dao`)
- **`ConexionDB`**: Gestiona el pool de conexiones utilizando **HikariCP** para maximizar el rendimiento y la estabilidad. Los parámetros se extraen de `config/db.properties`.
- **`DatosFinancierosDAO`**: Contiene las consultas SQL nativas (`PreparedStatement`) para extraer el historial mensual de ventas y compras consolidado, evitando el problema del producto cartesiano usando subconsultas en SQL.

### 2.3. Lógica de Negocio y Servicios (`com.sistemacontable.modulo8.servicio`)
- **`MotorPrediccionIA`**: Es el cerebro del módulo. Implementa una **Red Neuronal Artificial (Multilayer Perceptron - MLP)** programada íntegramente en Java puro (sin dependencias de Machine Learning de terceros).

### 2.4. Vista y Controlador (`com.sistemacontable.modulo8.vista` / `controlador`)
- **`AnalisisControlador`**: Orquesta el flujo de ejecución utilizando el patrón `SwingWorker` para correr los cálculos matemáticos en segundo plano, evitando el congelamiento de la UI.
- **`VentanaAnalisis`**: La GUI principal construida con `Java Swing`.
- **`PanelGrafica`**: Componente de dibujado personalizado (`Graphics2D`) para renderizar el historial real y la predicción gráfica de manera estética.

### 2.5. Utilidades (`com.sistemacontable.modulo8.util`)
- Sistema avanzado de exportación a PDF **sin usar librerías de terceros** (como PDFBox o iText).
- Usa una arquitectura modular: `PlantillaPDF`, `GeneradorTablasPDF` y `GeneradorGraficaPDF`, ensambladas por el orquestador `ExportadorPDF`.

---

## 3. Red Neuronal Multicapa (MLP)

El algoritmo predictivo no utiliza regresión lineal simple; emplea una red neuronal Feed-Forward entrenada mediante *Backpropagation* y descenso de gradiente (Gradient Descent).

### 3.1. Arquitectura de la Red
- **Capa de Entrada (Input)**: 3 neuronas (Mes norm., Ventas Anteriores norm., Compras Anteriores norm.)
- **Capa Oculta 1**: 8 neuronas con función de activación **ReLU** ($f(x) = \max(0, x)$).
- **Capa Oculta 2**: 4 neuronas con función de activación **ReLU**.
- **Capa de Salida**: 2 neuronas con función de activación **Sigmoidal** para normalizar los valores de salida al rango $(0, 1)$.

### 3.2. Hiperparámetros y Entrenamiento
- **Épocas (Epochs)**: 5,000 iteraciones para garantizar la convergencia.
- **Tasa de Aprendizaje (Learning Rate)**: $0.05$ inicial con un **Step Decay** del 10% cada 1,000 épocas. Esto permite una convergencia veloz al inicio y estabilidad al acercarse al mínimo local del error (fine-tuning final).
- **Cálculo de Error (Loss Function)**: Error Cuadrático Medio (MSE - Mean Squared Error).

---

## 4. Normalización de Datos (`PreprocesadorDatos`)
La red neuronal requiere entradas en escala $[0, 1]$.
- El algoritmo halla los máximos históricos de compras y ventas de la empresa y normaliza todo el dataset (Min-Max Scaling con un $10\%$ de margen superior para evitar saturar las neuronas).
- Antes de devolver el `ResultadoPrediccion`, el motor des-normaliza la respuesta (multiplica por los factores originales) para devolver valores monetarios exactos.

---

## 5. Módulo de Logging
El sistema de depuración fue refactorizado usando `java.util.logging.Logger`, logrando que todos los mensajes de consola (SQL y reportes del motor) se integren con los estándares de la plataforma sin ensuciar la salida `System.out` ni `System.err`.

---

## 6. Integración PDF
El documento es generado renderizando los componentes en memoria usando arreglos binarios y empaquetándolos como un archivo regido por el estándar PDF-1.4. Esta solución reduce enormemente el peso del ejecutable final (`.jar`) al eliminar la necesidad de bibliotecas gigantescas, y demuestra la alta capacidad matemática y binaria del sistema base de Java.
