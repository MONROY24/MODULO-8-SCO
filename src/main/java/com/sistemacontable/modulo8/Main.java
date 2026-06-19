package com.sistemacontable.modulo8;

import com.sistemacontable.modulo8.controlador.AnalisisControlador;
import com.sistemacontable.modulo8.vista.VentanaAnalisis;
import javax.swing.*;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Punto de entrada principal.
 *
 * Para integrar este módulo al proyecto unificado,
 * simplemente llama a Main.lanzar() desde el menú principal.
 */
public class Main {

    public static void main(String[] args) {
        // Ejecutar en el Event Dispatch Thread de Swing
        SwingUtilities.invokeLater(Main::lanzar);
    }

    /**
     * Instancia y muestra la ventana del módulo.
     * Llama a este método desde el menú principal del proyecto unificado.
     */
    public static void lanzar() {
        // 1. Crear Vista
        VentanaAnalisis vista = new VentanaAnalisis();

        // 2. Crear Controlador y conectarlo a la Vista
        AnalisisControlador controlador = new AnalisisControlador(vista);
        vista.setControlador(controlador);

        // 3. Cargar datos iniciales (empresas)
        controlador.cargarEmpresas();

        // 4. Mostrar ventana
        vista.setVisible(true);
    }
}
