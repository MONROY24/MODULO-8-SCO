package com.sistemacontable.modulo8.controlador;

import com.sistemacontable.modulo8.dao.DatosFinancierosDAO;
import com.sistemacontable.modulo8.modelo.EmpresaItem;
import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;
import com.sistemacontable.modulo8.servicio.MotorPrediccionIA;
import com.sistemacontable.modulo8.util.ExportadorPDF;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AnalisisRestController {

    private static final Logger log = LoggerFactory.getLogger(AnalisisRestController.class);

    private final DatosFinancierosDAO dao;
    private final MotorPrediccionIA motorIA;
    private final ExportadorPDF exportadorPDF;

    public AnalisisRestController() {
        this.dao = new DatosFinancierosDAO();
        this.motorIA = new MotorPrediccionIA();
        this.exportadorPDF = new ExportadorPDF();
    }

    public void getEmpresas(Context ctx) throws SQLException {
        log.info("Obteniendo lista de empresas...");
        List<EmpresaItem> empresas = dao.obtenerEmpresas();
        ctx.json(empresas);
    }

    public void getAnalisis(Context ctx) throws SQLException {
        int idEmpresa = Integer.parseInt(ctx.pathParam("id"));
        log.info("Ejecutando análisis para la empresa ID: {}", idEmpresa);

        List<RegistroFinanciero> historial = dao.obtenerHistorial(idEmpresa);

        if (historial.isEmpty()) {
            throw new IllegalStateException("No se encontró historial financiero para la empresa seleccionada.");
        }

        String nombre = dao.obtenerNombreEmpresa(idEmpresa);
        ResultadoPrediccion resultado = motorIA.predecir(historial, nombre, idEmpresa);

        ctx.json(resultado);
    }

    public void exportarPdf(Context ctx) throws SQLException, IOException {
        int idEmpresa = Integer.parseInt(ctx.pathParam("id"));
        log.info("Exportando PDF para la empresa ID: {}", idEmpresa);

        List<RegistroFinanciero> historial = dao.obtenerHistorial(idEmpresa);

        if (historial.isEmpty()) {
            throw new IllegalStateException("No se encontró historial financiero para la empresa seleccionada.");
        }

        String nombre = dao.obtenerNombreEmpresa(idEmpresa);
        ResultadoPrediccion resultado = motorIA.predecir(historial, nombre, idEmpresa);

        byte[] pdfBytes = exportadorPDF.generarPDF(resultado);

        String filename = "Analisis_" + nombre.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

        ctx.contentType("application/pdf");
        ctx.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        ctx.result(pdfBytes);
    }
}
