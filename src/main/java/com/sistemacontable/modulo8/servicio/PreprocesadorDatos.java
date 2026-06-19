package com.sistemacontable.modulo8.servicio;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Servicio: Preprocesamiento y normalización de datos para la red neuronal.
 *
 * La red neuronal trabaja mejor con valores en el rango [0, 1].
 * Esta clase escala los datos usando Min-Max normalization y los
 * organiza en matrices de entrada (features) y salida (labels).
 *
 * FEATURES de entrada por mes:
 *   [0] total_compras normalizado
 *   [1] total_ventas  normalizado
 *   [2] índice de mes normalizado (posición temporal)
 *
 * LABEL de salida por mes:
 *   [0] total_ventas del mes siguiente (normalizado)
 *   [1] total_compras del mes siguiente (normalizado)
 */
public class PreprocesadorDatos {

    // ── Estado interno ────────────────────────────────────────────────────────
    private double minVentas, maxVentas;
    private double minCompras, maxCompras;
    private boolean estadoCalculado = false;

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Calcula min/max del historial y normaliza todos los valores.
     * Debe llamarse antes de usar matrizEntrada() o matrizSalida().
     *
     * @param historial Lista de registros ordenados cronológicamente.
     */
    public void ajustar(List<RegistroFinanciero> historial) {
        if (historial == null || historial.isEmpty()) {
            throw new IllegalArgumentException("El historial no puede estar vacío.");
        }

        minVentas  = Double.MAX_VALUE;
        maxVentas  = -Double.MAX_VALUE;
        minCompras = Double.MAX_VALUE;
        maxCompras = -Double.MAX_VALUE;

        for (RegistroFinanciero r : historial) {
            if (r.getTotalVentas()  < minVentas)  minVentas  = r.getTotalVentas();
            if (r.getTotalVentas()  > maxVentas)  maxVentas  = r.getTotalVentas();
            if (r.getTotalCompras() < minCompras) minCompras = r.getTotalCompras();
            if (r.getTotalCompras() > maxCompras) maxCompras = r.getTotalCompras();
        }

        // Evitar división por cero si todos los valores son iguales
        if (maxVentas  == minVentas)  maxVentas  = minVentas  + 1;
        if (maxCompras == minCompras) maxCompras = minCompras + 1;

        estadoCalculado = true;
    }

    /**
     * Construye la matriz de entrada para la red neuronal.
     * Cada fila = un mes de entrenamiento (todos excepto el último).
     * Cada columna = una feature normalizada.
     *
     * @param historial Lista de registros.
     * @return double[n-1][3]
     */
    public double[][] matrizEntrada(List<RegistroFinanciero> historial) {
        verificarEstado();
        int n = historial.size();
        double[][] matriz = new double[n - 1][3];
        int total = historial.size();

        for (int i = 0; i < n - 1; i++) {
            RegistroFinanciero r = historial.get(i);
            matriz[i][0] = normalizar(r.getTotalCompras(), minCompras, maxCompras);
            matriz[i][1] = normalizar(r.getTotalVentas(),  minVentas,  maxVentas);
            matriz[i][2] = (double) i / (total - 1);  // posición temporal
        }
        return matriz;
    }

    /**
     * Construye la matriz de salida (labels) para la red neuronal.
     * La salida del mes i es el valor real del mes i+1.
     *
     * @param historial Lista de registros.
     * @return double[n-1][2]
     */
    public double[][] matrizSalida(List<RegistroFinanciero> historial) {
        verificarEstado();
        int n = historial.size();
        double[][] matriz = new double[n - 1][2];

        for (int i = 0; i < n - 1; i++) {
            RegistroFinanciero siguiente = historial.get(i + 1);
            matriz[i][0] = normalizar(siguiente.getTotalVentas(),  minVentas,  maxVentas);
            matriz[i][1] = normalizar(siguiente.getTotalCompras(), minCompras, maxCompras);
        }
        return matriz;
    }

    /**
     * Construye el vector de entrada del último mes (para predecir el siguiente).
     *
     * @param historial Lista de registros.
     * @return double[3]
     */
    public double[] vectorUltimoMes(List<RegistroFinanciero> historial) {
        verificarEstado();
        RegistroFinanciero ultimo = historial.get(historial.size() - 1);
        return new double[]{
                normalizar(ultimo.getTotalCompras(), minCompras, maxCompras),
                normalizar(ultimo.getTotalVentas(),  minVentas,  maxVentas),
                1.0  // posición temporal = 1.0 (próximo mes)
        };
    }

    /**
     * Desnormaliza un valor de ventas (vuelve a la escala original en $).
     * @param valorNorm
     */
    public double desnormalizarVentas(double valorNorm) {
        return valorNorm * (maxVentas - minVentas) + minVentas;
    }

    /**
     * Desnormaliza un valor de compras.
     * @return 
     */
    public double desnormalizarCompras(double valorNorm) {
        return valorNorm * (maxCompras - minCompras) + minCompras;
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    private double normalizar(double valor, double min, double max) {
        return (valor - min) / (max - min);
    }

    private void verificarEstado() {
        if (!estadoCalculado) {
            throw new IllegalStateException("Debe llamar a ajustar() antes de transformar datos.");
        }
    }

    // ── Getters para debug / reporte ──────────────────────────────────────────
    public double getMinVentas()  { return minVentas; }
    public double getMaxVentas()  { return maxVentas; }
    public double getMinCompras() { return minCompras; }
    public double getMaxCompras() { return maxCompras; }
}
