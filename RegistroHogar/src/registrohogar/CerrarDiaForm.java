package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CerrarDiaForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);

    private static final Color COLOR_ALERT_BG = new Color(225, 245, 235);
    private static final Color COLOR_ALERT_BORDER = new Color(160, 215, 185);
    private static final Color COLOR_ALERT_TEXT = new Color(35, 110, 65);
    private static final Color COLOR_CHECK = new Color(40, 150, 70);

    private String nombreHogar;
    private int dia;
    private DetalleHogarForm detalleHogarForm;

    public CerrarDiaForm(String nombreHogar, int dia, DetalleHogarForm detalleHogarForm) {
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.detalleHogarForm = detalleHogarForm;
        initComponents();
    }

    private void initComponents() {
        setTitle("Cerrar día " + dia);
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitulo = new JLabel("Cerrar día " + dia);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        List<String[]> lista = GastosDiaForm.getListaGastosDelDia(dia);
        int cantHogar = 0;
        int cantPersonal = 0;
        long totalCOP = 0;

        for (String[] g : lista) {
            String tipo = g.length > 7 ? g[7] : "1";
            if (tipo.contains("2")) {
                cantPersonal++;
            } else {
                cantHogar++;
            }
            try { totalCOP += Long.parseLong(g[5]); } catch (Exception e) {}
        }

        int totalGastos = lista.size();

        JPanel cardResumen = new JPanel();
        cardResumen.setBackground(COLOR_CARD_BG);
        cardResumen.setLayout(new BoxLayout(cardResumen, BoxLayout.Y_AXIS));
        cardResumen.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblResumenTit = new JLabel("Resumen del día");
        lblResumenTit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblResumenTit.setForeground(COLOR_TEXT_DARK);

        cardResumen.add(lblResumenTit);
        cardResumen.add(Box.createRigidArea(new Dimension(0, 10)));

        agregarFilaResumen(cardResumen, "Gastos del hogar (1)", String.valueOf(cantHogar));
        agregarFilaResumen(cardResumen, "Gastos personales (2)", String.valueOf(cantPersonal));
        agregarFilaResumen(cardResumen, "Frecuencia de compra completa", String.valueOf(totalGastos));
        agregarFilaResumen(cardResumen, "Gastos incompletos", "0");

        JSeparator sep = new JSeparator();
        sep.setForeground(COLOR_BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        cardResumen.add(Box.createRigidArea(new Dimension(0, 4)));
        cardResumen.add(sep);
        cardResumen.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setOpaque(false);
        JLabel lblTotalTit = new JLabel("Total del día (COP)");
        lblTotalTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalTit.setForeground(COLOR_TEXT_DARK);

        NumberFormat nf = NumberFormat.getInstance(new Locale("es", "CO"));
        JLabel lblTotalVal = new JLabel(nf.format(totalCOP));
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalVal.setForeground(COLOR_TEXT_DARK);

        panelTotal.add(lblTotalTit, BorderLayout.WEST);
        panelTotal.add(lblTotalVal, BorderLayout.EAST);
        cardResumen.add(panelTotal);

        bodyPanel.add(cardResumen);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel panelAlerta = new JPanel();
        panelAlerta.setBackground(COLOR_ALERT_BG);
        panelAlerta.setLayout(new BoxLayout(panelAlerta, BoxLayout.Y_AXIS));
        panelAlerta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_ALERT_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblMsg1 = new JLabel("Todos los gastos están completos.");
        lblMsg1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMsg1.setForeground(COLOR_ALERT_TEXT);

        JLabel lblMsg2 = new JLabel("Puedes cerrar el día.");
        lblMsg2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblMsg2.setForeground(COLOR_ALERT_TEXT);

        panelAlerta.add(lblMsg1);
        panelAlerta.add(Box.createRigidArea(new Dimension(0, 2)));
        panelAlerta.add(lblMsg2);

        bodyPanel.add(panelAlerta);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnConfirmar = new RoundedButton("Confirmar cierre del día " + dia, COLOR_HEADER, Color.WHITE);
        JButton btnCancelar = new RoundedButton("Cancelar", Color.WHITE, COLOR_HEADER);

        btnConfirmar.addActionListener(e -> {
            if (detalleHogarForm != null) {
                detalleHogarForm.marcarDiaComoCerrado(dia);
                detalleHogarForm.setVisible(true);
            }
            this.dispose();
        });

        btnCancelar.addActionListener(e -> {
            if (detalleHogarForm != null) {
                detalleHogarForm.setVisible(true);
            }
            this.dispose();
        });

        bodyPanel.add(btnConfirmar);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(btnCancelar);

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void agregarFilaResumen(JPanel container, String titulo, String valor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTit.setForeground(COLOR_TEXT_MUTED);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblVal.setForeground(COLOR_TEXT_DARK);

        JLabel lblCheck = new JLabel("✓");
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCheck.setForeground(COLOR_CHECK);

        rightPanel.add(lblVal);
        rightPanel.add(lblCheck);

        row.add(lblTit, BorderLayout.WEST);
        row.add(rightPanel, BorderLayout.EAST);

        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 4)));
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
            setPreferredSize(new Dimension(0, 38));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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