package presentacion;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

final class EstiloInterfaz {
    static final Color FONDO = new Color(240, 245, 249);
    static final Color TINTA = new Color(25, 47, 64);
    static final Color SECUNDARIO = new Color(69, 92, 107);
    static final Color BORDE = new Color(202, 217, 225);
    private static final Color MARCA = new Color(0, 105, 97);
    private static final Color SELECCION = new Color(217, 239, 234);
    private static final Color FILA_ALTERNA = new Color(246, 250, 252);
    private static final String FUENTE = "Segoe UI";

    private EstiloInterfaz() {}

    static void tamanoTableta(Window ventana) {
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int anchoDisponible = Math.max(1, pantalla.width - 32);
        int altoDisponible = Math.max(1, pantalla.height - 48);
        int ancho = Math.min(1000, anchoDisponible);
        int alto = Math.min(720, altoDisponible);
        ventana.setMinimumSize(new Dimension(Math.min(640, ancho), Math.min(480, alto)));
        ventana.setSize(ancho, alto);
        ventana.setLocationRelativeTo(null);
    }

    static Font fuente(int estilo, int tamano) {
        return new Font(FUENTE, estilo, tamano);
    }

    static void instalarTipografia() {
        UIDefaults valores = UIManager.getLookAndFeelDefaults();
        for (Object clave : valores.keySet().toArray()) {
            if (clave.toString().endsWith(".font") && valores.get(clave) instanceof Font actual) {
                UIManager.put(clave, new javax.swing.plaf.FontUIResource(fuente(actual.getStyle(), actual.getSize())));
            }
        }
    }

    static JPanel encabezado(String texto, String detalle) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 3));
        panel.setBackground(TINTA);
        panel.setBorder(new EmptyBorder(17, 25, 18, 25));
        JLabel titulo = new JLabel(texto);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(fuente(Font.BOLD, 25));
        panel.add(titulo);
        if (!detalle.isBlank()) {
            JLabel linea = new JLabel(detalle);
            linea.setForeground(new Color(194, 219, 229));
            linea.setFont(fuente(Font.PLAIN, 13));
            panel.add(linea);
        }
        return panel;
    }

    static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.BOLD, 19));
        etiqueta.setForeground(TINTA);
        return etiqueta;
    }

    static JLabel nota(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente(Font.PLAIN, 13));
        etiqueta.setForeground(SECUNDARIO);
        return etiqueta;
    }

    static JPanel tarjeta(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE), new EmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    static JPanel filaBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    static JButton boton(String texto, boolean principal) {
        JButton boton = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D copia = (Graphics2D) g.create();
                copia.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color relleno = Color.WHITE;
                Color borde = BORDE;
                if (!isEnabled()) {
                    relleno = FONDO;
                } else if (principal) {
                    relleno = getModel().isPressed() ? MARCA.darker()
                            : getModel().isRollover() ? new Color(0, 120, 110) : MARCA;
                    borde = relleno;
                } else if (getModel().isRollover()) {
                    relleno = SELECCION;
                }
                copia.setColor(relleno);
                copia.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                copia.setColor(borde);
                copia.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                if (hasFocus()) {
                    copia.setColor(principal ? Color.WHITE : MARCA);
                    copia.drawRoundRect(3, 3, getWidth() - 7, getHeight() - 7, 9, 9);
                }
                copia.dispose();
                super.paintComponent(g);
            }
        };
        boton.setForeground(principal ? Color.WHITE : TINTA);
        boton.setFont(fuente(Font.BOLD, 13));
        boton.setContentAreaFilled(false);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(10, 16, 10, 16));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    static JPanel campo(String texto, JComponent control) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(TINTA);
        etiqueta.setFont(fuente(Font.BOLD, 13));
        etiqueta.setLabelFor(control);
        control.setFont(fuente(Font.PLAIN, 14));
        control.setBackground(Color.WHITE);
        control.setForeground(TINTA);
        Dimension tamano = control.getPreferredSize();
        control.setPreferredSize(new Dimension(tamano.width, 34));
        if (control instanceof JTextField entrada) {
            entrada.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE), new EmptyBorder(3, 8, 3, 8)));
        }
        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    static JScrollPane tabla(JTable tabla) {
        tabla.setFont(fuente(Font.PLAIN, 14));
        tabla.setForeground(TINTA);
        tabla.setRowHeight(35);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(SELECCION);
        tabla.setSelectionForeground(TINTA);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(fuente(Font.BOLD, 13));
        tabla.getTableHeader().setForeground(TINTA);
        tabla.getTableHeader().setBackground(new Color(224, 235, 241));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer filas = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean seleccionada, boolean foco, int fila, int columna) {
                super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                setBorder(new EmptyBorder(0, 9, 0, 9));
                setBackground(seleccionada ? SELECCION : fila % 2 == 0 ? Color.WHITE : FILA_ALTERNA);
                setForeground(TINTA);
                return this;
            }
        };
        tabla.setDefaultRenderer(Object.class, filas);
        tabla.setDefaultRenderer(Number.class, filas);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }
}
