package com.sistemacontable.modulo8.util;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Utilidad: Genera un reporte PDF del análisis predictivo.
 *
 * Implementación pura en Java usando Graphics2D + escritura manual de PDF.
 * No requiere dependencias externas (iText, PDFBox, etc.).
 *
 * El PDF generado incluye:
 *   Página 1: Encabezado corporativo · KPIs ejecutivos · Tabla de historial
 *   Página 2: Gráfica de barras + línea de tendencia · Recomendaciones
 */
public class ExportadorPDF {

    // ── Constantes de página A4 (en puntos / píxeles a 72 dpi) ───────────────
    private static final int PAGE_W    = 595;
    private static final int PAGE_H    = 842;
    private static final int MARGIN    = 48;
    private static final int CONTENT_W = PAGE_W - 2 * MARGIN;
    private static final int SCALE     = 4; // Renderizar 4x más grande para mayor resolución

    // ── Paleta ────────────────────────────────────────────────────────────────
    private static final Color C_PRIMARIO   = new Color(30,  80, 160);
    private static final Color C_SECUNDARIO = new Color(59, 130, 246);
    private static final Color C_ACENTO     = new Color(240, 245, 255);
    private static final Color C_VERDE      = new Color(22,  163,  74);
    private static final Color C_ROJO       = new Color(220,  38,  38);
    private static final Color C_AMARILLO   = new Color(234, 179,   8);
    private static final Color C_GRIS       = new Color(100, 116, 139);
    private static final Color C_LINEA      = new Color(226, 232, 240);

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Genera el PDF del análisis y lo guarda en la ruta indicada.
     *
     * @param resultado   Resultado del análisis predictivo.
     * @param rutaArchivo Ruta completa del PDF a crear (ej: "C:/reportes/reporte.pdf").
     * @throws IOException si hay error de escritura en disco.
     */
    public void generarPDF(ResultadoPrediccion resultado, String rutaArchivo) throws IOException {
        List<BufferedImage> paginas = renderizarPaginas(resultado);
        try (FileOutputStream fos = new FileOutputStream(rutaArchivo)) {
            escribirPDF(fos, paginas);
        }
    }

    // ── Renderizado de páginas ────────────────────────────────────────────────

    private List<BufferedImage> renderizarPaginas(ResultadoPrediccion res) {
        List<BufferedImage> lista = new ArrayList<>();
        lista.add(renderizarPagina1(res));
        lista.add(renderizarPagina2(res));
        return lista;
    }

    // ─────────────────────────────── PÁGINA 1 ────────────────────────────────

    private BufferedImage renderizarPagina1(ResultadoPrediccion res) {
        BufferedImage img = nuevaPagina();
        Graphics2D    g   = crearGraphics(img);

        int y = dibujarEncabezado(g, res);
        y += 22;
        y  = dibujarResumenEjecutivo(g, res, y);
        y += 22;
             dibujarTablaHistorial(g, res, y);
             dibujarPieDePagina(g, 1, 2, res);

        g.dispose();
        return img;
    }

    // ─────────────────────────────── PÁGINA 2 ────────────────────────────────

    private BufferedImage renderizarPagina2(ResultadoPrediccion res) {
        BufferedImage img = nuevaPagina();
        Graphics2D    g   = crearGraphics(img);

        int y = MARGIN + 10;
        dibujarTituloSeccion(g, "Análisis de Tendencia y Gráfica Histórica", y);
        y += 30;
        y  = dibujarGrafica(g, res, y);
        y += 22;
             dibujarRecomendaciones(g, res, y);
             dibujarPieDePagina(g, 2, 2, res);

        g.dispose();
        return img;
    }

    // ── Componentes visuales ──────────────────────────────────────────────────

    private int dibujarEncabezado(Graphics2D g, ResultadoPrediccion res) {
        // Banda superior con degradado
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

        // Línea ámbar decorativa
        g.setColor(C_AMARILLO);
        g.fillRect(0, 76, PAGE_W, 4);

        // Datos de empresa
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
        dibujarTituloSeccion(g, "Resumen Ejecutivo de Predicción", y);
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

        dibujarTarjetaInfo(g, MARGIN, y, cardW, cardH,
                "Clasificación de Tendencia",
                res.getClasificacionTendencia(),
                colorPorClasificacion(res.getClasificacionTendencia()));

        dibujarTarjetaInfo(g, MARGIN + cardW + 10, y, cardW, cardH,
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

    private void dibujarTarjetaInfo(Graphics2D g, int x, int y, int w, int h,
                                     String etiqueta, String valor, Color color) {
        g.setColor(C_ACENTO);
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 6, 6));
        g.setColor(color);
        g.setStroke(new BasicStroke(1.8f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 6, 6));
        g.setStroke(new BasicStroke(1));

        g.setColor(C_GRIS);
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.drawString(etiqueta, x + 8, y + 14);

        g.setColor(color);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(valor, x + 8, y + h - 10);
    }

    private int dibujarTablaHistorial(Graphics2D g, ResultadoPrediccion res, int y) {
        dibujarTituloSeccion(g, "Historial Financiero Mensual", y);
        y += 28;

        List<RegistroFinanciero> hist = res.getHistorial();
        int[] colX = {MARGIN, MARGIN + 58, MARGIN + 155, MARGIN + 275, MARGIN + 385};
        String[] cabeceras = {"Mes", "Período", "Total Compras ($)", "Total Ventas ($)", "Utilidad ($)"};
        int rowH = 22;

        // Cabecera
        g.setColor(C_PRIMARIO);
        g.fillRect(MARGIN, y, CONTENT_W, rowH);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        for (int i = 0; i < cabeceras.length; i++) {
            g.drawString(cabeceras[i], colX[i] + 4, y + 15);
        }
        y += rowH;

        // Filas de datos
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 0; i < hist.size(); i++) {
            RegistroFinanciero r = hist.get(i);
            g.setColor(i % 2 == 0 ? Color.WHITE : C_ACENTO);
            g.fillRect(MARGIN, y, CONTENT_W, rowH);

            g.setColor(Color.DARK_GRAY);
            g.drawString(r.getNombreMes(),                          colX[0] + 4, y + 15);
            g.drawString(r.getMesNumero() + "/" + r.getAnio(),      colX[1] + 4, y + 15);
            g.drawString(String.format("%,.2f", r.getTotalCompras()), colX[2] + 4, y + 15);
            g.drawString(String.format("%,.2f", r.getTotalVentas()),  colX[3] + 4, y + 15);
            double util = r.getTotalVentas() - r.getTotalCompras();
            g.setColor(util >= 0 ? C_VERDE : C_ROJO);
            g.drawString(String.format("%,.2f", util),              colX[4] + 4, y + 15);

            y += rowH;
            g.setColor(C_LINEA);
            g.drawLine(MARGIN, y, MARGIN + CONTENT_W, y);
        }

        // Fila de predicción
        y += 4;
        g.setColor(new Color(219, 234, 254));
        g.fillRoundRect(MARGIN, y, CONTENT_W, rowH + 2, 4, 4);
        g.setColor(C_PRIMARIO);
        g.setFont(new Font("SansSerif", Font.BOLD, 10));
        g.drawString("PREDICCIÓN →",                                     colX[0] + 4, y + 16);
        g.drawString("Próx. mes",                                        colX[1] + 4, y + 16);
        g.setColor(C_SECUNDARIO);
        g.drawString(String.format("%,.2f", res.getComprasPredichas()), colX[2] + 4, y + 16);
        g.drawString(String.format("%,.2f", res.getVentasPredichas()),  colX[3] + 4, y + 16);
        double utilProy = res.getUtilidadProyectada();
        g.setColor(utilProy >= 0 ? C_VERDE : C_ROJO);
        g.drawString(String.format("%,.2f", utilProy),                  colX[4] + 4, y + 16);

        return y + rowH + 10;
    }

    private int dibujarGrafica(Graphics2D g, ResultadoPrediccion res, int y) {
        List<RegistroFinanciero> hist = res.getHistorial();
        int grafH = 200, grafW = CONTENT_W;
        int grafX = MARGIN,   grafY = y;

        // Fondo
        g.setColor(new Color(248, 250, 252));
        g.fillRect(grafX, grafY, grafW, grafH);
        g.setColor(C_LINEA);
        g.drawRect(grafX, grafY, grafW, grafH);

        double maxVal = 0;
        for (RegistroFinanciero r : hist)
            maxVal = Math.max(maxVal, Math.max(r.getTotalVentas(), r.getTotalCompras()));
        maxVal = Math.max(maxVal, res.getVentasPredichas()) * 1.15;

        int n      = hist.size() + 1;
        int barMg  = 8;
        int grupW  = (grafW - 40) / n;
        int barW   = Math.max(8, (grupW - barMg * 2) / 2);

        // Cuadrícula
        g.setFont(new Font("SansSerif", Font.PLAIN, 8));
        for (int q = 0; q <= 4; q++) {
            int lineY = grafY + grafH - (int)(grafH * q / 4.0);
            g.setColor(C_LINEA);
            g.drawLine(grafX + 30, lineY, grafX + grafW, lineY);
            g.setColor(C_GRIS);
            g.drawString(String.format("$%.0f", maxVal * q / 4.0), grafX + 2, lineY + 4);
        }

        // Barras históricas con gradiente
        for (int i = 0; i < hist.size(); i++) {
            RegistroFinanciero r = hist.get(i);
            int baseX = grafX + 35 + i * grupW;

            int hC = (int)(grafH * r.getTotalCompras() / maxVal);
            int hV = (int)(grafH * r.getTotalVentas()  / maxVal);

            // Compras (azul)
            GradientPaint gpC = new GradientPaint(
                    baseX + barMg, grafY + grafH - hC, C_SECUNDARIO.darker(),
                    baseX + barMg, grafY + grafH,       C_SECUNDARIO);
            g.setPaint(gpC);
            g.fillRect(baseX + barMg, grafY + grafH - hC, barW, hC);

            // Ventas (verde)
            GradientPaint gpV = new GradientPaint(
                    baseX + barMg + barW + 2, grafY + grafH - hV, C_VERDE.darker(),
                    baseX + barMg + barW + 2, grafY + grafH,       C_VERDE);
            g.setPaint(gpV);
            g.fillRect(baseX + barMg + barW + 2, grafY + grafH - hV, barW, hV);

            g.setColor(C_GRIS);
            g.setFont(new Font("SansSerif", Font.PLAIN, 8));
            g.drawString(r.getNombreMes(), baseX + barMg, grafY + grafH + 12);
        }

        // Barra de predicción (ámbar)
        int predX  = grafX + 35 + hist.size() * grupW;
        int altPredV = (int)(grafH * res.getVentasPredichas()  / maxVal);
        int altPredC = (int)(grafH * res.getComprasPredichas() / maxVal);

        g.setColor(new Color(254, 243, 199, 120));
        g.fillRect(predX, grafY, grupW, grafH);

        GradientPaint gpPC = new GradientPaint(predX + barMg, grafY + grafH - altPredC,
                C_AMARILLO.darker(), predX + barMg, grafY + grafH, C_AMARILLO);
        g.setPaint(gpPC);
        g.fillRect(predX + barMg, grafY + grafH - altPredC, barW, altPredC);

        GradientPaint gpPV = new GradientPaint(predX + barMg + barW + 2, grafY + grafH - altPredV,
                new Color(0, 150, 50), predX + barMg + barW + 2, grafY + grafH, new Color(0, 200, 80));
        g.setPaint(gpPV);
        g.fillRect(predX + barMg + barW + 2, grafY + grafH - altPredV, barW, altPredV);

        g.setColor(C_GRIS);
        g.setFont(new Font("SansSerif", Font.BOLD, 8));
        g.drawString("Pred.", predX + barMg, grafY + grafH + 12);

        // Leyenda
        y = grafY + grafH + 22;
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.setPaint(C_SECUNDARIO); g.fillRect(MARGIN,      y, 12, 10);
        g.setColor(Color.DARK_GRAY); g.drawString("Compras",    MARGIN + 16,  y + 9);
        g.setPaint(C_VERDE);         g.fillRect(MARGIN + 75, y, 12, 10);
        g.setColor(Color.DARK_GRAY); g.drawString("Ventas",     MARGIN + 91,  y + 9);
        g.setPaint(C_AMARILLO);      g.fillRect(MARGIN + 145, y, 12, 10);
        g.setColor(Color.DARK_GRAY); g.drawString("Predicción", MARGIN + 161, y + 9);

        return y + 20;
    }

    private int dibujarRecomendaciones(Graphics2D g, ResultadoPrediccion res, int y) {
        dibujarTituloSeccion(g, "Interpretación y Recomendaciones", y);
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

    private void dibujarTituloSeccion(Graphics2D g, String titulo, int y) {
        g.setColor(C_PRIMARIO);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(titulo, MARGIN, y + 14);
        GradientPaint gp = new GradientPaint(MARGIN, 0, C_SECUNDARIO, MARGIN + CONTENT_W, 0, C_ACENTO);
        g.setPaint(gp);
        g.fillRect(MARGIN, y + 17, CONTENT_W, 2);
    }

    private void dibujarPieDePagina(Graphics2D g, int pagActual, int pagTotal,
                                     ResultadoPrediccion res) {
        int pieY = PAGE_H - 36;
        g.setColor(C_LINEA);
        g.fillRect(0, pieY, PAGE_W, 1);

        g.setFont(new Font("SansSerif", Font.PLAIN, 8));
        g.setColor(C_GRIS);
        g.drawString("Módulo 8 – Análisis Predictivo con IA  |  " +
                "Generado el " + res.getFechaAnalisis().format(FMT_FECHA), MARGIN, pieY + 18);

        String pagTexto = "Página " + pagActual + " de " + pagTotal;
        FontMetrics fm  = g.getFontMetrics();
        g.drawString(pagTexto, PAGE_W - MARGIN - fm.stringWidth(pagTexto), pieY + 18);
    }

    // ── Helpers de renderizado ────────────────────────────────────────────────

    private BufferedImage nuevaPagina() {
        return new BufferedImage(PAGE_W * SCALE, PAGE_H * SCALE, BufferedImage.TYPE_INT_RGB);
    }

    private Graphics2D crearGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.scale(SCALE, SCALE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, PAGE_W, PAGE_H);
        return g;
    }

    private Color colorPorClasificacion(String c) {
        return switch (c) {
            case "CRECIMIENTO"   -> C_VERDE;
            case "DECRECIMIENTO" -> C_ROJO;
            default              -> C_AMARILLO;
        };
    }

    // ── Escritura PDF (estructura mínima válida, imágenes JPEG embebidas) ─────

    /**
     * Genera un archivo PDF mínimo válido embebiendo las páginas como imágenes JPEG.
     *
     * FIX: Se calcula correctamente el offset del header "%PDF-1.4\n"
     * antes de construir la tabla xref, garantizando que los offsets
     * del xref sean absolutos respecto al inicio del archivo.
     */
    private void escribirPDF(OutputStream out, List<BufferedImage> paginas) throws IOException {

        // 1. Codificar páginas como JPEG en memoria
        List<byte[]> jpegs = new ArrayList<>();
        for (BufferedImage img : paginas) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "jpeg", baos);
            jpegs.add(baos.toByteArray());
        }

        // 2. Header del PDF (se escribe primero; su longitud afecta los offsets)
        byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1);
        int    headerLen = header.length;

        // 3. Construir cuerpo del PDF en memoria para calcular offsets exactos
        ByteArrayOutputStream body    = new ByteArrayOutputStream();
        List<Integer>         offsets = new ArrayList<>();

        java.util.function.Consumer<String> writeStr = s -> {
            try { body.write(s.getBytes(StandardCharsets.ISO_8859_1)); }
            catch (IOException ex) { throw new RuntimeException(ex); }
        };
        java.util.function.Consumer<byte[]> writeBytes = b -> {
            try { body.write(b); }
            catch (IOException ex) { throw new RuntimeException(ex); }
        };

        // Objeto 1 – Catálogo
        offsets.add(headerLen + body.size());
        writeStr.accept("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        // Objeto 2 – Pages
        StringBuilder kids = new StringBuilder();
        int firstPageObj = 3;
        for (int i = 0; i < paginas.size(); i++) {
            kids.append(firstPageObj + i * 3).append(" 0 R ");
        }
        offsets.add(headerLen + body.size());
        writeStr.accept("2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count "
                + paginas.size() + " >>\nendobj\n");

        // Objetos por página: Page dict + Content stream + XObject (imagen JPEG)
        for (int p = 0; p < paginas.size(); p++) {
            int pageObj  = firstPageObj + p * 3;
            int contObj  = pageObj + 1;
            int imgObj   = pageObj + 2;

            byte[] jpeg = jpegs.get(p);
            int    imgW = paginas.get(p).getWidth();
            int    imgH = paginas.get(p).getHeight();

            // Page
            offsets.add(headerLen + body.size());
            writeStr.accept(pageObj + " 0 obj\n"
                    + "<< /Type /Page /Parent 2 0 R\n"
                    + "   /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "]\n"
                    + "   /Contents " + contObj + " 0 R\n"
                    + "   /Resources << /XObject << /Img" + p + " " + imgObj + " 0 R >> >>\n"
                    + ">>\nendobj\n");

            // Content stream: dibuja la imagen cubriendo toda la página
            String cs      = "q " + PAGE_W + " 0 0 " + PAGE_H + " 0 0 cm /Img" + p + " Do Q\n";
            byte[] csBytes = cs.getBytes(StandardCharsets.ISO_8859_1);
            offsets.add(headerLen + body.size());
            writeStr.accept(contObj + " 0 obj\n<< /Length " + csBytes.length + " >>\nstream\n");
            writeBytes.accept(csBytes);
            writeStr.accept("\nendstream\nendobj\n");

            // XObject imagen JPEG
            offsets.add(headerLen + body.size());
            writeStr.accept(imgObj + " 0 obj\n"
                    + "<< /Type /XObject /Subtype /Image\n"
                    + "   /Width " + imgW + " /Height " + imgH + "\n"
                    + "   /ColorSpace /DeviceRGB /BitsPerComponent 8\n"
                    + "   /Filter /DCTDecode /Length " + jpeg.length + "\n"
                    + ">>\nstream\n");
            writeBytes.accept(jpeg);
            writeStr.accept("\nendstream\nendobj\n");
        }

        // 4. Tabla xref y trailer
        int    xrefOffset = headerLen + body.size();
        int    totalObjs  = offsets.size() + 1;

        StringBuilder xref = new StringBuilder();
        xref.append("xref\n0 ").append(totalObjs).append("\n");
        xref.append("0000000000 65535 f \n");
        for (int off : offsets) {
            xref.append(String.format("%010d 00000 n \n", off));
        }
        xref.append("trailer\n<< /Size ").append(totalObjs)
            .append(" /Root 1 0 R >>\nstartxref\n")
            .append(xrefOffset).append("\n%%EOF\n");

        // 5. Escribir al stream de salida: header + body + xref
        out.write(header);
        body.writeTo(out);
        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
