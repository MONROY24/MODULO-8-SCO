package com.sistemacontable.modulo8.controlador;

import com.sistemacontable.modulo8.dao.DatosFinancierosDAO;
import com.sistemacontable.modulo8.modelo.EmpresaItem;
import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;
import com.sistemacontable.modulo8.servicio.MotorPrediccionIA;
import com.sistemacontable.modulo8.util.ExportadorPDF;
import com.sistemacontable.modulo8.vista.VentanaAnalisis;
import java.awt.Desktop;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Controlador (MVC): Orquesta la interacción entre la Vista, el DAO y los Servicios.
 *
 * Responsabilidades:
 *   1. Escuchar el clic del botón "Ejecutar Análisis" en la Vista.
 *   2. Pedir el historial al DAO (con el id_empresa seleccionado).
 *   3. Invocar el Motor IA para obtener la predicción.
 *   4. Enviar el resultado de vuelta a la Vista.
 *   5. Coordinar la exportación a PDF cuando el usuario lo solicita.
 */
public class AnalisisControlador {

    // ── Dependencias ──────────────────────────────────────────────────────────
    private final VentanaAnalisis    vista;
    private final DatosFinancierosDAO dao;
    private final MotorPrediccionIA  motorIA;
    private final ExportadorPDF      exportadorPDF;

    // Estado del último análisis (para exportar sin recalcular)
    private ResultadoPrediccion ultimoResultado;
    
    // Referencia al worker activo para poder cancelarlo
    private SwingWorker<ResultadoPrediccion, Void> workerAnalisis;

    // ── Constructor ───────────────────────────────────────────────────────────

    public AnalisisControlador(VentanaAnalisis vista) {
        this.vista         = vista;
        this.dao           = new DatosFinancierosDAO();
        this.motorIA       = new MotorPrediccionIA();
        this.exportadorPDF = new ExportadorPDF();
    }

    // ── Acciones públicas (llamadas desde la Vista) ───────────────────────────

    /**
     * Carga la lista de empresas disponibles y la envía a la Vista.
     * Se llama al inicializar la ventana.
     */
    public void cargarEmpresas() {
        try {
            List<EmpresaItem> empresas = dao.obtenerEmpresas();
            vista.poblarSelectorEmpresas(empresas);
        } catch (SQLException e) {
            mostrarError("No se pudo conectar a la base de datos.\n" + e.getMessage());
        }
    }

    /**
     * Ejecuta el análisis predictivo para la empresa seleccionada.
     * Se ejecuta en un hilo de fondo (SwingWorker) para no bloquear la UI.
     *
     * @param idEmpresa ID de la empresa seleccionada en la Vista.
     */
    public void ejecutarAnalisis(int idEmpresa) {
        // Cancelar el worker anterior si sigue corriendo
        if (workerAnalisis != null && !workerAnalisis.isDone()) {
            workerAnalisis.cancel(true);
        }

        vista.setEstadoCargando(true);
        vista.limpiarResultados();

        workerAnalisis = new SwingWorker<>() {

            @Override
            protected ResultadoPrediccion doInBackground() throws Exception {
                // 1. Obtener historial desde la BD
                List<RegistroFinanciero> historial = dao.obtenerHistorial(idEmpresa);

                if (historial.isEmpty()) {
                    throw new IllegalStateException(
                            "No se encontró historial financiero para la empresa seleccionada.");
                }

                // 2. Obtener nombre de la empresa
                String nombre = dao.obtenerNombreEmpresa(idEmpresa);

                // 3. Ejecutar la red neuronal
                return motorIA.predecir(historial, nombre, idEmpresa);
            }

            @Override
            protected void done() {
                vista.setEstadoCargando(false);
                try {
                    ultimoResultado = get();
                    vista.mostrarResultados(ultimoResultado);
                } catch (java.util.concurrent.ExecutionException ee) {
                    Throwable causa = ee.getCause();
                    if (causa instanceof IllegalArgumentException ||
                        causa instanceof IllegalStateException) {
                        mostrarError(causa.getMessage());
                    } else {
                        mostrarError("Error durante el análisis: " + causa.getMessage());
                    }
                } catch (Exception ex) {
                    mostrarError("Error inesperado: " + ex.getMessage());
                }
            }
        };

        workerAnalisis.execute();
    }

    /**
     * Exporta el último análisis a PDF.
     * Muestra un diálogo para que el usuario elija la ruta de guardado.
     */
    public void exportarPDF() {
        if (ultimoResultado == null) {
            mostrarError("Primero ejecute un análisis antes de exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar Reporte PDF");
        chooser.setSelectedFile(new File("Analisis_" +
                ultimoResultado.getNombreEmpresa().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF (*.pdf)", "pdf"));

        int opcion = chooser.showSaveDialog(null);
        if (opcion != JFileChooser.APPROVE_OPTION) return;

        String ruta = chooser.getSelectedFile().getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".pdf")) ruta += ".pdf";

        final String rutaFinal = ruta;
        vista.setEstadoCargando(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                exportadorPDF.generarPDF(ultimoResultado, rutaFinal);
                return null;
            }

            @Override
            protected void done() {
                vista.setEstadoCargando(false);
                try {
                    get();
                    int resp = JOptionPane.showConfirmDialog(null,
                            "PDF generado exitosamente.\n¿Desea abrirlo ahora?",
                            "Exportación completada",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE);
                    if (resp == JOptionPane.YES_OPTION) {
                        try {
                            Desktop.getDesktop().open(new File(rutaFinal));
                        } catch (Exception ex) {
                            mostrarError("No se pudo abrir el archivo: " + ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    mostrarError("Error al generar el PDF: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    // ── Internos ──────────────────────────────────────────────────────────────

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, "Error – Módulo 8 IA",
                JOptionPane.ERROR_MESSAGE);
    }
}
