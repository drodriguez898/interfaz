package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MisHogaresForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_ALERT_BG = new Color(254, 243, 230);
    private static final Color COLOR_ALERT_BORDER = new Color(245, 180, 110);
    private static final Color COLOR_ALERT_TEXT = new Color(180, 90, 10);
    private static final Color COLOR_GREEN = new Color(40, 140, 70);

    private JPanel panelHogares;
    private JLabel lblSincronizacion;
    private List<String[]> hogares = new ArrayList<>();

    public MisHogaresForm() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Mis hogares");
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblTitulo = new JLabel("Mis hogares");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblEncuestador = new JLabel("Encuestador: Duvan Arley · Cuadernillo 2 urbano");
        lblEncuestador.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEncuestador.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(lblEncuestador);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Alerta de sincronización
        JPanel panelAlerta = new JPanel(new BorderLayout());
        panelAlerta.setBackground(COLOR_ALERT_BG);
        panelAlerta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_ALERT_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        panelAlerta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        lblSincronizacion = new JLabel("Sin conexión · 0 registros por sincronizar");
        lblSincronizacion.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSincronizacion.setForeground(COLOR_ALERT_TEXT);
        panelAlerta.add(lblSincronizacion, BorderLayout.WEST);

        bodyPanel.add(panelAlerta);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Lista de tarjetas
        panelHogares = new JPanel();
        panelHogares.setOpaque(false);
        panelHogares.setLayout(new BoxLayout(panelHogares, BoxLayout.Y_AXIS));

        bodyPanel.add(panelHogares);

        // Botón "+ Registrar hogar"
        JButton btnRegistrarHogar = new RoundedButton("+ Registrar hogar", Color.WHITE, COLOR_HEADER);
        btnRegistrarHogar.addActionListener(e -> {
            RegistroHogarForm paso1 = new RegistroHogarForm(this);
            paso1.setVisible(true);
            this.setVisible(false);
        });

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(btnRegistrarHogar);

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void registrarNuevoHogar(String familia, String direccion, String municipio) {
        String ubicacion = direccion + " · " + municipio;
        agregarTarjetaHogar(familia, ubicacion, "Día 1 de 14", 1, 14, "Al día", COLOR_GREEN);
        
        hogares.add(new String[]{familia, ubicacion});
        int totalSinSincronizar = hogares.size();
        lblSincronizacion.setText("Sin conexión · " + totalSinSincronizar + " registro" + (totalSinSincronizar > 1 ? "s" : "") + " por sincronizar");
        
        panelHogares.revalidate();
        panelHogares.repaint();
    }

    private void agregarTarjetaHogar(String nombreFamilia, String direccion, String diaTexto, int progreso, int maxProgreso, String estadoTexto, Color estadoColor) {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblNombre = new JLabel(nombreFamilia);
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setForeground(new Color(30, 41, 55));

        JLabel lblDia = new JLabel(diaTexto);
        lblDia.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDia.setForeground(new Color(120, 130, 145));

        topRow.add(lblNombre, BorderLayout.WEST);
        topRow.add(lblDia, BorderLayout.EAST);

        JLabel lblDir = new JLabel(direccion);
        lblDir.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDir.setForeground(new Color(110, 120, 135));

        JProgressBar bar = new JProgressBar(0, maxProgreso);
        bar.setValue(progreso);
        bar.setPreferredSize(new Dimension(0, 5));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        bar.setForeground(COLOR_HEADER);
        bar.setBackground(new Color(225, 230, 238));
        bar.setBorderPainted(false);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        JLabel lblEstado = new JLabel(estadoTexto);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblEstado.setForeground(estadoColor);

        JButton btnAbrir = new JButton("Abrir");
        btnAbrir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAbrir.setForeground(COLOR_HEADER);
        btnAbrir.setBackground(Color.WHITE);
        btnAbrir.setBorder(new LineBorder(COLOR_HEADER, 1, true));
        btnAbrir.setFocusPainted(false);
        btnAbrir.setPreferredSize(new Dimension(70, 26));
        btnAbrir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAbrir.addActionListener(e -> {
            DetalleHogarForm detalle = new DetalleHogarForm(nombreFamilia, direccion, this);
            detalle.setVisible(true);
            this.setVisible(false);
        });

        bottomRow.add(lblEstado, BorderLayout.WEST);
        bottomRow.add(btnAbrir, BorderLayout.EAST);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(topRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        centerPanel.add(lblDir);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        centerPanel.add(bar);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        centerPanel.add(bottomRow);

        card.add(centerPanel, BorderLayout.CENTER);

        panelHogares.add(card);
        panelHogares.add(Box.createRigidArea(new Dimension(0, 8)));
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
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setForeground(textColor);
            setPreferredSize(new Dimension(0, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR_HEADER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MisHogaresForm().setVisible(true));
    }
}