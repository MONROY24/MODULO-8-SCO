package com.sistemacontable.modulo8.util;

import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sistemacontable.modulo8.util.PlantillaPDF.*;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Utilidad: Genera un reporte PDF del análisis predictivo.
 *
 * Actúa como coordinador delegando:
 * - Dibujo de gráficas a GeneradorGraficaPDF
 * - Dibujo de tablas a GeneradorTablasPDF
 * - Configuración, paleta y empaquetado PDF a PlantillaPDF
 */
public class ExportadorPDF {

    /**
     * Genera el PDF del análisis y lo retorna como arreglo de bytes.
     *
     * @param resultado Resultado del análisis predictivo.
     * @return Arreglo de bytes del PDF.
     * @throws IOException si hay un error al generar.
     */
    public byte[] generarPDF(ResultadoPrediccion resultado) throws IOException {
        List<BufferedImage> paginas = renderizarPaginas(resultado);
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            PlantillaPDF.escribirPDF(baos, paginas);
            return baos.toByteArray();
        }
    }

    private List<BufferedImage> renderizarPaginas(ResultadoPrediccion res) {
        List<BufferedImage> lista = new ArrayList<>();
        lista.add(renderizarPagina1(res));
        lista.add(renderizarPagina2(res));
        return lista;
    }

    private BufferedImage renderizarPagina1(ResultadoPrediccion res) {
        BufferedImage img = PlantillaPDF.nuevaPagina();
        Graphics2D    g   = PlantillaPDF.crearGraphics(img);

        int y = dibujarEncabezado(g, res);
        y += 22;
        y  = dibujarResumenEjecutivo(g, res, y);
        y += 22;
             GeneradorTablasPDF.dibujarTablaHistorial(g, res, y);
             PlantillaPDF.dibujarPieDePagina(g, 1, 2, res);

        g.dispose();
        return img;
    }

    private BufferedImage renderizarPagina2(ResultadoPrediccion res) {
        BufferedImage img = PlantillaPDF.nuevaPagina();
        Graphics2D    g   = PlantillaPDF.crearGraphics(img);

        int y = MARGIN + 10;
        PlantillaPDF.dibujarTituloSeccion(g, "Evolución Histórica y Predicción (Compras vs Ventas)", y);
        y += 30;
        y  = GeneradorGraficaPDF.dibujarGrafica(g, res, y);
        
        y += 15;
        PlantillaPDF.dibujarTituloSeccion(g, "Evolución de Utilidad Neta (Márgenes)", y);
        y += 30;
        y = GeneradorGraficaPDF.dibujarGraficaUtilidad(g, res, y);
        
        y += 22;
             dibujarRecomendaciones(g, res, y);
             PlantillaPDF.dibujarPieDePagina(g, 2, 2, res);

        g.dispose();
        return img;
    }

    private int dibujarEncabezado(Graphics2D g, ResultadoPrediccion res) {
        GradientPaint gp = new GradientPaint(0, 0, C_PRIMARIO, PAGE_W, 80, new Color(49, 112, 204));
        g.setPaint(gp);
        g.fillRect(0, 0, PAGE_W, 80);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 17));
        g.drawString("SISTEMA CONTABLE COMPUTARIZADO", MARGIN, 28);

        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Módulo 8 – Análisis Predictivo con Inteligencia Artificial", MARGIN, 44);

        String fecha = "Emitido: " + res.getFechaAnalisis().format(FMT_FECHA);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(fecha, PAGE_W - MARGIN - fm.stringWidth(fecha), 44);

        g.setColor(C_AMARILLO);
        g.fillRect(0, 76, PAGE_W, 4);

        int y = 100;
        g.setColor(C_PRIMARIO);
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        g.drawString("Empresa: " + res.getNombreEmpresa(), MARGIN, y);

        y += 17;
        g.setColor(C_GRIS);
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("ID: " + res.getIdEmpresa() +
                "   |   Meses analizados: " + res.getHistorial().size() +
                "   |   Tiempo de entrenamiento: " + res.getTiempoEntrenamientoMs() + " ms",
                MARGIN, y);

        return y + 10;
    }

    private int dibujarResumenEjecutivo(Graphics2D g, ResultadoPrediccion res, int y) {
        PlantillaPDF.dibujarTituloSeccion(g, "Resumen Ejecutivo de Predicción", y);
        y += 28;

        int cardW = (CONTENT_W - 20) / 3;
        int cardH = 68;

        dibujarKPI(g, MARGIN,                 y, cardW, cardH,
                "Ventas Proyectadas (Próx. Mes)",
                String.format("$%,.2f", res.getVentasPredichas()), C_VERDE);

        dibujarKPI(g, MARGIN + cardW + 10,    y, cardW, cardH,
                "Compras Proyectadas (Próx. Mes)",
                String.format("$%,.2f", res.getComprasPredichas()), C_SECUNDARIO);

        double utilidad     = res.getUtilidadProyectada();
        Color  colorUtil    = utilidad >= 0 ? C_VERDE : C_ROJO;
        dibujarKPI(g, MARGIN + (cardW + 10) * 2, y, cardW, cardH,
                "Utilidad Proyectada",
                String.format("$%,.2f", utilidad), colorUtil);

        y += cardH + 14;
        dibujarIndicadoresSecundarios(g, res, y);
        return y + 55;
    }

    private void dibujarKPI(Graphics2D g, int x, int y, int w, int h,
                             String etiqueta, String valor, Color color) {
        GradientPaint gp = new GradientPaint(x, y, color, x, y + h, color.darker());
        g.setPaint(gp);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 8, 8));

        g.setColor(new Color(255, 255, 255, 170));
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.drawString(etiqueta, x + 8, y + 16);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.drawString(valor, x + 8, y + h - 13);
    }

    private void dibujarIndicadoresSecundarios(Graphics2D g, ResultadoPrediccion res, int y) {
        int cardW = (CONTENT_W - 10) / 2;
        int cardH = 44;

        PlantillaPDF.dibujarTarjetaInfo(g, MARGIN, y, cardW, cardH,
                "Clasificación de Tendencia",
                res.getClasificacionTendencia(),
                PlantillaPDF.colorPorClasificacion(res.getClasificacionTendencia()));

        PlantillaPDF.dibujarTarjetaInfo(g, MARGIN + cardW + 10, y, cardW, cardH,
                "Nivel de Confianza del Modelo",
                res.getNivelConfianza() + "  (" + res.getHistorial().size() + " meses de datos)",
                C_SECUNDARIO);

        y += cardH + 8;
        String varTexto = String.format("Variación vs. último mes real: %+.2f%%",
                res.getTendenciaPorcentaje());
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(res.getTendenciaPorcentaje() >= 0 ? C_VERDE : C_ROJO);
        g.drawString(varTexto, MARGIN, y + 14);
    }

    private int dibujarRecomendaciones(Graphics2D g, ResultadoPrediccion res, int y) {
        PlantillaPDF.dibujarTituloSeccion(g, "Interpretación y Recomendaciones", y);
        y += 28;

        String[] recomendaciones = generarRecomendaciones(res);
        int alturaBloque = 12 + recomendaciones.length * 20;

        g.setColor(C_ACENTO);
        g.fillRoundRect(MARGIN, y, CONTENT_W, alturaBloque, 6, 6);
        g.setColor(C_SECUNDARIO);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(MARGIN, y, CONTENT_W, alturaBloque, 6, 6);
        g.setStroke(new BasicStroke(1));

        y += 16;
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (String rec : recomendaciones) {
            g.setColor(Color.DARK_GRAY);
            g.drawString("  •  " + rec, MARGIN + 8, y);
            y += 20;
        }
        return y + 10;
    }

    private String[] generarRecomendaciones(ResultadoPrediccion res) {
        String clasificacion = res.getClasificacionTendencia();
        double tendencia     = res.getTendenciaPorcentaje();
        double utilidad      = res.getUtilidadProyectada();

        List<String> lista = new ArrayList<>();
        switch (clasificacion) {
            case "CRECIMIENTO":
                lista.add(String.format("Las ventas proyectan un crecimiento del %.1f%% respecto al mes anterior.", tendencia));
                lista.add("Considerar aumentar el inventario y la capacidad operativa para sostener la demanda.");
                lista.add("Evaluar si el flujo de caja actual soporta el crecimiento proyectado.");
                break;
            case "DECRECIMIENTO":
                lista.add(String.format("Las ventas proyectan una caída del %.1f%%, requiere atención inmediata.", Math.abs(tendencia)));
                lista.add("Revisar estrategias de ventas, precios y retención de clientes.");
                lista.add("Reducir gastos no esenciales para proteger la utilidad operativa.");
                break;
            default:
                lista.add("Las ventas se mantienen estables; el mercado muestra comportamiento predecible.");
                lista.add("Mantener la estrategia actual y buscar oportunidades de crecimiento moderado.");
        }
        if (utilidad < 0)
            lista.add("ALERTA: La utilidad proyectada es negativa. Revisar estructura de costos urgentemente.");
        if ("Bajo".equals(res.getNivelConfianza()))
            lista.add("NOTA: Con menos de 6 meses de datos el modelo tiene menor precisión. Agregar más historial mejorará las predicciones.");

        return lista.toArray(new String[0]);
    }
}
