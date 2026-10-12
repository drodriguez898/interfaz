package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TransmitirCasoForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);
    private static final Color COLOR_CHECK = new Color(40, 150, 70);
    
    private static final Color COLOR_CIRCLE_BG = new Color(220, 245, 230);
    private static final Color COLOR_CIRCLE_BORDER = new Color(40, 150, 70);
    private static final Color COLOR_BADGE_BG = new Color(254, 243, 225);
    private static final Color COLOR_BADGE_TEXT = new Color(180, 90, 10);

    private String nombreHogar;
    private DetalleHogarForm detalleHogarForm;

    private JPanel mainPanel;
    private JLabel lblHeaderTitulo;
    private List<CorreccionSolicitadaForm.ObservacionItem> listaErrores = new ArrayList<>();

    public TransmitirCasoForm(String nombreHogar, DetalleHogarForm detalleHogarForm) {
        this.nombreHogar = nombreHogar;
        this.detalleHogarForm = detalleHogarForm;
        initComponents();
        mostrarVistaVerificacion();
    }

    public void setListaErrores(List<CorreccionSolicitadaForm.ObservacionItem> errores) {
        this.listaErrores = errores;
    }

    private void initComponents() {
        setTitle("Finalizar y transmitir");
        setSize(360, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        lblHeaderTitulo = new JLabel("Finalizar y transmitir");
        lblHeaderTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeaderTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblHeaderTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        add(mainPanel);
    }

    private void mostrarVistaVerificacion() {
        lblHeaderTitulo.setText("Finalizar y transmitir");

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel cardVerificacion = new JPanel();
        cardVerificacion.setBackground(COLOR_CARD_BG);
        cardVerificacion.setLayout(new BoxLayout(cardVerificacion, BoxLayout.Y_AXIS));
        cardVerificacion.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lblVerificacionTit = new JLabel("Verificación del caso");
        lblVerificacionTit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblVerificacionTit.setForeground(COLOR_TEXT_DARK);

        cardVerificacion.add(lblVerificacionTit);
        cardVerificacion.add(Box.createRigidArea(new Dimension(0, 12)));

        agregarFilaVerificacion(cardVerificacion, "Datos del hogar");
        agregarFilaVerificacion(cardVerificacion, "Integrantes mayores de 10 años");
        agregarFilaVerificacion(cardVerificacion, "Visitas registradas (3)");
        agregarFilaVerificacion(cardVerificacion, "Gastos diarios: 14 de 14 días");
        agregarFilaVerificacion(cardVerificacion, "Gastos incompletos: 0");
        agregarFilaVerificacion(cardVerificacion, "COICOP asignada a cada gasto");
        agregarFilaVerificacion(cardVerificacion, "Frecuencia y tipo de gasto definidos");

        bodyPanel.add(cardVerificacion);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnTransmitir = new RoundedButton("Transmitir a revisión", COLOR_HEADER, Color.WHITE);
        JButton btnVolver = new RoundedButton("Volver", Color.WHITE, COLOR_HEADER);

        btnTransmitir.addActionListener(e -> mostrarVistaTransmitido());

        btnVolver.addActionListener(e -> {
            if (detalleHogarForm != null) {
                detalleHogarForm.setVisible(true);
            }
            this.dispose();
        });

        bodyPanel.add(btnTransmitir);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(btnVolver);

        JLabel lblNota = new JLabel("Después de transmitir, el supervisor revisará el caso.");
        lblNota.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblNota.setForeground(COLOR_TEXT_MUTED);
        lblNota.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(lblNota);

        JScrollPane scroll = new JScrollPane(bodyPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        BorderLayout layout = (BorderLayout) mainPanel.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp != null) {
            mainPanel.remove(centerComp);
        }

        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void mostrarVistaTransmitido() {
        lblHeaderTitulo.setText("Caso transmitido");

        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(30, 20, 20, 20));

        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CIRCLE_BG);
                g2.fillOval(2, 2, getWidth() - 5, getHeight() - 5);
                g2.setColor(COLOR_CIRCLE_BORDER);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(2, 2, getWidth() - 5, getHeight() - 5);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        circlePanel.setOpaque(false);
        circlePanel.setPreferredSize(new Dimension(90, 90));
        circlePanel.setMaximumSize(new Dimension(90, 90));
        circlePanel.setLayout(new GridBagLayout());

        JLabel lblCheckLarge = new JLabel("✓");
        lblCheckLarge.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblCheckLarge.setForeground(COLOR_CIRCLE_BORDER);
        circlePanel.add(lblCheckLarge);

        circlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyPanel.add(circlePanel);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblTituloRevision = new JLabel("Caso en revisión");
        lblTituloRevision.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTituloRevision.setForeground(COLOR_TEXT_DARK);
        lblTituloRevision.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubText1 = new JLabel("El supervisor revisará la información.");
        lblSubText1.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubText1.setForeground(COLOR_TEXT_MUTED);
        lblSubText1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubText2 = new JLabel("Te avisaremos si hay correcciones.");
        lblSubText2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubText2.setForeground(COLOR_TEXT_MUTED);
        lblSubText2.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyPanel.add(lblTituloRevision);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        bodyPanel.add(lblSubText1);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        bodyPanel.add(lblSubText2);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblBadge = new JLabel(" Estado: En revisión ");
        lblBadge.setOpaque(true);
        lblBadge.setBackground(COLOR_BADGE_BG);
        lblBadge.setForeground(COLOR_BADGE_TEXT);
        lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadge.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(245, 210, 160), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        lblBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        bodyPanel.add(lblBadge);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        JButton btnVolverHogares = new RoundedButton("Volver a Mis hogares", COLOR_HEADER, Color.WHITE);
        
        btnVolverHogares.addActionListener(e -> {
            // Si hay errores, abre la pantalla de Corrección solicitada (Foto 1)
            if (listaErrores != null && !listaErrores.isEmpty()) {
                CorreccionSolicitadaForm formCorr = new CorreccionSolicitadaForm(nombreHogar, detalleHogarForm, listaErrores);
                formCorr.setVisible(true);
            } else {
                // Si NO hay errores, directamente a Mis hogares
                MisHogaresForm mainHogares = new MisHogaresForm();
                mainHogares.setVisible(true);
                if (detalleHogarForm != null) {
                    detalleHogarForm.dispose();
                }
            }
            this.dispose();
        });

        bodyPanel.add(btnVolverHogares);

        BorderLayout layout = (BorderLayout) mainPanel.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);
        if (centerComp != null) {
            mainPanel.remove(centerComp);
        }

        mainPanel.add(bodyPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void agregarFilaVerificacion(JPanel container, String titulo) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTit.setForeground(COLOR_TEXT_DARK);

        JLabel lblCheck = new JLabel("✓");
        lblCheck.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCheck.setForeground(COLOR_CHECK);

        row.add(lblTit, BorderLayout.WEST);
        row.add(lblCheck, BorderLayout.EAST);

        container.add(row);
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
            setPreferredSize(new Dimension(0, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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