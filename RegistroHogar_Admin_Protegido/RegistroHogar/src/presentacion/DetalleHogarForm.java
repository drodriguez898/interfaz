package presentacion;

import negocio.EncuestadoResumen;
import persistencia.CasosDAO;
import persistencia.GastoDAO;
import persistencia.HogarDAO;
import persistencia.SesionActual;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DetalleHogarForm extends JFrame {

    private static final Color COLOR_HEADER = EstiloInterfaz.TINTA;
    private static final Color COLOR_BG = EstiloInterfaz.FONDO;
    private static final Color COLOR_CARD_BG = Color.WHITE;
    private static final Color COLOR_BORDER = new Color(215, 220, 228);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);
    
    private static final Color COLOR_DAY_GRAY = new Color(225, 228, 234);
    private static final Color COLOR_DAY_GRAY_TEXT = new Color(120, 130, 145);

    private String nombreHogar;
    private String direccionHogar;
    private int idHogar;
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
        this(0,nombreHogar,direccionHogar,ventanaPadre);
    }

    public DetalleHogarForm(int idHogar, String nombreHogar, String direccionHogar, JFrame ventanaPadre) {
        this.idHogar = idHogar;
        this.nombreHogar = nombreHogar;
        this.direccionHogar = direccionHogar;
        this.ventanaPadre = ventanaPadre;
        initComponents();
        if (idHogar > 0) {
    try {
        diasCerrados.addAll(GastoDAO.diasCerrados(idHogar));
        seleccionarDia(diaSeleccionado);
    } catch (java.sql.SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "No se pudo consultar el estado de los días:\n" + ex.getMessage(),
                "Oracle",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
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

    public int getIdHogar() { return idHogar; }

    private void initComponents() {
        setTitle(nombreHogar);
        EstiloInterfaz.tamanoTableta(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER);
        headerPanel.add(EstiloInterfaz.encabezado(nombreHogar, direccionHogar), BorderLayout.CENTER);
        JButton volver = EstiloInterfaz.boton("Volver", false);
        JPanel navegacion = EstiloInterfaz.filaBotones();
        navegacion.setBorder(new EmptyBorder(24, 0, 12, 14));
        navegacion.add(volver);
        headerPanel.add(navegacion, BorderLayout.EAST);
        volver.addActionListener(e -> volverAHogares());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                volverAHogares();
            }
        });
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Cuerpo
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(COLOR_BG);
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblCiclo = EstiloInterfaz.titulo("Ciclo de 14 días");
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
        lblLeyenda1.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        lblLeyenda1.setForeground(COLOR_TEXT_MUTED);
        lblLeyenda1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLeyenda2 = new JLabel("Gris: sin iniciar");
        lblLeyenda2.setFont(EstiloInterfaz.fuente(Font.BOLD, 13));
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
        cardGastos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
        cardGastos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSeccionD = EstiloInterfaz.titulo("Gastos diarios del hogar");
        lblSeccionD.setForeground(COLOR_TEXT_DARK);

        lblGastosSub = new JLabel("Día 1 · 0 gastos registrados");
        lblGastosSub.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        lblGastosSub.setForeground(COLOR_TEXT_MUTED);

        btnRegistrarGastos = new RoundedButton("Consultar gastos del día 1", COLOR_HEADER, Color.WHITE);
        
        btnRegistrarGastos.addActionListener(e -> {
            GastosDiaForm gastos = new GastosDiaForm(idHogar, nombreHogar, diaSeleccionado, this);
            gastos.setVisible(true);
            setVisible(false);
        });

        cardGastos.add(lblSeccionD);
        cardGastos.add(Box.createRigidArea(new Dimension(0, 2)));
        cardGastos.add(lblGastosSub);
        cardGastos.add(Box.createRigidArea(new Dimension(0, 6)));
        cardGastos.add(btnRegistrarGastos);
        lblGastosSub.setText("Los gastos son diligenciados por el jefe del hogar.");

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
        cardTransmitir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        cardTransmitir.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblFinalizar = EstiloInterfaz.titulo("Finalizar y transmitir");
        lblFinalizar.setForeground(COLOR_TEXT_DARK);

        JLabel lblInfoTransmitir1 = new JLabel("Disponible cuando los 14 días estén completos.");
        lblInfoTransmitir1.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        lblInfoTransmitir1.setForeground(COLOR_TEXT_MUTED);

        lblInfoTransmitir2 = new JLabel("Faltan 14 días por completar.");
        lblInfoTransmitir2.setFont(EstiloInterfaz.fuente(Font.PLAIN, 13));
        lblInfoTransmitir2.setForeground(COLOR_TEXT_MUTED);

        btnTransmitir = new RoundedButton("Transmitir a revisión", COLOR_HEADER, Color.WHITE);
        
        // Habilitado por defecto para pruebas o según progreso
        btnTransmitir.setEnabled(true); 

        btnTransmitir.addActionListener(e -> {
            if (!(ventanaPadre instanceof MisHogaresForm)) { JOptionPane.showMessageDialog(this,"Inicie sesión como encuestador."); return; }
            int encuestador=((MisHogaresForm)ventanaPadre).getIdEncuestadorActual();
            try { CasosDAO.transmitir(idHogar,encuestador); JOptionPane.showMessageDialog(this,"Encuesta transmitida a revisión."); }
            catch (java.sql.SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"No se pudo transmitir",JOptionPane.ERROR_MESSAGE); }
        });

        cardTransmitir.add(lblFinalizar);
        cardTransmitir.add(Box.createRigidArea(new Dimension(0, 2)));
        cardTransmitir.add(lblInfoTransmitir1);
        cardTransmitir.add(lblInfoTransmitir2);
        cardTransmitir.add(Box.createRigidArea(new Dimension(0, 6)));
        cardTransmitir.add(btnTransmitir);
        JButton btnVisita=EstiloInterfaz.boton("Registrar visita de hoy", false);
        btnVisita.addActionListener(e -> {
            if (!(ventanaPadre instanceof MisHogaresForm)) { JOptionPane.showMessageDialog(this,"Inicie sesión como encuestador."); return; }
            try { CasosDAO.visita(idHogar,((MisHogaresForm)ventanaPadre).getIdEncuestadorActual()); JOptionPane.showMessageDialog(this,"Visita registrada en Oracle."); }
            catch (java.sql.SQLException ex) { JOptionPane.showMessageDialog(this,ex.getMessage(),"No se pudo registrar la visita",JOptionPane.ERROR_MESSAGE); }
        });
        btnVisita.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVisita.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardTransmitir.add(Box.createVerticalStrut(6));
        cardTransmitir.add(btnVisita);
        JButton btnVerEncuestados=EstiloInterfaz.boton("Ver ID de encuestados", false);
        btnVerEncuestados.addActionListener(e -> {
            try {
                StringBuilder lista = new StringBuilder();
                for (EncuestadoResumen encuestado : HogarDAO.listarEncuestados(idHogar)) {
                    lista.append("ID ").append(encuestado.id()).append(" - ")
                            .append(encuestado.nombre()).append("\n");
                }
                JOptionPane.showMessageDialog(this, lista.length() == 0 ? "No hay encuestados registrados." : lista.toString());
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnVerEncuestados.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnVerEncuestados.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardTransmitir.add(Box.createVerticalStrut(6));
        cardTransmitir.add(btnVerEncuestados);

        JButton btnObservaciones = EstiloInterfaz.boton("Ver o resolver observaciones", false);
        btnObservaciones.addActionListener(e -> {
            try {
                String observaciones = CasosDAO.observacionesPendientes(idHogar);
                if (observaciones.isBlank()) {
                    JOptionPane.showMessageDialog(this, "No hay observaciones pendientes.");
                    return;
                }
                JTextArea lista = new JTextArea(observaciones, 9, 34);
                lista.setEditable(false);
                JTextField idObservacion = new JTextField();
                Object[] campos = {new JScrollPane(lista),
                    "Cuando el jefe haya corregido el gasto, escriba el ID de la observación", idObservacion};
                int respuesta = JOptionPane.showConfirmDialog(this, campos, "Observaciones pendientes",
                        JOptionPane.OK_CANCEL_OPTION);
                if (respuesta == JOptionPane.OK_OPTION) {
                    CasosDAO.resolverObservacion(idHogar, SesionActual.id(),
                            Integer.parseInt(idObservacion.getText().trim()));
                    JOptionPane.showMessageDialog(this, "Observación resuelta.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un ID de observación numérico.");
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Observaciones", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnObservaciones.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnObservaciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cardTransmitir.add(Box.createVerticalStrut(6));
        cardTransmitir.add(btnObservaciones);

        bodyPanel.add(cardTransmitir);

        JScrollPane scrollPane = new JScrollPane(bodyPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void volverAHogares() {
        if (ventanaPadre instanceof MisHogaresForm hogares && hogares.isDisplayable()) {
            hogares.cargarHogaresDesdeOracle();
            hogares.setVisible(true);
        } else if (ventanaPadre != null && ventanaPadre.isDisplayable()) {
            ventanaPadre.setVisible(true);
        } else {
            new AccesoForm().setVisible(true);
        }
        dispose();
    }

    private void seleccionarDia(int dia) {
        this.diaSeleccionado = dia;
        for (BotonDia btn : listaBotonesDias) {
            btn.setSeleccionado(btn.getDia() == dia);
        }

        if (diasCerrados.contains(dia)) {
            lblGastosSub.setText("Día " + dia + " · COMPLETO Y CERRADO");

        } else {
            lblGastosSub.setText("Día " + dia + " · Gastos en progreso");
        }
        btnRegistrarGastos.setText("Consultar gastos del día " + dia);
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
            setFont(EstiloInterfaz.fuente(Font.BOLD, 13));
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
            setFont(EstiloInterfaz.fuente(Font.BOLD, 13));
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

}
