package com.sistemacontable.modulo8.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Modelo: Encapsula el resultado completo del análisis predictivo.
 * Este objeto se pasa entre el Motor IA, el Controlador y el generador de PDF.
 */
public class ResultadoPrediccion {

    private String              nombreEmpresa;
    private int                 idEmpresa;
    private double              ventasPredichas;
    private double              comprasPredichas;
    private double              tendenciaPorcentaje;   // % de cambio respecto al último mes real
    private String              clasificacionTendencia; // "CRECIMIENTO", "ESTABLE", "DECRECIMIENTO"
    private String              nivelConfianza;         // "Alto", "Medio", "Bajo"
    private List<RegistroFinanciero> historial;
    private LocalDateTime       fechaAnalisis;
    private long                tiempoEntrenamientoMs;

    // ── Constructor ───────────────────────────────────────────────────────────

    public ResultadoPrediccion() {
        this.fechaAnalisis = LocalDateTime.now();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public String  getNombreEmpresa()           { return nombreEmpresa; }
    public void    setNombreEmpresa(String s)   { this.nombreEmpresa = s; }

    public int     getIdEmpresa()               { return idEmpresa; }
    public void    setIdEmpresa(int id)         { this.idEmpresa = id; }

    public double  getVentasPredichas()         { return ventasPredichas; }
    public void    setVentasPredichas(double v) { this.ventasPredichas = v; }

    public double  getComprasPredichas()        { return comprasPredichas; }
    public void    setComprasPredichas(double c){ this.comprasPredichas = c; }

    public double  getTendenciaPorcentaje()              { return tendenciaPorcentaje; }
    public void    setTendenciaPorcentaje(double t)      { this.tendenciaPorcentaje = t; }

    public String  getClasificacionTendencia()           { return clasificacionTendencia; }
    public void    setClasificacionTendencia(String c)   { this.clasificacionTendencia = c; }

    public String  getNivelConfianza()                   { return nivelConfianza; }
    public void    setNivelConfianza(String n)           { this.nivelConfianza = n; }

    public List<RegistroFinanciero> getHistorial()       { return historial; }
    public void    setHistorial(List<RegistroFinanciero> h){ this.historial = h; }

    public LocalDateTime getFechaAnalisis()              { return fechaAnalisis; }
    public void    setFechaAnalisis(LocalDateTime f)     { this.fechaAnalisis = f; }

    public long    getTiempoEntrenamientoMs()            { return tiempoEntrenamientoMs; }
    public void    setTiempoEntrenamientoMs(long t)      { this.tiempoEntrenamientoMs = t; }

    // ── Utilidades ────────────────────────────────────────────────────────────

    /** Calcula la utilidad proyectada (ventas – compras). */
    public double getUtilidadProyectada() {
        return ventasPredichas - comprasPredichas;
    }

    /** Devuelve el último registro real del historial (mes más reciente). */
    public RegistroFinanciero getUltimoRegistro() {
        if (historial == null || historial.isEmpty()) return null;
        return historial.get(historial.size() - 1);
    }
}
