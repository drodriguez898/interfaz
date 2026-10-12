package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class AgregarIntegranteForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_BORDER = new Color(200, 205, 215);
    private static final Color COLOR_TEXT_MUTED = new Color(130, 140, 155);

    private JTextField txtNombreCompleto;
    private JTextField txtEdad;
    private JComboBox<String> cbSexo;
    private JComboBox<String> cbParentesco;
    private JToggleButton btnSi;
    private JToggleButton btnNo;
    private ButtonGroup bgResponsable;

    private IntegrantesHogarForm padreForm;

    public AgregarIntegranteForm() {
        this(null);
    }

    public AgregarIntegranteForm(IntegrantesHogarForm padreForm) {
        this.padreForm = padreForm;
        initComponents();
    }

    private void initComponents() {
        setTitle("Agregar integrante");
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

        JLabel lblTitulo = new JLabel("Agregar integrante");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Ingresa los datos del integrante");
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

        JLabel lblNombre = new JLabel("Nombre completo *");
        lblNombre.setFont(labelFont);
        lblNombre.setForeground(COLOR_TEXT_DARK);
        txtNombreCompleto = createRoundedTextField();
        aplicarFiltroTexto(txtNombreCompleto);

        JPanel rowEdadSexo = new JPanel(new GridLayout(1, 2, 10, 0));
        rowEdadSexo.setOpaque(false);

        JPanel colEdad = new JPanel();
        colEdad.setOpaque(false);
        colEdad.setLayout(new BoxLayout(colEdad, BoxLayout.Y_AXIS));
        JLabel lblEdad = new JLabel("Edad *");
        lblEdad.setFont(labelFont);
        lblEdad.setForeground(COLOR_TEXT_DARK);
        txtEdad = createRoundedTextField();
        aplicarFiltroNumeros(txtEdad);
        colEdad.add(lblEdad);
        colEdad.add(Box.createRigidArea(new Dimension(0, 4)));
        colEdad.add(txtEdad);

        JPanel colSexo = new JPanel();
        colSexo.setOpaque(false);
        colSexo.setLayout(new BoxLayout(colSexo, BoxLayout.Y_AXIS));
        JLabel lblSexo = new JLabel("Sexo *");
        lblSexo.setFont(labelFont);
        lblSexo.setForeground(COLOR_TEXT_DARK);
        cbSexo = new JComboBox<>(new String[]{"", "Mujer", "Hombre", "Otro"});
        styleComboBox(cbSexo);
        colSexo.add(lblSexo);
        colSexo.add(Box.createRigidArea(new Dimension(0, 4)));
        colSexo.add(cbSexo);

        rowEdadSexo.add(colEdad);
        rowEdadSexo.add(colSexo);

        JLabel lblParentesco = new JLabel("Parentesco con el jefe del hogar *");
        lblParentesco.setFont(labelFont);
        lblParentesco.setForeground(COLOR_TEXT_DARK);
        cbParentesco = new JComboBox<>(new String[]{
            "", "Jefe del hogar", "Cónyuge", "Hijo", 
            "Padre / Madre", "Nieto(a)", "Otro familiar", "No familiar"
        });
        styleComboBox(cbParentesco);

        JLabel lblResponsable = new JLabel("¿Es responsable de gastos? *");
        lblResponsable.setFont(labelFont);
        lblResponsable.setForeground(COLOR_TEXT_DARK);

        JPanel panelGastos = new JPanel(new GridLayout(1, 2, 10, 0));
        panelGastos.setOpaque(false);

        btnSi = createCardToggleButton("Sí");
        btnNo = createCardToggleButton("No");

        bgResponsable = new ButtonGroup();
        bgResponsable.add(btnSi);
        bgResponsable.add(btnNo);

        panelGastos.add(btnSi);
        panelGastos.add(btnNo);

        JLabel lblNota = new JLabel("Solo las personas mayores de 10 años registran gastos.");
        lblNota.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblNota.setForeground(COLOR_TEXT_MUTED);

        JButton btnGuardar = new RoundedButton("Guardar integrante", COLOR_HEADER, Color.WHITE);
        JButton btnCancelar = new RoundedButton("Cancelar", Color.WHITE, COLOR_HEADER);

        btnCancelar.addActionListener(e -> {
            limpiarCampos();
            this.dispose();
        });

        btnGuardar.addActionListener(e -> guardarIntegrante());

        formPanel.add(lblNombre);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(txtNombreCompleto);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(rowEdadSexo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(lblParentesco);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(cbParentesco);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(lblResponsable);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(panelGastos);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(lblNota);

        formPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        formPanel.add(btnGuardar);
        formPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        formPanel.add(btnCancelar);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void limpiarCampos() {
        txtNombreCompleto.setText("");
        txtEdad.setText("");
        cbSexo.setSelectedIndex(0);
        cbParentesco.setSelectedIndex(0);
        bgResponsable.clearSelection();
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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(0, 36));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

    private JToggleButton createCardToggleButton(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(COLOR_HEADER);
                    setForeground(Color.WHITE);
                } else {
                    g2.setColor(Color.WHITE);
                    setForeground(COLOR_TEXT_DARK);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? COLOR_HEADER : COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(0, 38));
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

    private void guardarIntegrante() {
        if (txtNombreCompleto.getText().trim().isEmpty() || 
            txtEdad.getText().trim().isEmpty() || 
            cbSexo.getSelectedIndex() <= 0 || 
            cbParentesco.getSelectedIndex() <= 0 || 
            (!btnSi.isSelected() && !btnNo.isSelected())) {
            
            JOptionPane.showMessageDialog(this, 
                "Por favor complete todos los campos obligatorios (*).", 
                "Campos Incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (padreForm != null) {
            padreForm.agregarPersona(
                txtNombreCompleto.getText().trim(),
                txtEdad.getText().trim(),
                cbSexo.getSelectedItem().toString(),
                cbParentesco.getSelectedItem().toString(),
                btnSi.isSelected()
            );
        }

        limpiarCampos();
        this.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AgregarIntegranteForm().setVisible(true));
    }
}