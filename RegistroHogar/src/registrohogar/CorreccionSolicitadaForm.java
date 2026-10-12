package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CorreccionSolicitadaForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);

    private static final Color COLOR_SUPERVISOR_BG = new Color(254, 243, 230);
    private static final Color COLOR_SUPERVISOR_BORDER = new Color(225, 140, 60);
    private static final Color COLOR_SUPERVISOR_TEXT = new Color(180, 85, 15);

    private String nombreHogar;
    private String nombreSupervisor = "Carlos Mendoza";
    private DetalleHogarForm detalleHogarForm;
    private List<ObservacionItem> listaObservaciones = new ArrayList<>();
    
    private JButton btnRetransmitir;

    public static class ObservacionItem {
        int dia;
        String articulo;
        String observacion;

        public ObservacionItem(int dia, String articulo, String observacion) {
            this.dia = dia;
            this.articulo = articulo;
            this.observacion = observacion;
        }
    }

    public CorreccionSolicitadaForm(String nombreHogar, DetalleHogarForm detalleHogarForm, List<ObservacionItem> observaciones) {
        this.nombreHogar = nombreHogar;
        this.detalleHogarForm = detalleHogarForm;
        if (observaciones != null) {
            this.listaObservaciones = observaciones;
        }
        initComponents();
    }

    private void initComponents() {
        setTitle("Corrección solicitada");
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
        headerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitulo = new JLabel("Corrección solicitada");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar + " · Caso en corrección");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Banner Supervisor
        JPanel bannerSupervisor = new JPanel();
        bannerSupervisor.setBackground(COLOR_SUPERVISOR_BG);
        bannerSupervisor.setLayout(new BoxLayout(bannerSupervisor, BoxLayout.Y_AXIS));
        bannerSupervisor.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_SUPERVISOR_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblSupTit = new JLabel("Solicitud del supervisor " + nombreSupervisor);
        lblSupTit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSupTit.setForeground(COLOR_SUPERVISOR_TEXT);

        JLabel lblSupSub = new JLabel(listaObservaciones.size() + " observación" + (listaObservaciones.size() != 1 ? "es" : "") + " por corregir");
        lblSupSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSupSub.setForeground(COLOR_SUPERVISOR_TEXT);

        bannerSupervisor.add(lblSupTit);
        bannerSupervisor.add(Box.createRigidArea(new Dimension(0, 2)));
        bannerSupervisor.add(lblSupSub);

        bodyPanel.add(bannerSupervisor);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Lista de observaciones
        for (int i = 0; i < listaObservaciones.size(); i++) {
            ObservacionItem obs = listaObservaciones.get(i);
            int indexObs = i;
            JPanel cardObs = crearTarjetaObservacion(obs, indexObs);
            bodyPanel.add(cardObs);
            bodyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Botón Retransmitir
        btnRetransmitir = new RoundedButton("Retransmitir a revisión", new Color(150, 160, 172), Color.WHITE);
        btnRetransmitir.setEnabled(listaObservaciones.isEmpty());

        btnRetransmitir.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "¡Caso retransmitido con éxito!", "Retransmitido", JOptionPane.INFORMATION_MESSAGE);
            MisHogaresForm mainHogares = new MisHogaresForm();
            mainHogares.setVisible(true);
            if (detalleHogarForm != null) {
                detalleHogarForm.dispose();
            }
            this.dispose();
        });

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        bodyPanel.add(btnRetransmitir);

        JLabel lblNota = new JLabel("Corrige todas las observaciones para retransmitir.");
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

    private JPanel crearTarjetaObservacion(ObservacionItem obs, int index) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel lblTit = new JLabel("Día " + obs.dia + " · " + obs.articulo);
        lblTit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTit.setForeground(COLOR_TEXT_DARK);

        JLabel lblObs = new JLabel("<html><body style='width: 170px;'>Observación: " + obs.observacion + "</body></html>");
        lblObs.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblObs.setForeground(COLOR_TEXT_MUTED);

        leftPanel.add(lblTit);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftPanel.add(lblObs);

        JButton btnCorregir = new JButton("Corregir");
        btnCorregir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCorregir.setForeground(COLOR_HEADER);
        btnCorregir.setBackground(Color.WHITE);
        btnCorregir.setFocusPainted(false);
        btnCorregir.setBorder(new LineBorder(COLOR_HEADER, 1, true));
        btnCorregir.setPreferredSize(new Dimension(80, 32));
        btnCorregir.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCorregir.addActionListener(e -> {
            GastosDiaForm formGastos = new GastosDiaForm(nombreHogar, obs.dia, detalleHogarForm);
            formGastos.setVisible(true);
            this.dispose();
        });

        card.add(leftPanel, BorderLayout.CENTER);
        card.add(btnCorregir, BorderLayout.EAST);

        return card;
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
            g2.setColor(isEnabled() ? COLOR_HEADER : bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? COLOR_HEADER : bgColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }
}