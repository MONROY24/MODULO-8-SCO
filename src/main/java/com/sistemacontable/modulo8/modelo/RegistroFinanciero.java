package com.sistemacontable.modulo8.modelo;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Modelo: Representa una fila del historial financiero mensual.
 */
public class RegistroFinanciero {

    private int    idEmpresa;
    private int    mesNumero;
    private int    anio;
    private double totalCompras;
    private double totalVentas;

    // ── Constructores ─────────────────────────────────────────────────────────

    public RegistroFinanciero() {}

    public RegistroFinanciero(int idEmpresa, int mesNumero, int anio,
                               double totalCompras, double totalVentas) {
        this.idEmpresa    = idEmpresa;
        this.mesNumero    = mesNumero;
        this.anio         = anio;
        this.totalCompras = totalCompras;
        this.totalVentas  = totalVentas;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int    getIdEmpresa()    { return idEmpresa; }
    public void   setIdEmpresa(int idEmpresa)       { this.idEmpresa = idEmpresa; }

    public int    getMesNumero()    { return mesNumero; }
    public void   setMesNumero(int mesNumero)       { this.mesNumero = mesNumero; }

    public int    getAnio()         { return anio; }
    public void   setAnio(int anio) { this.anio = anio; }

    public double getTotalCompras() { return totalCompras; }
    public void   setTotalCompras(double totalCompras) { this.totalCompras = totalCompras; }

    public double getTotalVentas()  { return totalVentas; }
    public void   setTotalVentas(double totalVentas)   { this.totalVentas  = totalVentas; }

    /** Nombre corto del mes para mostrar en gráficas. */
    public String getNombreMes() {
        String[] meses = {"", "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                              "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
        return (mesNumero >= 1 && mesNumero <= 12) ? meses[mesNumero] : "?";
    }

    @Override
    public String toString() {
        return String.format("Mes %02d/%d – Ventas: %.2f | Compras: %.2f",
                mesNumero, anio, totalVentas, totalCompras);
    }
}
