package com.sistemacontable.modulo8.servicio;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.util.List;
import java.util.Random;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Servicio: Motor de predicción mediante Red Neuronal Artificial (MLP).
 *
 * Implementación pura en Java (sin dependencia externa de DL4J)
 * para maximizar la portabilidad del módulo.
 *
 * Arquitectura de la red:
 *   - Capa de entrada:  3 neuronas (compras, ventas, índice temporal)
 *   - Capa oculta 1:    8 neuronas, activación ReLU
 *   - Capa oculta 2:    4 neuronas, activación ReLU
 *   - Capa de salida:   2 neuronas, activación Sigmoid (ventas pred., compras pred.)
 *
 * Algoritmo de entrenamiento: Backpropagation con descenso de gradiente estocástico.
 */
public class MotorPrediccionIA {

    // ── Hiperparámetros ───────────────────────────────────────────────────────
    private static final int    INPUT_SIZE        = 3;
    private static final int    HIDDEN1_SIZE      = 8;
    private static final int    HIDDEN2_SIZE      = 4;
    private static final int    OUTPUT_SIZE       = 2;
    private static final int    EPOCAS            = 5000;
    private static final double TASA_APRENDIZAJE  = 0.05;

    /** Intervalo de épocas para imprimir el error (0 = sin log). */
    private static final int    LOG_INTERVALO     = 1000;

    // ── Pesos de la red (inicializados en constructor) ────────────────────────
    private double[][] w1; // INPUT   → HIDDEN1
    private double[]   b1; // bias HIDDEN1
    private double[][] w2; // HIDDEN1 → HIDDEN2
    private double[]   b2; // bias HIDDEN2
    private double[][] w3; // HIDDEN2 → OUTPUT
    private double[]   b3; // bias OUTPUT

    private final PreprocesadorDatos preprocesador;

    // ── Constructor ───────────────────────────────────────────────────────────

    public MotorPrediccionIA() {
        this.preprocesador = new PreprocesadorDatos();
        inicializarPesos();
    }

    // ── Inicialización de pesos ───────────────────────────────────────────────

    /** Inicializa pesos con escala Xavier para convergencia más rápida. */
    private void inicializarPesos() {
        Random rng = new Random(42); // semilla fija para reproducibilidad

        w1 = new double[INPUT_SIZE][HIDDEN1_SIZE];
        b1 = new double[HIDDEN1_SIZE];
        w2 = new double[HIDDEN1_SIZE][HIDDEN2_SIZE];
        b2 = new double[HIDDEN2_SIZE];
        w3 = new double[HIDDEN2_SIZE][OUTPUT_SIZE];
        b3 = new double[OUTPUT_SIZE];

        llenarPesos(rng, w1, INPUT_SIZE);
        llenarPesos(rng, w2, HIDDEN1_SIZE);
        llenarPesos(rng, w3, HIDDEN2_SIZE);
    }

    private void llenarPesos(Random rng, double[][] w, int fanIn) {
        double escala = Math.sqrt(2.0 / fanIn);
        for (double[] fila : w) {
            for (int j = 0; j < fila.length; j++) {
                fila[j] = rng.nextGaussian() * escala;
            }
        }
    }

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Entrena la red neuronal y genera la predicción para el mes siguiente.
     *
     * @param historial     Lista de registros financieros ordenados cronológicamente.
     * @param nombreEmpresa Nombre de la empresa para incluir en el resultado.
     * @param idEmpresa     ID de la empresa (soporte Multi-Empresa).
     * @return {@link ResultadoPrediccion} con ventas y compras proyectadas.
     * @throws IllegalArgumentException si el historial tiene menos de 3 meses.
     */
    public ResultadoPrediccion predecir(List<RegistroFinanciero> historial,
                                        String nombreEmpresa,
                                        int    idEmpresa) {

        if (historial == null || historial.size() < 3) {
            throw new IllegalArgumentException(
                    "Se necesitan al menos 3 meses de historial para entrenar la red neuronal.");
        }

        long inicio = System.currentTimeMillis();

        // 1. Reiniciar pesos para cada empresa (evita contaminación entre análisis)
        inicializarPesos();

        // 2. Preprocesar: normalización Min-Max
        preprocesador.ajustar(historial);
        double[][] X = preprocesador.matrizEntrada(historial);
        double[][] Y = preprocesador.matrizSalida(historial);

        // 3. Entrenamiento con log del error cada LOG_INTERVALO épocas
        for (int epoca = 0; epoca < EPOCAS; epoca++) {
            for (int i = 0; i < X.length; i++) {
                entrenarMuestra(X[i], Y[i]);
            }

            if (LOG_INTERVALO > 0 && (epoca + 1) % LOG_INTERVALO == 0) {
                double mse = calcularMSE(X, Y);
                System.out.printf("[MotorIA] Época %d/%d → MSE: %.6f%n",
                        epoca + 1, EPOCAS, mse);
            }
        }

        // 4. Inferencia: predecir el mes siguiente
        double[] vectorPrediccion = preprocesador.vectorUltimoMes(historial);
        double[] salidaNorm       = forward(vectorPrediccion);

        double ventasPred  = preprocesador.desnormalizarVentas (salidaNorm[0]);
        double comprasPred = preprocesador.desnormalizarCompras(salidaNorm[1]);

        // 5. Calcular tendencia respecto al último mes real
        RegistroFinanciero ultimo = historial.get(historial.size() - 1);
        double tendencia = ((ventasPred - ultimo.getTotalVentas()) / ultimo.getTotalVentas()) * 100.0;

        // 6. Ensamblar y retornar resultado
        ResultadoPrediccion resultado = new ResultadoPrediccion();
        resultado.setNombreEmpresa      (nombreEmpresa);
        resultado.setIdEmpresa          (idEmpresa);
        resultado.setVentasPredichas    (Math.max(0, ventasPred));
        resultado.setComprasPredichas   (Math.max(0, comprasPred));
        resultado.setTendenciaPorcentaje(tendencia);
        resultado.setClasificacionTendencia(clasificarTendencia(tendencia));
        resultado.setNivelConfianza     (calcularConfianza(historial.size()));
        resultado.setHistorial          (historial);
        resultado.setTiempoEntrenamientoMs(System.currentTimeMillis() - inicio);

        long tiempoTotal = System.currentTimeMillis() - inicio;
        System.out.printf("[MotorIA] Predicción completada en %d ms → Ventas: $%.2f | Compras: $%.2f%n",
                tiempoTotal, ventasPred, comprasPred);

        return resultado;
    }

    // ── Forward Pass ──────────────────────────────────────────────────────────

    /** Ejecuta una pasada hacia adelante y devuelve la salida de la red. */
    private double[] forward(double[] entrada) {
        double[] h1  = activarReLU    (multiplicar(entrada, w1, b1));
        double[] h2  = activarReLU    (multiplicar(h1,     w2, b2));
        return         activarSigmoid  (multiplicar(h2,     w3, b3));
    }

    // ── Backpropagation ───────────────────────────────────────────────────────

    private void entrenarMuestra(double[] x, double[] yReal) {
        // — Forward —
        double[] netH1 = multiplicar(x,  w1, b1);
        double[] h1    = activarReLU(netH1);
        double[] netH2 = multiplicar(h1, w2, b2);
        double[] h2    = activarReLU(netH2);
        double[] netOut= multiplicar(h2, w3, b3);
        double[] out   = activarSigmoid(netOut);

        // — Delta capa de salida —
        double[] deltaOut = new double[OUTPUT_SIZE];
        for (int k = 0; k < OUTPUT_SIZE; k++) {
            double error = yReal[k] - out[k];
            deltaOut[k] = error * derivadaSigmoid(out[k]);
        }

        // — Delta capa oculta 2 —
        double[] deltaH2 = new double[HIDDEN2_SIZE];
        for (int j = 0; j < HIDDEN2_SIZE; j++) {
            double suma = 0;
            for (int k = 0; k < OUTPUT_SIZE; k++) suma += deltaOut[k] * w3[j][k];
            deltaH2[j] = suma * derivadaReLU(netH2[j]);
        }

        // — Delta capa oculta 1 —
        double[] deltaH1 = new double[HIDDEN1_SIZE];
        for (int j = 0; j < HIDDEN1_SIZE; j++) {
            double suma = 0;
            for (int k = 0; k < HIDDEN2_SIZE; k++) suma += deltaH2[k] * w2[j][k];
            deltaH1[j] = suma * derivadaReLU(netH1[j]);
        }

        // — Actualizar W3 y b3 —
        for (int j = 0; j < HIDDEN2_SIZE; j++)
            for (int k = 0; k < OUTPUT_SIZE; k++)
                w3[j][k] += TASA_APRENDIZAJE * deltaOut[k] * h2[j];
        for (int k = 0; k < OUTPUT_SIZE; k++)
            b3[k] += TASA_APRENDIZAJE * deltaOut[k];

        // — Actualizar W2 y b2 —
        for (int j = 0; j < HIDDEN1_SIZE; j++)
            for (int k = 0; k < HIDDEN2_SIZE; k++)
                w2[j][k] += TASA_APRENDIZAJE * deltaH2[k] * h1[j];
        for (int k = 0; k < HIDDEN2_SIZE; k++)
            b2[k] += TASA_APRENDIZAJE * deltaH2[k];

        // — Actualizar W1 y b1 —
        for (int i = 0; i < INPUT_SIZE; i++)
            for (int k = 0; k < HIDDEN1_SIZE; k++)
                w1[i][k] += TASA_APRENDIZAJE * deltaH1[k] * x[i];
        for (int k = 0; k < HIDDEN1_SIZE; k++)
            b1[k] += TASA_APRENDIZAJE * deltaH1[k];
    }

    // ── Cálculo de error MSE (diagnóstico) ───────────────────────────────────

    private double calcularMSE(double[][] X, double[][] Y) {
        double errorTotal = 0;
        for (int i = 0; i < X.length; i++) {
            double[] pred = forward(X[i]);
            for (int k = 0; k < OUTPUT_SIZE; k++) {
                double diff = Y[i][k] - pred[k];
                errorTotal += diff * diff;
            }
        }
        return errorTotal / (X.length * OUTPUT_SIZE);
    }

    // ── Funciones matemáticas ─────────────────────────────────────────────────

    private double[] multiplicar(double[] entrada, double[][] pesos, double[] bias) {
        int salida = pesos[0].length;
        double[] resultado = new double[salida];
        for (int j = 0; j < salida; j++) {
            resultado[j] = bias[j];
            for (int i = 0; i < entrada.length; i++) {
                resultado[j] += entrada[i] * pesos[i][j];
            }
        }
        return resultado;
    }

    private double[] activarReLU(double[] z) {
        double[] a = new double[z.length];
        for (int i = 0; i < z.length; i++) a[i] = Math.max(0, z[i]);
        return a;
    }

    private double[] activarSigmoid(double[] z) {
        double[] a = new double[z.length];
        for (int i = 0; i < z.length; i++) a[i] = 1.0 / (1.0 + Math.exp(-z[i]));
        return a;
    }

    private double derivadaReLU   (double z) { return z > 0 ? 1.0 : 0.0; }
    private double derivadaSigmoid(double a) { return a * (1.0 - a); }

    // ── Clasificadores ────────────────────────────────────────────────────────

    private String clasificarTendencia(double porcentaje) {
        if (porcentaje >  5.0) return "CRECIMIENTO";
        if (porcentaje < -5.0) return "DECRECIMIENTO";
        return "ESTABLE";
    }

    private String calcularConfianza(int nMeses) {
        if (nMeses >= 10) return "Alto";
        if (nMeses >=  6) return "Medio";
        return "Bajo";
    }
}
