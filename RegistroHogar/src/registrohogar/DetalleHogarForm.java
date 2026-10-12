package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DetalleHogarForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);
    
    private static final Color COLOR_DAY_GRAY = new Color(225, 228, 234);
    private static final Color COLOR_DAY_GRAY_TEXT = new Color(120, 130, 145);

    private String nombreHogar;
    private String direccionHogar;
    private JFrame ventanaPadre;

    private int diaSeleccionado = 1;
    private Set<Integer> diasCerrados = new HashSet<>();
    private List<BotonDia> listaBotonesDias = new ArrayList<>();
    private JButton btnRegistrarGastos;
    private JButton btnTransmitir;
    private JLabel lblGastosSub;
    private JLabel lblInfoTransmitir2;

    public DetalleHogarForm() {
        this("Hogar 45091", "Calle 14 #34-12 · Bucaramanga", null);
    }

    public DetalleHogarForm(String nombreHogar, String direccionHogar, JFrame ventanaPadre) {
        this.nombreHogar = nombreHogar;
        this.direccionHogar = direccionHogar;
        this.ventanaPadre = ventanaPadre;
        initComponents();
    }

    public void marcarDiaComoCerrado(int dia) {
        diasCerrados.add(dia);
        seleccionarDia(dia);
        
        int faltantes = 14 - diasCerrados.size();
        if (lblInfoTransmitir2 != null) {
            lblInfoTransmitir2.setText(faltantes == 0 ? "¡Todos los 14 días están completos!" : "Faltan " + faltantes + " días por completar.");
        }

        // Habilita el botón si los 14 días están completos
        if (diasCerrados.size() >= 14 && btnTransmitir != null) {
            btnTransmitir.setEnabled(true);
        }

        for (BotonDia btn : listaBotonesDias) {
            btn.repaint();
        }
    }

    private void initComponents() {
        setTitle(nombreHogar);
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
        headerPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel lblTitulo = new JLabel(nombreHogar);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblDireccion = new JLabel(direccionHogar);
        lblDireccion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDireccion.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblDireccion);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblCiclo = new JLabel("Ciclo de 14 días");
        lblCiclo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCiclo.setForeground(COLOR_TEXT_DARK);
        lblCiclo.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(lblCiclo);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // Grilla 14 días
        JPanel gridDias = new JPanel(new GridLayout(2, 7, 3, 3));
        gridDias.setOpaque(false);
        gridDias.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        gridDias.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 1; i <= 14; i++) {
            BotonDia btnDia = new BotonDia(i);
            if (i == 1) btnDia.setSeleccionado(true);
            listaBotonesDias.add(btnDia);
            gridDias.add(btnDia);
        }

        bodyPanel.add(gridDias);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel lblLeyenda1 = new JLabel("Oscuro: completo · 3!: pendiente · Borde: hoy");
        lblLeyenda1.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblLeyenda1.setForeground(COLOR_TEXT_MUTED);
        lblLeyenda1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLeyenda2 = new JLabel("Gris: sin iniciar");
        lblLeyenda2.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblLeyenda2.setForeground(COLOR_TEXT_MUTED);
        lblLeyenda2.setAlignmentX(Component.LEFT_ALIGNMENT);

        bodyPanel.add(lblLeyenda1);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        bodyPanel.add(lblLeyenda2);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Card Gastos
        JPanel cardGastos = new JPanel();
        cardGastos.setBackground(COLOR_CARD_BG);
        cardGastos.setLayout(new BoxLayout(cardGastos, BoxLayout.Y_AXIS));
        cardGastos.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        cardGastos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        cardGastos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSeccionD = new JLabel("Sección D · Gastos diarios del hogar");
        lblSeccionD.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSeccionD.setForeground(COLOR_TEXT_DARK);

        lblGastosSub = new JLabel("Día 1 · 0 gastos registrados");
        lblGastosSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblGastosSub.setForeground(COLOR_TEXT_MUTED);

        btnRegistrarGastos = new RoundedButton("Registrar gastos del día 1", COLOR_HEADER, Color.WHITE);
        
        btnRegistrarGastos.addActionListener(e -> {
            if (diasCerrados.contains(diaSeleccionado)) {
                JOptionPane.showMessageDialog(this,
                    "El día " + diaSeleccionado + " ya fue CERRADO.\nNo se pueden modificar ni agregar más gastos a este día.",
                    "Día Cerrado",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                GastosDiaForm formGastos = new GastosDiaForm(nombreHogar, diaSeleccionado, DetalleHogarForm.this);
                formGastos.setVisible(true);
            }
        });

        cardGastos.add(lblSeccionD);
        cardGastos.add(Box.createRigidArea(new Dimension(0, 2)));
        cardGastos.add(lblGastosSub);
        cardGastos.add(Box.createRigidArea(new Dimension(0, 6)));
        cardGastos.add(btnRegistrarGastos);

        bodyPanel.add(cardGastos);
        bodyPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // Card Transmitir
        JPanel cardTransmitir = new JPanel();
        cardTransmitir.setBackground(COLOR_CARD_BG);
        cardTransmitir.setLayout(new BoxLayout(cardTransmitir, BoxLayout.Y_AXIS));
        cardTransmitir.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        cardTransmitir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        cardTransmitir.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFinalizar = new JLabel("Finalizar y transmitir");
        lblFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFinalizar.setForeground(COLOR_TEXT_DARK);

        JLabel lblInfoTransmitir1 = new JLabel("Disponible cuando los 14 días estén completos.");
        lblInfoTransmitir1.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoTransmitir1.setForeground(COLOR_TEXT_MUTED);

        lblInfoTransmitir2 = new JLabel("Faltan 14 días por completar.");
        lblInfoTransmitir2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblInfoTransmitir2.setForeground(COLOR_TEXT_MUTED);

        btnTransmitir = new RoundedButton("Transmitir a revisión", COLOR_HEADER, Color.WHITE);
        
        // Habilitado por defecto para pruebas o según progreso
        btnTransmitir.setEnabled(true); 

        btnTransmitir.addActionListener(e -> {
            TransmitirCasoForm formTransmitir = new TransmitirCasoForm(nombreHogar, DetalleHogarForm.this);
            formTransmitir.setVisible(true);
            this.setVisible(false);
        });

        cardTransmitir.add(lblFinalizar);
        cardTransmitir.add(Box.createRigidArea(new Dimension(0, 2)));
        cardTransmitir.add(lblInfoTransmitir1);
        cardTransmitir.add(lblInfoTransmitir2);
        cardTransmitir.add(Box.createRigidArea(new Dimension(0, 6)));
        cardTransmitir.add(btnTransmitir);

        bodyPanel.add(cardTransmitir);

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Navegación
        JPanel bottomNav = new JPanel(new GridLayout(1, 3));
        bottomNav.setBackground(Color.WHITE);
        bottomNav.setPreferredSize(new Dimension(0, 42));
        bottomNav.setBorder(new LineBorder(COLOR_BORDER, 1));

        JButton btnNavHogares = createNavButton("Hogares", true);
        JButton btnNavSincronizar = createNavButton("Sincronizar", false);
        JButton btnNavPerfil = createNavButton("Perfil", false);

        btnNavHogares.addActionListener(e -> {
            if (ventanaPadre != null) {
                ventanaPadre.setVisible(true);
            }
            this.dispose();
        });

        bottomNav.add(btnNavHogares);
        bottomNav.add(btnNavSincronizar);
        bottomNav.add(btnNavPerfil);

        mainPanel.add(bottomNav, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void seleccionarDia(int dia) {
        this.diaSeleccionado = dia;
        for (BotonDia btn : listaBotonesDias) {
            btn.setSeleccionado(btn.getDia() == dia);
        }

        if (diasCerrados.contains(dia)) {
            lblGastosSub.setText("Día " + dia + " · COMPLETO Y CERRADO");
            btnRegistrarGastos.setText("Día " + dia + " cerrado");
            btnRegistrarGastos.setEnabled(false);
        } else {
            lblGastosSub.setText("Día " + dia + " · Gastos en progreso");
            btnRegistrarGastos.setText("Registrar gastos del día " + dia);
            btnRegistrarGastos.setEnabled(true);
        }
    }

    private class BotonDia extends JButton {
        private int dia;
        private boolean seleccionado = false;

        public BotonDia(int dia) {
            super(String.valueOf(dia));
            this.dia = dia;
            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setMargin(new Insets(0, 0, 0, 0));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addActionListener(e -> seleccionarDia(dia));
        }

        public int getDia() {
            return dia;
        }

        public void setSeleccionado(boolean seleccionado) {
            this.seleccionado = seleccionado;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (diasCerrados.contains(dia)) {
                g2.setColor(COLOR_HEADER);
                setForeground(Color.WHITE);
            } else {
                g2.setColor(COLOR_DAY_GRAY);
                setForeground(COLOR_DAY_GRAY_TEXT);
            }

            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            if (seleccionado) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_HEADER);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 6, 6);
                g2.dispose();
            }
        }
    }

    private JButton createNavButton(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", activo ? Font.BOLD : Font.PLAIN, 11));
        btn.setForeground(activo ? COLOR_TEXT_DARK : COLOR_TEXT_MUTED);
        btn.setBackground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(textColor);
            setPreferredSize(new Dimension(0, 34));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? bgColor : new Color(200, 205, 215));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            super.paintComponent(g2);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isEnabled() ? bgColor : new Color(200, 205, 215));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DetalleHogarForm().setVisible(true));
    }
}