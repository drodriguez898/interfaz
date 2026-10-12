package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class DetalleGastoForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);
    private static final Color COLOR_TAG_BG = new Color(220, 245, 230);
    private static final Color COLOR_TAG_TEXT = new Color(35, 130, 70);

    private String nombreHogar;
    private int dia;
    private int indiceGasto;
    private String[] datosGasto;
    private GastosDiaForm ventanaPadre;

    public DetalleGastoForm(GastosDiaForm ventanaPadre, String nombreHogar, int dia, int indiceGasto, String[] datosGasto) {
        this.ventanaPadre = ventanaPadre;
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.indiceGasto = indiceGasto;
        this.datosGasto = datosGasto;
        initComponents();
    }

    private void initComponents() {
        setTitle("Detalle del gasto");
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel("Detalle del gasto");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar + " · Día " + dia);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel cardInfo = new JPanel();
        cardInfo.setBackground(COLOR_CARD_BG);
        cardInfo.setLayout(new BoxLayout(cardInfo, BoxLayout.Y_AXIS));
        cardInfo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lblTag = new JLabel(" Completo ");
        lblTag.setOpaque(true);
        lblTag.setBackground(COLOR_TAG_BG);
        lblTag.setForeground(COLOR_TAG_TEXT);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setBorder(new EmptyBorder(2, 6, 2, 6));

        cardInfo.add(lblTag);
        cardInfo.add(Box.createRigidArea(new Dimension(0, 8)));

        String articulo = datosGasto[0];
        String cantidad = datosGasto[1];
        String unidad = datosGasto[2];
        String adquisicion = datosGasto[3];
        String categoria = datosGasto[4];
        long valorNum = 0;
        try { valorNum = Long.parseLong(datosGasto[5]); } catch (Exception e) {}
        
        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        String valorFormateado = "$ " + nf.format(valorNum);

        String frecuencia = datosGasto.length > 6 ? datosGasto[6] : "Semanal";
        String tipoGasto = datosGasto.length > 7 ? datosGasto[7] : "1 · Gasto del hogar";
        String fecha = "08/10/2026";

        agregarCampoDetalle(cardInfo, "Artículo", articulo);
        agregarCampoDetalle(cardInfo, "Categoría COICOP", categoria);
        agregarCampoDetalle(cardInfo, "Cantidad", cantidad);
        agregarCampoDetalle(cardInfo, "Unidad", unidad);
        agregarCampoDetalle(cardInfo, "Forma de adquisición", adquisicion);
        agregarCampoDetalle(cardInfo, "Valor", valorFormateado);
        agregarCampoDetalle(cardInfo, "Frecuencia de compra", frecuencia);
        agregarCampoDetalle(cardInfo, "Tipo de gasto", tipoGasto);
        agregarCampoDetalle(cardInfo, "Fecha", fecha);

        bodyPanel.add(cardInfo);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        JButton btnEditar = new RoundedButton("Editar gasto", COLOR_HEADER, Color.WHITE);
        JButton btnEliminar = new RoundedButton("Eliminar gasto", Color.WHITE, COLOR_HEADER);

        btnEditar.addActionListener(e -> {
            NuevoGastoForm formEdit = new NuevoGastoForm(nombreHogar, dia, indiceGasto, datosGasto);
            formEdit.setVisible(true);
            this.dispose();
            if (ventanaPadre != null) {
                ventanaPadre.dispose();
            }
        });

        btnEliminar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que deseas eliminar este gasto?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (resp == JOptionPane.YES_OPTION) {
                GastosDiaForm.eliminarGasto(dia, indiceGasto);
                if (ventanaPadre != null) {
                    ventanaPadre.cargarGastos();
                    ventanaPadre.setVisible(true);
                }
                this.dispose();
            }
        });

        bodyPanel.add(btnEditar);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(btnEliminar);

        JLabel lblNota = new JLabel("Se puede editar mientras el caso no esté cerrado.");
        lblNota.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNota.setForeground(COLOR_TEXT_MUTED);
        lblNota.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(lblNota);

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void agregarCampoDetalle(JPanel container, String titulo, String valor) {
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblTit.setForeground(COLOR_TEXT_MUTED);

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVal.setForeground(COLOR_TEXT_DARK);

        container.add(lblTit);
        container.add(Box.createRigidArea(new Dimension(0, 1)));
        container.add(lblVal);
        container.add(Box.createRigidArea(new Dimension(0, 6)));
    }

    private class RoundedButton extends JButton {
        private Color bgColor;
        private Color textColor;

        public RoundedButton(String text, Color bg, Color fg) {
            super(text);
            this.bgColor = bg;
            this.textColor = fg;
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(textColor);
            setPreferredSize(new Dimension(0, 36));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor.equals(Color.WHITE) ? COLOR_HEADER : bgColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }
}