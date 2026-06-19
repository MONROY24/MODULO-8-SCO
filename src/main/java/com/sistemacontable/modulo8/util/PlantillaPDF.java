package com.sistemacontable.modulo8.util;

import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Plantilla principal para documentos PDF.
 * Contiene paletas de colores, márgenes, métodos comunes de dibujo
 * y la lógica de bajo nivel para codificar y guardar el archivo PDF.
 */
public class PlantillaPDF {

    public static final int PAGE_W    = 595;
    public static final int PAGE_H    = 842;
    public static final int MARGIN    = 48;
    public static final int CONTENT_W = PAGE_W - 2 * MARGIN;
    public static final int SCALE     = 4;

    public static final Color C_PRIMARIO   = new Color(30,  80, 160);
    public static final Color C_SECUNDARIO = new Color(59, 130, 246);
    public static final Color C_ACENTO     = new Color(240, 245, 255);
    public static final Color C_VERDE      = new Color(22,  163,  74);
    public static final Color C_ROJO       = new Color(220,  38,  38);
    public static final Color C_AMARILLO   = new Color(234, 179,   8);
    public static final Color C_GRIS       = new Color(100, 116, 139);
    public static final Color C_LINEA      = new Color(226, 232, 240);

    public static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static BufferedImage nuevaPagina() {
        return new BufferedImage(PAGE_W * SCALE, PAGE_H * SCALE, BufferedImage.TYPE_INT_RGB);
    }

    public static Graphics2D crearGraphics(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        g.scale(SCALE, SCALE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, PAGE_W, PAGE_H);
        return g;
    }

    public static Color colorPorClasificacion(String c) {
        return switch (c) {
            case "CRECIMIENTO"   -> C_VERDE;
            case "DECRECIMIENTO" -> C_ROJO;
            default              -> C_AMARILLO;
        };
    }

    public static void dibujarTituloSeccion(Graphics2D g, String titulo, int y) {
        g.setColor(C_PRIMARIO);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString(titulo, MARGIN, y + 14);
        GradientPaint gp = new GradientPaint(MARGIN, 0, C_SECUNDARIO, MARGIN + CONTENT_W, 0, C_ACENTO);
        g.setPaint(gp);
        g.fillRect(MARGIN, y + 17, CONTENT_W, 2);
    }

    public static void dibujarTarjetaInfo(Graphics2D g, int x, int y, int w, int h,
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

    public static void dibujarPieDePagina(Graphics2D g, int pagActual, int pagTotal,
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

    public static void escribirPDF(OutputStream out, List<BufferedImage> paginas) throws IOException {
        List<byte[]> jpegs = new ArrayList<>();
        for (BufferedImage img : paginas) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "jpeg", baos);
            jpegs.add(baos.toByteArray());
        }

        byte[] header = "%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1);
        int    headerLen = header.length;

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

        offsets.add(headerLen + body.size());
        writeStr.accept("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        StringBuilder kids = new StringBuilder();
        int firstPageObj = 3;
        for (int i = 0; i < paginas.size(); i++) {
            kids.append(firstPageObj + i * 3).append(" 0 R ");
        }
        offsets.add(headerLen + body.size());
        writeStr.accept("2 0 obj\n<< /Type /Pages /Kids [" + kids + "] /Count "
                + paginas.size() + " >>\nendobj\n");

        for (int p = 0; p < paginas.size(); p++) {
            int pageObj  = firstPageObj + p * 3;
            int contObj  = pageObj + 1;
            int imgObj   = pageObj + 2;

            byte[] jpeg = jpegs.get(p);
            int    imgW = paginas.get(p).getWidth();
            int    imgH = paginas.get(p).getHeight();

            offsets.add(headerLen + body.size());
            writeStr.accept(pageObj + " 0 obj\n"
                    + "<< /Type /Page /Parent 2 0 R\n"
                    + "   /MediaBox [0 0 " + PAGE_W + " " + PAGE_H + "]\n"
                    + "   /Contents " + contObj + " 0 R\n"
                    + "   /Resources << /XObject << /Img" + p + " " + imgObj + " 0 R >> >>\n"
                    + ">>\nendobj\n");

            String cs      = "q " + PAGE_W + " 0 0 " + PAGE_H + " 0 0 cm /Img" + p + " Do Q\n";
            byte[] csBytes = cs.getBytes(StandardCharsets.ISO_8859_1);
            offsets.add(headerLen + body.size());
            writeStr.accept(contObj + " 0 obj\n<< /Length " + csBytes.length + " >>\nstream\n");
            writeBytes.accept(csBytes);
            writeStr.accept("\nendstream\nendobj\n");

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

        out.write(header);
        body.writeTo(out);
        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
    }
}
