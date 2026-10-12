package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class RegistroHogarForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_BORDER = new Color(200, 205, 215);

    private JTextField txtNombre;
    private JTextField txtDireccion;
    private JTextField txtBarrio;
    private JComboBox<String> cbMunicipio;
    private JTextField txtTelefono;
    private JTextField txtPersonas;
    private JTextField txtFecha;

    private MisHogaresForm menuPrincipal;

    public RegistroHogarForm() {
        this(null);
    }

    public RegistroHogarForm(MisHogaresForm menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        initComponents();
    }

    private void initComponents() {
        setTitle("Registro del hogar");
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

        JLabel lblTitulo = new JLabel("Registro del hogar");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Paso 1 de 2 · Información básica");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formPanel = new JPanel();
        formPanel.setBackground(COLOR_BG);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        Color labelColor = new Color(30, 41, 55);

        txtNombre = addField(formPanel, "Nombre de la familia / hogar *", labelFont, labelColor);
        txtDireccion = addField(formPanel, "Dirección *", labelFont, labelColor);
        txtBarrio = addField(formPanel, "Barrio / Sector", labelFont, labelColor);

        aplicarFiltroTexto(txtNombre);
        aplicarFiltroTexto(txtBarrio);

        JLabel lblMunicipio = new JLabel("Municipio *");
        lblMunicipio.setFont(labelFont);
        lblMunicipio.setForeground(labelColor);
        cbMunicipio = new JComboBox<>(new String[]{"", "Bucaramanga", "Floridablanca", "Girón", "Piedecuesta"});
        cbMunicipio.setPreferredSize(new Dimension(0, 36));
        cbMunicipio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbMunicipio.setBackground(Color.WHITE);
        cbMunicipio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        formPanel.add(lblMunicipio);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(cbMunicipio);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        txtTelefono = addField(formPanel, "Teléfono de contacto", labelFont, labelColor);
        aplicarFiltroNumeros(txtTelefono);

        JPanel rowPersonasFecha = new JPanel(new GridLayout(1, 2, 10, 0));
        rowPersonasFecha.setOpaque(false);

        JPanel colPersonas = new JPanel();
        colPersonas.setOpaque(false);
        colPersonas.setLayout(new BoxLayout(colPersonas, BoxLayout.Y_AXIS));
        JLabel lblPersonas = new JLabel("Personas *");
        lblPersonas.setFont(labelFont);
        lblPersonas.setForeground(labelColor);
        txtPersonas = createRoundedTextField();
        aplicarFiltroNumeros(txtPersonas);
        colPersonas.add(lblPersonas);
        colPersonas.add(Box.createRigidArea(new Dimension(0, 4)));
        colPersonas.add(txtPersonas);

        JPanel colFecha = new JPanel();
        colFecha.setOpaque(false);
        colFecha.setLayout(new BoxLayout(colFecha, BoxLayout.Y_AXIS));
        JLabel lblFecha = new JLabel("Fecha ciclo *");
        lblFecha.setFont(labelFont);
        lblFecha.setForeground(labelColor);
        txtFecha = createRoundedTextField();
        colFecha.add(lblFecha);
        colFecha.add(Box.createRigidArea(new Dimension(0, 4)));
        colFecha.add(txtFecha);

        rowPersonasFecha.add(colPersonas);
        rowPersonasFecha.add(colFecha);

        formPanel.add(rowPersonasFecha);
        formPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnSiguiente = new RoundedButton("Siguiente: integrantes", COLOR_HEADER, Color.WHITE);
        JButton btnCancelar = new RoundedButton("Cancelar", Color.WHITE, COLOR_HEADER);

        btnCancelar.addActionListener(e -> {
            limpiarCampos();
            if (menuPrincipal != null) {
                menuPrincipal.setVisible(true);
            }
            this.dispose();
        });
        
        btnSiguiente.addActionListener(e -> validarYSiguiente());

        formPanel.add(btnSiguiente);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(btnCancelar);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JTextField addField(JPanel panel, String title, Font font, Color color) {
        JLabel label = new JLabel(title);
        label.setFont(font);
        label.setForeground(color);
        JTextField tf = createRoundedTextField();
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(tf);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        return tf;
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setBorder(new EmptyBorder(0, 10, 0, 10));
        return field;
    }

    private void aplicarFiltroNumeros(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("\\d+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    private void aplicarFiltroTexto(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
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
            g2.setColor(bgColor.equals(Color.WHITE) ? COLOR_HEADER : bgColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtDireccion.setText("");
        txtBarrio.setText("");
        cbMunicipio.setSelectedIndex(0);
        txtTelefono.setText("");
        txtPersonas.setText("");
        txtFecha.setText("");
    }

    private void validarYSiguiente() {
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String municipio = cbMunicipio.getSelectedIndex() > 0 ? cbMunicipio.getSelectedItem().toString() : "";
        String personas = txtPersonas.getText().trim();

        if (nombre.isEmpty() || direccion.isEmpty() || municipio.isEmpty() || personas.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor complete todos los campos obligatorios (*).", 
                "Campos incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        IntegrantesHogarForm paso2 = new IntegrantesHogarForm(nombre, personas, direccion, municipio, menuPrincipal);
        paso2.setVisible(true);
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistroHogarForm().setVisible(true));
    }
}