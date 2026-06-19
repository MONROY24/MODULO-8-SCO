package com.sistemacontable.modulo8.modelo;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * POJO para representar de manera segura una empresa en los selectores.
 */
public class EmpresaItem {
    private final int id;
    private final String nombre;

    public EmpresaItem(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre; // Para que JComboBox lo muestre correctamente si se usara directamente
    }
}
