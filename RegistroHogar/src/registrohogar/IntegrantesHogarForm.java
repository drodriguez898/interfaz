package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IntegrantesHogarForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_ALERT_BG = new Color(254, 243, 230);
    private static final Color COLOR_ALERT_BORDER = new Color(245, 180, 110);
    private static final Color COLOR_ALERT_TEXT = new Color(180, 90, 10);
    private static final Color COLOR_TAG_BG = new Color(225, 245, 235);
    private static final Color COLOR_TAG_TEXT = new Color(30, 120, 60);

    private String nombreFamilia;
    private String direccion;
    private String municipio;
    private int totalPersonas;
    private MisHogaresForm menuPrincipal;
    private List<String[]> integrantesList = new ArrayList<>();

    private JPanel panelTarjetas;
    private JLabel lblContador;
    private JProgressBar progressBar;
    private JPanel panelAlerta;
    private JLabel lblAlerta;
    private JButton btnAgregar;
    private JButton btnGuardarHogar;

    public IntegrantesHogarForm() {
        this("Familia Gómez", "4", "Calle 14 #34-12", "Bucaramanga", null);
    }

    public IntegrantesHogarForm(String nombreFamilia, String totalPersonasStr, String direccion, String municipio, MisHogaresForm menuPrincipal) {
        this.nombreFamilia = nombreFamilia.isEmpty() ? "Familia Gómez" : nombreFamilia;
        this.direccion = direccion;
        this.municipio = municipio;
        this.menuPrincipal = menuPrincipal;
        try {
            this.totalPersonas = Integer.parseInt(totalPersonasStr.trim());
        } catch (Exception e) {
            this.totalPersonas = 4;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Integrantes del hogar");
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel lblTitulo = new JLabel("Integrantes del hogar");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreFamilia + " · Paso 2 de 2");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        lblContador = new JLabel("0 de " + totalPersonas + " personas registradas");
        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContador.setForeground(new Color(30, 41, 55));

        progressBar = new JProgressBar(0, totalPersonas);
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(0, 7));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
        progressBar.setForeground(COLOR_HEADER);
        progressBar.setBackground(new Color(220, 225, 232));
        progressBar.setBorderPainted(false);

        panelTarjetas = new JPanel();
        panelTarjetas.setOpaque(false);
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.Y_AXIS));

        panelAlerta = new JPanel(new BorderLayout());
        panelAlerta.setBackground(COLOR_ALERT_BG);
        panelAlerta.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_ALERT_BORDER, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        panelAlerta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        lblAlerta = new JLabel("Falta " + totalPersonas + " personas por registrar");
        lblAlerta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAlerta.setForeground(COLOR_ALERT_TEXT);
        panelAlerta.add(lblAlerta, BorderLayout.WEST);

        btnAgregar = new RoundedButton("Agregar integrante", COLOR_HEADER, Color.WHITE);
        btnGuardarHogar = new RoundedButton("Guardar hogar", new Color(150, 160, 172), Color.WHITE);
        btnGuardarHogar.setEnabled(false);

        btnAgregar.addActionListener(e -> {
            AgregarIntegranteForm form = new AgregarIntegranteForm(this);
            form.setVisible(true);
        });

        btnGuardarHogar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "¡Registro del hogar finalizado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            if (menuPrincipal != null) {
                menuPrincipal.registrarNuevoHogar(nombreFamilia, direccion, municipio);
                menuPrincipal.setVisible(true);
            }
            this.dispose();
        });

        bodyPanel.add(lblContador);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(progressBar);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        bodyPanel.add(panelTarjetas);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(panelAlerta);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        bodyPanel.add(btnAgregar);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(btnGuardarHogar);

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    public void agregarPersona(String nombre, String edad, String sexo, String parentesco, boolean esResponsable) {
        integrantesList.add(new String[]{nombre, edad, sexo, parentesco, esResponsable ? "Sí" : "No"});

        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lblNombreCard = new JLabel(nombre);
        lblNombreCard.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombreCard.setForeground(new Color(30, 41, 55));

        JLabel lblDetalles = new JLabel(edad + " años · " + sexo + " · " + parentesco);
        lblDetalles.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDetalles.setForeground(new Color(110, 120, 135));

        card.add(lblNombreCard);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(lblDetalles);

        if (esResponsable) {
            JLabel tagGasto = new JLabel(" Responsable de gastos ");
            tagGasto.setOpaque(true);
            tagGasto.setBackground(COLOR_TAG_BG);
            tagGasto.setForeground(COLOR_TAG_TEXT);
            tagGasto.setFont(new Font("Segoe UI", Font.BOLD, 10));
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(tagGasto);
        }

        panelTarjetas.add(card);
        panelTarjetas.add(Box.createRigidArea(new Dimension(0, 6)));

        int actuales = integrantesList.size();
        int faltan = totalPersonas - actuales;

        lblContador.setText(actuales + " de " + totalPersonas + " personas registradas");
        progressBar.setValue(actuales);

        if (faltan > 0) {
            lblAlerta.setText("Falta " + faltan + " persona" + (faltan > 1 ? "s" : "") + " por registrar");
        } else {
            panelAlerta.setVisible(false);
            btnAgregar.setEnabled(false);
            btnGuardarHogar.setEnabled(true);
            ((RoundedButton) btnGuardarHogar).setBgColor(COLOR_HEADER);
            btnGuardarHogar.repaint();
        }

        panelTarjetas.revalidate();
        panelTarjetas.repaint();
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

        public void setBgColor(Color bg) {
            this.bgColor = bg;
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
            g2.setColor(bgColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IntegrantesHogarForm().setVisible(true));
    }
}