package com.sistemacontable.modulo8.util;

import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.awt.*;
import java.util.List;

import static com.sistemacontable.modulo8.util.PlantillaPDF.*;

public class GeneradorTablasPDF {

    public static int dibujarTablaHistorial(Graphics2D g, ResultadoPrediccion res, int y) {
        PlantillaPDF.dibujarTituloSeccion(g, "Historial Financiero Mensual", y);
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
}
