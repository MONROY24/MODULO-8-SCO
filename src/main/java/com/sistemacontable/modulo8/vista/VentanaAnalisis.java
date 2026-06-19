package com.sistemacontable.modulo8.vista;

import com.sistemacontable.modulo8.controlador.AnalisisControlador;
import com.sistemacontable.modulo8.modelo.EmpresaItem;
import com.sistemacontable.modulo8.modelo.RegistroFinanciero;
import com.sistemacontable.modulo8.modelo.ResultadoPrediccion;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MÓDULO 8 – Análisis Predictivo con IA
 * Vista principal del módulo (Swing).
 *
 * Layout general:
 *   ┌─────────────────────────────────────────────────────┐
 *   │  HEADER  (título + subtítulo)                       │
 *   ├─────────────────────────────────────────────────────┤
 *   │  PANEL CONTROL  │  KPIs (5 tarjetas)               │
 *   │  Empresa ▼      ├──────────────────────────────────┤
 *   │  ▶ Analizar     │  Gráfica de tendencia            │
 *   │  📄 PDF         ├──────────────────────────────────┤
 *   │  Info modelo    │  Tabla de historial financiero   │
 *   └─────────────────────────────────────────────────────┘
 *
 * Mejoras:
 *   - Tipografía consistente y moderna.
 *   - Ícono de ventana generado programáticamente.
 *   - Panel de control con separador visual mejorado.
 *   - Tabla con colores alternados y renderer de color para utilidad.
 */
public class VentanaAnalisis extends JFrame {

    // ── Controlador ───────────────────────────────────────────────────────────
    private AnalisisControlador controlador;

    // ── Componentes de control ────────────────────────────────────────────────
    private JComboBox<EmpresaItem> cmbEmpresa;
    private JButton           btnAnalizar;
    private JButton           btnExportarPDF;
    private JProgressBar      progressBar;

    // ── Componentes de resultado ──────────────────────────────────────────────
    private JLabel lblVentasPredichas;
    private JLabel lblComprasPredichas;
    private JLabel lblUtilidad;
    private JLabel lblTendencia;
    private JLabel lblConfianza;
    private JLabel lblFecha;
    private JLabel lblTiempo;

    private PanelGrafica      panelGrafica;
    private DefaultTableModel modeloTabla;
    private JTable            tablaHistorial;

    // ── Paleta de colores ─────────────────────────────────────────────────────
    private static final Color C_PRIMARIO   = new Color(30,   72, 156);
    private static final Color C_SECUNDARIO = new Color(59,  130, 246);
    private static final Color C_FONDO      = new Color(241, 245, 249);
    private static final Color C_PANEL      = new Color(255, 255, 255);
    private static final Color C_VERDE      = new Color(22,  163,  74);
    private static final Color C_ROJO       = new Color(220,  38,  38);
    private static final Color C_AMARILLO   = new Color(234, 179,   8);
    private static final Color C_GRIS_TEXTO = new Color(100, 116, 139);
    private static final Color C_BORDE      = new Color(226, 232, 240);
    private static final Color C_BLANCO     = Color.WHITE;
    private static final Color C_FILA_PAR   = new Color(248, 250, 252);
    private static final Color C_FILA_IMPAR = Color.WHITE;

    // ── Tipografía ────────────────────────────────────────────────────────────
    private static final Font F_TITULO    = new Font("SansSerif", Font.BOLD,  18);
    private static final Font F_SUBTITULO = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font F_LABEL     = new Font("SansSerif", Font.BOLD,  11);
    private static final Font F_NORMAL    = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font F_SMALL     = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font F_TINY      = new Font("SansSerif", Font.ITALIC,  9);
    private static final Font F_KPI_VAL   = new Font("SansSerif", Font.BOLD,  15);
    private static final Font F_KPI_LBL   = new Font("SansSerif", Font.PLAIN,  9);

    // ── Constructor ───────────────────────────────────────────────────────────

    public VentanaAnalisis() {
        super("Módulo 8 – Análisis Predictivo con Inteligencia Artificial");
        configurarVentana();
        construirUI();
    }

    /** Conecta el controlador después del constructor. */
    public void setControlador(AnalisisControlador controlador) {
        this.controlador = controlador;
    }

    // ── Configuración de ventana ──────────────────────────────────────────────

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(920, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(C_FONDO);
        setLayout(new BorderLayout(0, 0));
        setIconImage(crearIconoVentana());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    /** Genera un ícono de ventana con degradado azul y símbolo de IA. */
    private Image crearIconoVentana() {
        int sz = 32;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(sz, sz, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, C_PRIMARIO, sz, sz, C_SECUNDARIO));
        g.fillRoundRect(0, 0, sz, sz, 8, 8);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("AI", (sz - fm.stringWidth("AI")) / 2, sz / 2 + fm.getAscent() / 2 - 2);
        g.dispose();
        return img;
    }

    // ── Construcción de la interfaz ───────────────────────────────────────────

    private void construirUI() {
        add(crearHeader(),       BorderLayout.NORTH);
        add(crearPanelControl(), BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout(0, 8));
        centro.setBackground(C_FONDO);
        centro.setBorder(new EmptyBorder(8, 8, 8, 8));
        centro.add(crearPanelKPIs(),    BorderLayout.NORTH);
        centro.add(crearPanelGrafica(), BorderLayout.CENTER);
        centro.add(crearPanelTabla(),   BorderLayout.SOUTH);
        add(centro, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, C_PRIMARIO, getWidth(), 0,
                        new Color(49, 112, 204)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(0, 64));
        header.setBorder(new EmptyBorder(0, 18, 0, 18));

        JLabel titulo = new JLabel("  Módulo 8 — Análisis Predictivo con IA");
        titulo.setFont(F_TITULO);
        titulo.setForeground(C_BLANCO);
        titulo.setIcon(new CircleIcon(18, C_AMARILLO));

        JLabel subtitulo = new JLabel("Sistema Contable Computarizado · Red Neuronal MLP · Ciclo I-2026");
        subtitulo.setFont(F_SUBTITULO);
        subtitulo.setForeground(new Color(186, 207, 240));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);
        header.add(textos, BorderLayout.WEST);

        // Línea inferior ámbar
        JPanel linea = new JPanel();
        linea.setOpaque(true);
        linea.setBackground(C_AMARILLO);
        linea.setPreferredSize(new Dimension(0, 3));
        header.add(linea, BorderLayout.SOUTH);

        return header;
    }

    // ── Panel de control lateral ──────────────────────────────────────────────

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(C_PANEL);
        panel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(18, 14, 16, 14)));
        panel.setPreferredSize(new Dimension(215, 0));

        // Selector de empresa
        JLabel lblEmpresa = nuevaEtiqueta("Seleccionar Empresa:", F_LABEL, C_PRIMARIO);

        cmbEmpresa = new JComboBox<>();
        cmbEmpresa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbEmpresa.setAlignmentX(LEFT_ALIGNMENT);
        cmbEmpresa.setFont(F_NORMAL);
        cmbEmpresa.setBackground(C_BLANCO);

        // Botones
        btnAnalizar    = crearBoton("▶  Ejecutar Análisis", C_PRIMARIO,   C_BLANCO);
        btnExportarPDF = crearBoton("📄  Exportar a PDF",   C_VERDE,      C_BLANCO);
        btnExportarPDF.setEnabled(false);

        btnAnalizar.addActionListener(e -> {
            EmpresaItem seleccion = (EmpresaItem) cmbEmpresa.getSelectedItem();
            if (seleccion == null) return;
            controlador.ejecutarAnalisis(seleccion.getId());
        });
        btnExportarPDF.addActionListener(e -> controlador.exportarPDF());

        // Barra de progreso
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        progressBar.setAlignmentX(LEFT_ALIGNMENT);
        progressBar.setForeground(C_SECUNDARIO);
        progressBar.setBorderPainted(false);

        // Info del modelo
        JTextArea info = new JTextArea(
                "Red Neuronal Artificial MLP\n" +
                "implementada en Java puro.\n\n" +
                "Arquitectura:\n" +
                "  Entrada → 3 neuronas\n" +
                "  Oculta 1 → 8 (ReLU)\n" +
                "  Oculta 2 → 4 (ReLU)\n" +
                "  Salida → 2 (Sigmoid)\n\n" +
                "Épocas de entrenamiento: 5.000\n" +
                "Tasa de aprendizaje: 0.05\n\n" +
                "Requiere ≥ 3 meses de historial.");
        info.setFont(F_SMALL);
        info.setForeground(C_GRIS_TEXTO);
        info.setBackground(C_PANEL);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setAlignmentX(LEFT_ALIGNMENT);
        info.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(C_BORDE),
                " Acerca del Modelo ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 10), C_PRIMARIO));

        // Etiquetas de estado
        lblFecha  = nuevaEtiqueta("Fecha: —",              F_TINY, C_GRIS_TEXTO);
        lblTiempo = nuevaEtiqueta("Entrenamiento: —",      F_TINY, C_GRIS_TEXTO);

        panel.add(lblEmpresa);
        panel.add(Box.createVerticalStrut(5));
        panel.add(cmbEmpresa);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnAnalizar);
        panel.add(Box.createVerticalStrut(6));
        panel.add(btnExportarPDF);
        panel.add(Box.createVerticalStrut(6));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(14));
        panel.add(info);
        panel.add(Box.createVerticalGlue());
        panel.add(nuevaSeparador());
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblFecha);
        panel.add(Box.createVerticalStrut(3));
        panel.add(lblTiempo);

        return panel;
    }

    // ── KPIs ──────────────────────────────────────────────────────────────────

    private JPanel crearPanelKPIs() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 8, 0));
        panel.setBackground(C_FONDO);
        panel.setPreferredSize(new Dimension(0, 88));

        lblVentasPredichas  = crearTarjetaKPI(panel, "Ventas Próx. Mes",   "—", C_VERDE);
        lblComprasPredichas = crearTarjetaKPI(panel, "Compras Próx. Mes",  "—", C_SECUNDARIO);
        lblUtilidad         = crearTarjetaKPI(panel, "Utilidad Proyectada","—", C_PRIMARIO);
        lblTendencia        = crearTarjetaKPI(panel, "Tendencia",           "—", C_AMARILLO.darker());
        lblConfianza        = crearTarjetaKPI(panel, "Confianza Modelo",    "—", new Color(100,116,139));

        return panel;
    }

    private JLabel crearTarjetaKPI(JPanel parent, String titulo, String valor, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, color, 0, getHeight(), color.darker()));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(F_KPI_LBL);
        lblTitulo.setForeground(new Color(255, 255, 255, 200));

        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(F_KPI_VAL);
        lblValor.setForeground(C_BLANCO);

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor,  BorderLayout.CENTER);
        parent.add(card);
        return lblValor;
    }

    // ── Gráfica ───────────────────────────────────────────────────────────────

    private JPanel crearPanelGrafica() {
        panelGrafica = new PanelGrafica();
        JPanel wrapper = crearPanelConBorde(" Gráfica de Tendencia Histórica y Predicción ");
        wrapper.add(panelGrafica, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Tabla de historial ────────────────────────────────────────────────────

    private JPanel crearPanelTabla() {
        String[] columnas = {
            "Mes", "Período", "Total Compras ($)", "Total Ventas ($)",
            "Utilidad ($)", "Var. Ventas (%)"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setRowHeight(24);
        tablaHistorial.setFont(F_NORMAL);
        tablaHistorial.setSelectionBackground(new Color(219, 234, 254));
        tablaHistorial.setSelectionForeground(C_PRIMARIO);
        tablaHistorial.setGridColor(C_BORDE);
        tablaHistorial.setShowGrid(true);
        tablaHistorial.setIntercellSpacing(new Dimension(0, 1));

        // Header con estilo
        JTableHeader header = tablaHistorial.getTableHeader();
        header.setFont(F_LABEL);
        header.setBackground(C_PRIMARIO);
        header.setForeground(C_BLANCO);
        header.setPreferredSize(new Dimension(0, 28));

        // Renderer para colores alternos y utilidad roja/verde
        tablaHistorial.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? C_FILA_PAR : C_FILA_IMPAR);
                    setForeground(Color.DARK_GRAY);
                }
                // Última fila = predicción destacada
                if (row == t.getRowCount() - 1) {
                    setBackground(new Color(239, 246, 255));
                    setFont(F_LABEL);
                    setForeground(C_PRIMARIO);
                } else {
                    setFont(F_NORMAL);
                }
                // Columna utilidad: verde/rojo
                if (col == 4 && val != null && !sel) {
                    String s = val.toString().replace(",", "").replace("$", "");
                    try {
                        double d = Double.parseDouble(s);
                        setForeground(d >= 0 ? C_VERDE : C_ROJO);
                    } catch (NumberFormatException ignored) {}
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tablaHistorial);
        scroll.setPreferredSize(new Dimension(0, 165));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel panel = crearPanelConBorde(" Historial Financiero Mensual ");
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── API para el Controlador ───────────────────────────────────────────────

    /** Puebla el selector de empresas. */
    public void poblarSelectorEmpresas(List<EmpresaItem> empresas) {
        cmbEmpresa.removeAllItems();
        for (EmpresaItem emp : empresas) {
            cmbEmpresa.addItem(emp);
        }
    }

    /** Muestra el resultado completo del análisis en la UI. */
    public void mostrarResultados(ResultadoPrediccion res) {
        // KPIs
        lblVentasPredichas .setText(String.format("$%,.2f", res.getVentasPredichas()));
        lblComprasPredichas.setText(String.format("$%,.2f", res.getComprasPredichas()));
        lblUtilidad        .setText(String.format("$%,.2f", res.getUtilidadProyectada()));

        String tendStr = res.getClasificacionTendencia() +
                String.format(" (%+.1f%%)", res.getTendenciaPorcentaje());
        lblTendencia.setText(tendStr);
        lblConfianza.setText(res.getNivelConfianza());

        // Meta info
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFecha .setText("Fecha: " + res.getFechaAnalisis().format(fmt));
        lblTiempo.setText("Entreno: " + res.getTiempoEntrenamientoMs() + " ms");

        // Tabla
        modeloTabla.setRowCount(0);
        List<RegistroFinanciero> hist = res.getHistorial();
        for (int i = 0; i < hist.size(); i++) {
            RegistroFinanciero r = hist.get(i);
            double utilMes = r.getTotalVentas() - r.getTotalCompras();
            String varStr  = "—";
            if (i > 0) {
                double varPorc = ((r.getTotalVentas() - hist.get(i - 1).getTotalVentas())
                        / hist.get(i - 1).getTotalVentas()) * 100.0;
                varStr = String.format("%+.1f%%", varPorc);
            }
            modeloTabla.addRow(new Object[]{
                r.getNombreMes(),
                r.getMesNumero() + "/" + r.getAnio(),
                String.format("%,.2f", r.getTotalCompras()),
                String.format("%,.2f", r.getTotalVentas()),
                String.format("%,.2f", utilMes),
                varStr
            });
        }

        // Fila de predicción
        modeloTabla.addRow(new Object[]{
            "▶ PRED.",
            "Próx. mes",
            String.format("%,.2f", res.getComprasPredichas()),
            String.format("%,.2f", res.getVentasPredichas()),
            String.format("%,.2f", res.getUtilidadProyectada()),
            String.format("%+.1f%%", res.getTendenciaPorcentaje())
        });

        panelGrafica.actualizar(res);
        btnExportarPDF.setEnabled(true);
    }

    /** Limpia todos los componentes de resultado. */
    public void limpiarResultados() {
        lblVentasPredichas .setText("—");
        lblComprasPredichas.setText("—");
        lblUtilidad        .setText("—");
        lblTendencia       .setText("—");
        lblConfianza       .setText("—");
        lblFecha           .setText("Fecha: —");
        lblTiempo          .setText("Entrenamiento: —");
        modeloTabla.setRowCount(0);
        panelGrafica.actualizar(null);
        btnExportarPDF.setEnabled(false);
    }

    /** Activa o desactiva la animación de carga. */
    public void setEstadoCargando(boolean cargando) {
        progressBar .setVisible(cargando);
        btnAnalizar .setEnabled(!cargando);
        cmbEmpresa  .setEnabled(!cargando);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    private JButton crearBoton(String texto, Color fondo, Color foreground) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled()) {
                    Color hover = getModel().isRollover() ? fondo.brighter() : fondo;
                    g2.setPaint(new GradientPaint(0, 0, hover.brighter(), 0, getHeight(), hover));
                } else {
                    g2.setColor(new Color(200, 210, 220));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        btn.setForeground(foreground);
        btn.setFont(F_LABEL);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        return btn;
    }

    private JLabel nuevaEtiqueta(String texto, Font font, Color color) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel crearPanelConBorde(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_PANEL);
        panel.setBorder(new TitledBorder(
                BorderFactory.createLineBorder(C_BORDE),
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                F_LABEL, C_PRIMARIO));
        return panel;
    }

    private JSeparator nuevaSeparador() {
        JSeparator sep = new JSeparator();
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(C_BORDE);
        return sep;
    }

    // ── Ícono circular decorativo ─────────────────────────────────────────────

    private static class CircleIcon implements Icon {
        private final int size;
        private final Color color;
        CircleIcon(int size, Color color) { this.size = size; this.color = color; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(x, y, color.brighter(), x + size, y + size, color));
            g2.fillOval(x, y, size, size);
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
    }
}
