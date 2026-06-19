package com.sistemacontable.modulo8.util;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.awt.*;
import java.util.List;

import static com.sistemacontable.modulo8.util.PlantillaPDF.*;

public class GeneradorGraficaPDF {

    public static int dibujarGrafica(Graphics2D g, ResultadoPrediccion res, int y) {
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
            String mes = r.getNombreMes();
            if (multiYear) {
                mes += String.format(" '%02d", r.getAnio() % 100);
            }
            g.drawString(mes, baseX + barMg, grafY + grafH + 12);
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
}
