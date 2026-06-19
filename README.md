# Módulo 8 – Análisis Predictivo con Inteligencia Artificial
### Sistema Contable Computarizado · Ciclo I-2026

---

## Índice
1. [Descripción General](#descripción-general)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Arquitectura MVC](#arquitectura-mvc)
4. [Configuración de Base de Datos](#configuración-de-base-de-datos)
5. [Cómo Ejecutar el Módulo](#cómo-ejecutar-el-módulo)
6. [Guía de Integración al Proyecto Unificado](#guía-de-integración-al-proyecto-unificado)
7. [Exportación a PDF](#exportación-a-pdf)
8. [Cómo Funciona la Red Neuronal](#cómo-funciona-la-red-neuronal)

---

## Descripción General

Este módulo implementa un sistema de **predicción de ventas y compras del mes siguiente**
usando una **Red Neuronal Artificial (MLP – Multilayer Perceptron)** implementada en Java puro,
sin dependencias externas de frameworks de Deep Learning.

El módulo es completamente autónomo y se conecta a la misma base de datos del proyecto
mediante tablas mock que pueden reemplazarse fácilmente al integrar con los otros módulos.

---

## Estructura del Proyecto

```
modulo8-ia/
├── .gitignore                              ← Archivos excluidos del repositorio
├── build.xml                              ← Build script (Apache Ant)
├── README.md                              ← Este archivo
│
├── config/                                ← Configuración externalizada
│   └── db.properties                      ← Credenciales de BD (NO versionar en producción)
│
├── lib/                                   ← Dependencias JAR
│   └── mariadb-java-client-3.5.6.jar     ← Driver JDBC (MySQL/MariaDB compatible)
│
├── sql/                                   ← Scripts de base de datos
│   └── 01_setup_mock_data.sql            ← Tablas y datos de prueba
│
├── docs/                                  ← Documentación técnica
│   └── arquitectura.md                   ← Diagrama de arquitectura detallado
│
└── src/main/java/com/sistemacontable/modulo8/
    ├── Main.java                          ← Punto de entrada
    ├── config/
    │   └── AppConfig.java                 ← Lee db.properties (credenciales externalizadas)
    ├── dao/
    │   ├── ConexionDB.java                ← Singleton thread-safe (double-checked locking)
    │   └── DatosFinancierosDAO.java       ← Consultas SQL multi-empresa
    ├── modelo/
    │   ├── RegistroFinanciero.java        ← Entidad de datos financieros
    │   └── ResultadoPrediccion.java       ← Resultado completo del análisis
    ├── servicio/
    │   ├── PreprocesadorDatos.java        ← Normalización Min-Max
    │   └── MotorPrediccionIA.java         ← Red Neuronal + Backpropagation + MSE logging
    ├── controlador/
    │   └── AnalisisControlador.java       ← Coordinador MVC
    ├── vista/
    │   ├── VentanaAnalisis.java           ← Pantalla principal (Swing mejorado)
    │   └── PanelGrafica.java             ← Gráfica con gradientes y curva suavizada
    └── util/
        └── ExportadorPDF.java            ← Generador PDF (offsets xref corregidos)
```

---

## Arquitectura MVC

```
┌─────────────────────────────────────────────────────────────────┐
│                         USUARIO                                  │
│         Selecciona empresa → Clic "Ejecutar Análisis"           │
└─────────────────────────┬───────────────────────────────────────┘
                          │ evento
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│  CONTROLADOR: AnalisisControlador                               │
│  · Recibe id_empresa de la Vista                                │
│  · Pide datos al DAO (en hilo de fondo – SwingWorker)          │
│  · Invoca el Motor IA                                           │
│  · Devuelve ResultadoPrediccion a la Vista                      │
│  · Coordina la exportación PDF                                  │
└──────────┬──────────────────────────────────────┬──────────────┘
           │                                      │
    ┌──────▼────────┐                    ┌────────▼─────────────┐
    │    MODELO     │                    │        VISTA         │
    │               │                   │                      │
    │ AppConfig     │                   │ VentanaAnalisis      │
    │ ConexionDB    │                   │ · Selector empresa   │
    │ DatosDAO      │                   │ · Botón Analizar     │
    │ PreprocesD.   │                   │ · 5 KPIs con gradiente│
    │ MotorIA       │                   │ · Gráfica mejorada   │
    │ ExportPDF     │                   │ · Tabla con colores  │
    │               │                   │ · Botón PDF          │
    └───────────────┘                    └──────────────────────┘
```

---

## Configuración de Base de Datos

Las credenciales ya **no están hardcodeadas** en el código. Se leen desde `config/db.properties`:

```properties
# config/db.properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/dbsco?useSSL=false&serverTimezone=UTC
db.user=root
db.password=
```

También puedes usar **variables de entorno** (tienen prioridad sobre el archivo):
```bash
set DB_URL=jdbc:mysql://miservidor:3306/miBD
set DB_USER=miusuario
set DB_PASSWORD=mipassword
```

---

## Cómo Ejecutar el Módulo

### Pre-requisitos
- Java JDK 17 o superior
- MySQL o MariaDB en ejecución
- Apache Ant (opcional para el build script)
- Driver JDBC en `lib/` (ya incluido: `mariadb-java-client-3.5.6.jar`)

### Paso 1: Configurar la base de datos
```sql
-- En MySQL/MariaDB:
source sql/01_setup_mock_data.sql
```

### Paso 2: Ajustar credenciales
Edita `config/db.properties` con tus datos reales (ver sección anterior).

### Paso 3: Compilar y ejecutar

**Con Apache Ant (recomendado):**
```bash
ant compile    # Compila y copia db.properties al classpath
ant run        # Ejecuta el módulo
ant jar        # Genera modulo8-ia.jar (incluye db.properties)
ant all        # Limpieza + JAR completo
```

**Con javac manual (Windows):**
```bash
# Compilar
javac -cp lib\mariadb-java-client-3.5.6.jar -d build\classes ^
  src\main\java\com\sistemacontable\modulo8\**\*.java

# Copiar configuración al classpath
xcopy config\*.properties build\classes\ /Y

# Ejecutar
java -cp "build\classes;lib\mariadb-java-client-3.5.6.jar" ^
  com.sistemacontable.modulo8.Main
```

---

## Guía de Integración al Proyecto Unificado

Cuando el equipo unifique el proyecto, solo debes hacer **2 cambios**:

### Cambio 1: SQL en `DatosFinancierosDAO.java`
Reemplaza `SQL_HISTORIAL_MOCK` por la consulta real
(ya incluida como comentario en el archivo). El Motor IA **no se toca**.

### Cambio 2: Punto de entrada en el menú principal
```java
import com.sistemacontable.modulo8.Main;
// En el ActionListener del botón "Análisis de IA":
new JMenuItem("Análisis de IA").addActionListener(e -> Main.lanzar());
```

### Lo que NO hay que cambiar
- `MotorPrediccionIA.java` (la red neuronal es agnóstica a los datos)
- `PreprocesadorDatos.java`
- `ExportadorPDF.java`
- Toda la capa Vista y Controlador

---

## Exportación a PDF

El botón **"📄 Exportar a PDF"** se habilita automáticamente después de ejecutar un análisis.

El PDF generado contiene:
- **Página 1:** Encabezado con degradado, 3 KPIs principales, indicadores de
  tendencia y confianza, tabla completa del historial financiero.
- **Página 2:** Gráfica de barras con gradientes y línea de tendencia,
  análisis de recomendaciones automático según el comportamiento del modelo.

El PDF usa Java Graphics2D puro (sin dependencias externas).
El bug de offsets en la tabla xref ha sido corregido en esta versión.

---

## Cómo Funciona la Red Neuronal

### Arquitectura
```
Entrada (3)  →  Oculta 1 (8, ReLU)  →  Oculta 2 (4, ReLU)  →  Salida (2, Sigmoid)
  compras          8 neuronas              4 neuronas            ventas_pred
  ventas                                                          compras_pred
  índice_temporal
```

### Proceso de entrenamiento
1. **Normalización Min-Max:** Todos los valores se escalan al rango [0, 1].
2. **Preparación de datos:** El historial de N meses genera N-1 pares (entrada, salida),
   donde la salida del mes i es el valor real del mes i+1.
3. **Backpropagation:** 5.000 épocas con tasa de aprendizaje = 0.05.
4. **Log de MSE:** El error cuadrático medio se imprime cada 1.000 épocas para diagnóstico.
5. **Predicción:** Se alimenta el último mes conocido y la red genera la predicción.
6. **Desnormalización:** El resultado se convierte de vuelta a la escala de dólares.

### Nivel de confianza
| Meses de historial | Nivel de confianza |
|---|---|
| ≥ 10 meses | Alto |
| 6 – 9 meses | Medio |
| 3 – 5 meses | Bajo |

---

*Módulo 8 · Sistema Contable Computarizado · Ciclo I-2026*
