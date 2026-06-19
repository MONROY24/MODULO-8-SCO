# Arquitectura Técnica – Módulo 8 IA

## Diagrama de capas

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE PRESENTACIÓN                              │
│                                                                              │
│  VentanaAnalisis.java          PanelGrafica.java                             │
│  ├── Header con degradado      ├── Barras con GradientPaint                  │
│  ├── 5 KPIs con gradient       ├── Línea de tendencia curvada (QuadTo)       │
│  ├── Panel control lateral     ├── Leyenda con gradientes                    │
│  ├── Tabla con renderer        └── Estado vacío decorativo                   │
│  └── Botón PDF                                                               │
└──────────────────────────┬───────────────────────────────────────────────────┘
                           │ implementa MVC (eventos + callbacks)
┌──────────────────────────▼───────────────────────────────────────────────────┐
│                            CAPA DE CONTROL                                   │
│                                                                              │
│  AnalisisControlador.java                                                    │
│  ├── cargarEmpresas()  → llama DAO + actualiza Vista                         │
│  ├── ejecutarAnalisis() → SwingWorker (hilo bg) → DAO → MotorIA → Vista      │
│  └── exportarPDF()    → SwingWorker → ExportadorPDF → Desktop.open()         │
└────────┬──────────────────────────────────────────────┬──────────────────────┘
         │                                              │
┌────────▼────────────────────┐          ┌─────────────▼───────────────────────┐
│     CAPA DE SERVICIO / IA   │          │     CAPA DE ACCESO A DATOS (DAO)    │
│                             │          │                                     │
│  MotorPrediccionIA          │          │  AppConfig                          │
│  ├── inicializarPesos()     │          │  └── Lee config/db.properties       │
│  ├── predecir()             │          │       o variables de entorno        │
│  │   ├── preprocesar        │          │                                     │
│  │   ├── entrenar 5000 époc │          │  ConexionDB (Singleton thread-safe) │
│  │   ├── forward + MSE log  │          │  └── double-checked locking         │
│  │   └── desnormalizar      │          │                                     │
│  └── Backpropagation        │          │  DatosFinancierosDAO                │
│                             │          │  ├── obtenerHistorial(idEmpresa)     │
│  PreprocesadorDatos         │          │  ├── obtenerEmpresas()              │
│  ├── ajustar() Min-Max      │          │  └── obtenerNombreEmpresa()         │
│  ├── matrizEntrada()        │          └─────────────────────────────────────┘
│  ├── matrizSalida()         │
│  └── vectorUltimoMes()      │
└─────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE UTILIDADES                                │
│                                                                              │
│  ExportadorPDF                                                               │
│  ├── generarPDF(resultado, ruta)                                             │
│  ├── renderizarPagina1() → Encabezado + KPIs + Tabla                        │
│  ├── renderizarPagina2() → Gráfica con degradados + Recomendaciones          │
│  └── escribirPDF()       → PDF manual con offsets xref corregidos            │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│                            CAPA DE MODELO                                    │
│                                                                              │
│  RegistroFinanciero                    ResultadoPrediccion                   │
│  ├── idEmpresa                         ├── nombreEmpresa                     │
│  ├── mesNumero                         ├── ventasPredichas                   │
│  ├── anio                              ├── comprasPredichas                  │
│  ├── totalCompras                      ├── tendenciaPorcentaje               │
│  ├── totalVentas                       ├── clasificacionTendencia            │
│  └── getNombreMes()                    ├── nivelConfianza                    │
│                                        ├── historial                         │
│                                        ├── fechaAnalisis                     │
│                                        └── getUtilidadProyectada()           │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Flujo de datos

```
Usuario clic "Ejecutar"
  └→ AnalisisControlador.ejecutarAnalisis(idEmpresa)
       └→ [SwingWorker - hilo bg]
            ├→ DatosFinancierosDAO.obtenerHistorial(idEmpresa)
            │    └→ ConexionDB.getConexion() [lee AppConfig → db.properties]
            │         └→ SQL → List<RegistroFinanciero>
            ├→ MotorPrediccionIA.predecir(historial, nombre, id)
            │    ├→ PreprocesadorDatos.ajustar() [Min-Max]
            │    ├→ preprocesador.matrizEntrada() + matrizSalida()
            │    ├→ Backpropagation × 5000 épocas
            │    │    └→ log MSE cada 1000 épocas
            │    ├→ forward(vectorUltimoMes)
            │    └→ desnormalizar → ResultadoPrediccion
            └→ [EDT - hilo UI]
                 └→ VentanaAnalisis.mostrarResultados(resultado)
                      ├→ Actualizar 5 KPIs
                      ├→ Actualizar tabla historial
                      └→ PanelGrafica.actualizar(resultado) → repaint()
```

## Decisiones de diseño

| Decisión | Justificación |
|---|---|
| MLP en Java puro | Máxima portabilidad, sin dependencias externas |
| Singleton ConexionDB | Una sola conexión JDBC por ejecución |
| Double-checked locking | Thread-safety sin overhead en cada llamada |
| `db.properties` externalizado | Credenciales fuera del código fuente |
| SwingWorker para análisis | Evita bloquear el Event Dispatch Thread |
| GradientPaint en vista | Aspecto premium sin dependencias adicionales |
| PDF manual con JPEG | Sin dependencias de iText/PDFBox |
