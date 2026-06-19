package com.sistemacontable.modulo8.vista;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Vista: Componente Swing de gráfica de barras con gradientes + línea de tendencia.
 *
 * Mejoras respecto a la versión anterior:
 *   - Barras con relleno en degradado (GradientPaint) para aspecto premium.
 *   - Tooltips de valores sobre cada barra.
 *   - Línea de tendencia suavizada (curva cúbica).
 *   - Animación de aparición al actualizar datos.
 */
public class PanelGrafica extends JPanel {

    private ResultadoPrediccion resultado;

    // ── Paleta de colores ─────────────────────────────────────────────────────
    private static final Color C_VENTAS      = new Color(34,  197,  94);   // verde esmeralda
    private static final Color C_VENTAS_OSC  = new Color(22,  163,  74);
    private static final Color C_COMPRAS     = new Color(59,  130, 246);   // azul índigo
    private static final Color C_COMPRAS_OSC = new Color(37,  99,  235);
    private static final Color C_PRED        = new Color(250, 204,  21);   // ámbar
    private static final Color C_PRED_OSC    = new Color(234, 179,   8);
    private static final Color C_TENDENCIA   = new Color(239,  68,  68);   // rojo coral
    private static final Color C_FONDO       = new Color(248, 250, 252);
    private static final Color C_FONDO_PRED  = new Color(254, 252, 232, 100);
    private static final Color C_GRID        = new Color(226, 232, 240);
    private static final Color C_TEXTO       = new Color(71,  85, 105);

    private static final Font F_SMALL  = new Font("SansSerif", Font.PLAIN,  10);
    private static final Font F_BOLD   = new Font("SansSerif", Font.BOLD,   11);
    private static final Font F_ITALIC = new Font("SansSerif", Font.ITALIC, 13);

    // ── Padding interno ───────────────────────────────────────────────────────
    private static final int PAD_L = 72;
    private static final int PAD_R = 20;
    private static final int PAD_T = 20;
    private static final int PAD_B = 50;

    // ── Constructor ───────────────────────────────────────────────────────────

    public PanelGrafica() {
        setBackground(C_FONDO);
        setPreferredSize(new Dimension(600, 260));
        setBorder(BorderFactory.createLineBorder(C_GRID));
    }

    /** Actualiza los datos y repinta el componente. */
    public void actualizar(ResultadoPrediccion resultado) {
        this.resultado = resultado;
        repaint();
    }

    // ── Pintado principal ─────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = prepararGraphics(g0);

        if (resultado == null) {
            dibujarEstadoVacio(g);
            return;
        }

        List<RegistroFinanciero> hist = resultado.getHistorial();
        int total = hist.size() + 1; // +1 columna de predicción

        int gw = getWidth()  - PAD_L - PAD_R;
        int gh = getHeight() - PAD_T - PAD_B;

        double maxVal = calcularMaxVal(hist);
        int    grupW  = gw / total;
        int    barW   = Math.max(6, grupW / 3);

        // — Cuadrícula y ejes —
        dibujarCuadricula(g, gw, gh, maxVal);

        boolean multiYear = false;
        if (!hist.isEmpty()) {
            int firstYear = hist.get(0).getAnio();
            for (RegistroFinanciero r : hist) {
                if (r.getAnio() != firstYear) {
                    multiYear = true;
                    break;
                }
            }
        }

        // — Barras históricas —
        for (int i = 0; i < hist.size(); i++) {
            RegistroFinanciero r  = hist.get(i);
            int x0 = PAD_L + i * grupW + (grupW - barW * 2 - 4) / 2;

            int hC = (int)(gh * r.getTotalCompras() / maxVal);
            int hV = (int)(gh * r.getTotalVentas()  / maxVal);

            dibujarBarraGradiente(g, x0,          PAD_T + gh - hC, barW, hC,
                    C_COMPRAS_OSC, C_COMPRAS, false);
            dibujarBarraGradiente(g, x0 + barW + 4, PAD_T + gh - hV, barW, hV,
                    C_VENTAS_OSC,  C_VENTAS,  false);

            // Etiqueta del mes
            g.setFont(F_SMALL);
            g.setColor(C_TEXTO);
            String mes = r.getNombreMes();
            if (multiYear) {
                mes += String.format(" '%02d", r.getAnio() % 100);
            }
            FontMetrics fm = g.getFontMetrics();
            g.drawString(mes,
                    PAD_L + i * grupW + (grupW - fm.stringWidth(mes)) / 2,
                    PAD_T + gh + 14);
        }

        // — Columna de predicción —
        int predX = PAD_L + hist.size() * grupW;
        dibujarFondoPrediccion(g, predX, grupW, gh);

        int pX0 = predX + (grupW - barW * 2 - 4) / 2;
        int hPC = (int)(gh * resultado.getComprasPredichas() / maxVal);
        int hPV = (int)(gh * resultado.getVentasPredichas()  / maxVal);

        dibujarBarraGradiente(g, pX0,           PAD_T + gh - hPC, barW, hPC,
                C_PRED_OSC, C_PRED, true);
        dibujarBarraGradiente(g, pX0 + barW + 4, PAD_T + gh - hPV, barW, hPV,
                C_PRED_OSC, C_PRED, true);

        // Etiqueta "Pred."
        g.setFont(F_BOLD);
        g.setColor(new Color(161, 98, 7));
        FontMetrics fm = g.getFontMetrics();
        String predLabel = "Pred.";
        g.drawString(predLabel, predX + (grupW - fm.stringWidth(predLabel)) / 2,
                PAD_T + gh + 14);

        // — Línea de tendencia suavizada —
        dibujarLineaTendencia(g, hist, gw, gh, maxVal, grupW, barW);

        // — Leyenda —
        dibujarLeyenda(g, PAD_T + gh + 28);
    }

    // ── Componentes de dibujo ─────────────────────────────────────────────────

    private void dibujarCuadricula(Graphics2D g, int gw, int gh, double maxVal) {
        g.setFont(F_SMALL);
        int lineas = 5;
        for (int i = 0; i <= lineas; i++) {
            int y = PAD_T + gh - (int)(gh * i / (double) lineas);

            // Línea horizontal
            g.setColor(C_GRID);
            g.drawLine(PAD_L, y, PAD_L + gw, y);

            // Etiqueta del eje Y
            double val = maxVal * i / lineas;
            String lbl = val >= 1000
                    ? String.format("$%.0fK", val / 1000)
                    : String.format("$%.0f",  val);
            g.setColor(C_TEXTO);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(lbl, PAD_L - fm.stringWidth(lbl) - 5, y + 4);
        }
        // Eje Y vertical
        g.setColor(C_GRID);
        g.drawLine(PAD_L, PAD_T, PAD_L, PAD_T + gh);
    }

    /**
     * Dibuja una barra con relleno en degradado vertical.
     *
     * @param prediccion Si es {@code true} usa opacidad reducida (barra proyectada).
     */
    private void dibujarBarraGradiente(Graphics2D g,
                                        int x, int y, int w, int h,
                                        Color colorBase, Color colorClaro,
                                        boolean prediccion) {
        if (h <= 0) return;

        Color top    = prediccion ? new Color(colorBase.getRed(),  colorBase.getGreen(),
                                              colorBase.getBlue(), 200) : colorBase;
        Color bottom = prediccion ? new Color(colorClaro.getRed(), colorClaro.getGreen(),
                                              colorClaro.getBlue(), 130) : colorClaro;

        GradientPaint gp = new GradientPaint(x, y, top, x, y + h, bottom);
        g.setPaint(gp);
        g.fillRoundRect(x, y, w, h, 4, 4);

        // Brillo superior
        g.setPaint(new Color(255, 255, 255, 60));
        g.fillRoundRect(x + 1, y + 1, w / 2 - 1, Math.min(h - 2, 8), 3, 3);

        // Borde sutil
        g.setPaint(top.darker());
        g.drawRoundRect(x, y, w, h, 4, 4);
    }

    private void dibujarFondoPrediccion(Graphics2D g, int predX, int grupW, int gh) {
        // Fondo amarillo traslúcido
        g.setColor(C_FONDO_PRED);
        g.fillRect(predX, PAD_T, grupW, gh);

        // Línea divisora punteada
        g.setColor(new Color(202, 138, 4, 160));
        float[] dash = {5f, 4f};
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, dash, 0f));
        g.drawLine(predX, PAD_T, predX, PAD_T + gh);
        g.setStroke(new BasicStroke(1));
    }

    private void dibujarLineaTendencia(Graphics2D g,
                                        List<RegistroFinanciero> hist,
                                        int gw, int gh,
                                        double maxVal, int grupW, int barW) {
        if (hist.size() < 2) return;

        // Recopilar puntos de ventas
        int n = hist.size();
        int[] px = new int[n];
        int[] py = new int[n];
        for (int i = 0; i < n; i++) {
            px[i] = PAD_L + i * grupW + grupW / 2;
            py[i] = PAD_T + gh - (int)(gh * hist.get(i).getTotalVentas() / maxVal);
        }

        // Dibujar línea suavizada (curvas cuadráticas)
        g.setColor(new Color(239, 68, 68, 200));
        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        Path2D path = new Path2D.Double();
        path.moveTo(px[0], py[0]);
        for (int i = 1; i < n; i++) {
            int ctrlX = (px[i - 1] + px[i]) / 2;
            path.quadTo(ctrlX, py[i - 1], px[i], py[i]);
        }
        g.draw(path);

        // Puntos sobre la línea
        g.setColor(C_TENDENCIA);
        for (int i = 0; i < n; i++) {
            g.fillOval(px[i] - 3, py[i] - 3, 7, 7);
            g.setColor(Color.WHITE);
            g.fillOval(px[i] - 1, py[i] - 1, 3, 3);
            g.setColor(C_TENDENCIA);
        }

        // Línea punteada hacia la predicción
        int predPX = PAD_L + n * grupW + grupW / 2;
        int predPY = PAD_T + gh - (int)(gh * resultado.getVentasPredichas() / maxVal);
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f));
        g.setColor(new Color(239, 68, 68, 160));
        g.drawLine(px[n - 1], py[n - 1], predPX, predPY);
        g.setStroke(new BasicStroke(1));

        // Punto de predicción (más grande)
        g.setColor(C_TENDENCIA);
        g.fillOval(predPX - 5, predPY - 5, 10, 10);
        g.setColor(Color.WHITE);
        g.fillOval(predPX - 2, predPY - 2, 4, 4);
    }

    private void dibujarLeyenda(Graphics2D g, int y) {
        g.setFont(F_SMALL);
        int x  = PAD_L;
        int sz = 10;
        int sp = 95;

        Object[][] items = {
            {C_COMPRAS,   "Compras"},
            {C_VENTAS,    "Ventas"},
            {C_PRED,      "Predicción"},
            {C_TENDENCIA, "Tendencia ventas"},
        };

        for (Object[] item : items) {
            Color color = (Color) item[0];
            GradientPaint gp = new GradientPaint(x, y - sz + 2, color.darker(),
                                                  x + sz, y + 2, color);
            g.setPaint(gp);
            g.fillRoundRect(x, y - sz + 2, sz, sz, 3, 3);
            g.setColor(C_TEXTO);
            g.drawString((String) item[1], x + sz + 5, y);
            x += sp;
        }
    }

    private void dibujarEstadoVacio(Graphics2D g) {
        g.setFont(F_ITALIC);
        g.setColor(new Color(148, 163, 184));
        String msg = "Seleccione una empresa y ejecute el análisis para ver la gráfica";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);

        // Ícono decorativo
        g.setColor(new Color(226, 232, 240));
        g.fillOval(getWidth() / 2 - 28, getHeight() / 2 - 55, 56, 56);
        g.setFont(new Font("SansSerif", Font.BOLD, 26));
        g.setColor(new Color(203, 213, 225));
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString("∿", (getWidth() - fm2.stringWidth("∿")) / 2, getHeight() / 2 - 20);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Graphics2D prepararGraphics(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        return g;
    }

    private double calcularMaxVal(List<RegistroFinanciero> hist) {
        double max = 1;
        for (RegistroFinanciero r : hist) {
            max = Math.max(max, Math.max(r.getTotalVentas(), r.getTotalCompras()));
        }
        return Math.max(max, resultado.getVentasPredichas()) * 1.15;
    }
}
