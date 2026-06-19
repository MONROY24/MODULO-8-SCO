# 🧠 Módulo 8: Análisis Predictivo con Inteligencia Artificial
### Sistema Contable Computarizado (SCO) · Ciclo I-2026

![Java Version](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![MLP](https://img.shields.io/badge/AI-Multilayer_Perceptron-000000?style=for-the-badge)

Este repositorio contiene el **Módulo 8** autónomo del Sistema Contable Computarizado, dedicado exclusivamente a procesar el historial financiero de compras y ventas de las empresas registradas para inferir y predecir el comportamiento del mercado del próximo mes.

El cerebro matemático de la herramienta es una **Red Neuronal Artificial (MLP)** codificada en Java nativo, que escala la data mediante `Min-Max`, entrena con `Backpropagation` a 5000 épocas (utilizando `Learning Rate Decay`), y devuelve la predicción monetaria y el nivel de confianza de su inferencia.

---

## 📑 Índice
1. [Características Principales](#-características-principales)
2. [Arquitectura del Sistema](#-arquitectura-del-sistema)
3. [Guía de Instalación Rápida](#-guía-de-instalación-rápida)
4. [Integración al SCO Base](#-integración-al-sco-base)
5. [Documentación Técnica Oficial](#-documentación-técnica-oficial)

---

## 🚀 Características Principales
- **Inteligencia Artificial Pura en Java**: Ningún Framework (ni TensorFlow ni PyTorch). Red Neuronal alimentada por matemática lineal construida desde cero.
- **Renderizado Dinámico de Gráficas Vectoriales**: Las proyecciones se dibujan matemáticamente con `Graphics2D`, incluyendo barras multi-anuales (`Ene '24`, `Feb '25`).
- **Exportación Nivel Empresa**: Genera reportes estáticos en `PDF-1.4` compilando los bits y bytes sin uso de iText o librerías de 3eros que sumen peso.
- **Protección Antiretraso**: El motor corre de forma aislada en un `SwingWorker`, evitando que la UI se congele mientras la IA converge.

---

## 📐 Arquitectura del Sistema
El sistema se construyó bajo una base de **MVC Puro**.

- **Capa DAO (`DatosFinancierosDAO.java`)**: Sustituye antiguos arreglos débiles por Listas de Objetos (POJOs). Está blindado contra SQL Injection gracias a `PreparedStatement`.
- **Capa Inteligencia (`MotorPrediccionIA.java`)**: Topología de red de `3-8-4-2` neuronas. Incorpora evaluación dual de nivel de confianza basado en la extensión del historial y el Error Cuadrático Medio (MSE).
- **Capa Persistencia (`HikariCP`)**: Las conexiones se reciclan con pools de alto desempeño, previniendo cuellos de botella al leer cientos de facturas.
- **Capa Vista (`VentanaAnalisis.java`)**: Estructurada con `BorderLayout`, evita subpestañas complejas para presentar métricas ejecutivas directamente al gerente con indicadores condicionales de alerta (Verde/Rojo).

---

## 💻 Guía de Instalación Rápida

### Pre-requisitos
- **Java JDK 11** (Recomendado JDK 17 o 21).
- **MariaDB 10.5+** o **MySQL 8.0+**.
- **Apache Ant** (Opcional, para scripts de compilación).

### Pasos de Despliegue
1. **Configura tu Entorno**:
   Renombra el archivo base `config/db.properties.example` a `config/db.properties`.
   ```properties
   db.driver=com.mysql.cj.jdbc.Driver
   db.url=jdbc:mysql://localhost:3306/sistema_contable?useSSL=false
   db.user=root
   db.password=tu_clave
   ```
2. **Puebla la Data Mock**:
   Inyecta en tu gestor SQL el script base:
   `source sql/01_setup_mock_data.sql`
3. **Compila y Ejecuta**:
   Con Ant, simplemente escribe:
   ```bash
   ant compile
   ant run
   ```

---

## 🔗 Integración al SCO Base
Este módulo está pensado para fusionarse a futuro con el **Sistema Contable Unificado**.

Para invocar la interfaz desde el **Menú Principal del ERP**, solo debes instanciar la llamada a la clase lanzadora en tu `ActionListener`:
```java
// Desde el Action Listener del Menú en tu SCO:
com.sistemacontable.modulo8.Main.lanzar();
```

*Importante:* Se deberá ajustar la consulta que actualmente utiliza las tablas mock (`empresa_mock` y `historial_financiero_mock`) para leer los datos consolidados directos de `factura_compra_cabecera` en el `DatosFinancierosDAO.java`. El `Anexo A` de la documentación técnica incluye el Query necesario para lograrlo sin generar fallas de producto cartesiano.

---

## 📘 Documentación Técnica Oficial
Se ha liberado la Documentación ISO Arquitectónica del módulo, que incluye:
- Matemáticas de la normalización paso a paso (Vmin, Vmax).
- Explicaciones de Forward y Backward Pass.
- Contratos de código fuente de cada componente.
- Manejo de Pruebas de Software fijando el Seed=42 y documentando el MSE obtenido en cada escenario.

**[Descarga / Abre el PDF del Manual Técnico Completo Aquí (40+ Páginas)](./documentacion_tecnica_sco.pdf)**

---
*Fin del README*
