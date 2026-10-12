package registrohogar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NuevoGastoForm extends JFrame {

    private static final Color COLOR_HEADER = new Color(46, 63, 80);
    private static final Color COLOR_BG = new Color(238, 240, 245);
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_BORDER = new Color(200, 205, 215);

    private JTextField txtArtículo;
    private JTextField txtCategoria;
    private JTextField txtCantidad;
    private JComboBox<String> cbUnidad;
    private JComboBox<String> cbAdquisicion;
    private JTextField txtValor;
    private JComboBox<String> cbFrecuencia;
    private JComboBox<String> cbLugar;
    private JToggleButton btnTipoHogar;
    private JToggleButton btnTipoPersonal;
    private ButtonGroup bgTipoGasto;

    private String nombreHogar;
    private int diaSeleccionado;
    private int indiceEdicion = -1;

    public NuevoGastoForm() {
        this("Hogar 45091", 1);
    }

    public NuevoGastoForm(String nombreHogar, int diaSeleccionado) {
        this.nombreHogar = nombreHogar;
        this.diaSeleccionado = diaSeleccionado;
        initComponents();
    }

    public NuevoGastoForm(String nombreHogar, int diaSeleccionado, int indiceEdicion, String[] datosGasto) {
        this.nombreHogar = nombreHogar;
        this.diaSeleccionado = diaSeleccionado;
        this.indiceEdicion = indiceEdicion;
        initComponents();
        cargarDatosEdicion(datosGasto);
    }

    private void initComponents() {
        setTitle(indiceEdicion >= 0 ? "Editar gasto" : "Nuevo gasto");
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

        JLabel lblTitulo = new JLabel(indiceEdicion >= 0 ? "Editar gasto" : "Nuevo gasto");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel(nombreHogar + " · Día " + diaSeleccionado + " · Sección D");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSubtitulo.setForeground(new Color(185, 195, 205));

        headerPanel.add(lblTitulo);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        headerPanel.add(lblSubtitulo);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(COLOR_BG);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 11);

        JLabel lblArtículo = new JLabel("Artículo *");
        lblArtículo.setFont(labelFont);
        lblArtículo.setForeground(COLOR_TEXT_DARK);
        txtArtículo = createRoundedTextField();
        txtArtículo.setEditable(false);
        txtArtículo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        txtArtículo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BuscarArticuloForm buscador = new BuscarArticuloForm(NuevoGastoForm.this, nombreHogar, diaSeleccionado);
                buscador.setVisible(true);
            }
        });

        JLabel lblCat = new JLabel("Categoría COICOP (automática)");
        lblCat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCat.setForeground(new Color(120, 130, 145));
        txtCategoria = createRoundedTextField();
        txtCategoria.setEditable(false);

        JPanel rowCantUnidad = new JPanel(new GridLayout(1, 2, 8, 0));
        rowCantUnidad.setOpaque(false);

        JPanel colCant = new JPanel();
        colCant.setOpaque(false);
        colCant.setLayout(new BoxLayout(colCant, BoxLayout.Y_AXIS));
        JLabel lblCant = new JLabel("Cantidad *");
        lblCant.setFont(labelFont);
        lblCant.setForeground(COLOR_TEXT_DARK);
        txtCantidad = createRoundedTextField();
        aplicarFiltroNumeros(txtCantidad);
        colCant.add(lblCant);
        colCant.add(Box.createRigidArea(new Dimension(0, 3)));
        colCant.add(txtCantidad);

        JPanel colUnidad = new JPanel();
        colUnidad.setOpaque(false);
        colUnidad.setLayout(new BoxLayout(colUnidad, BoxLayout.Y_AXIS));
        JLabel lblUnidad = new JLabel("Unidad *");
        lblUnidad.setFont(labelFont);
        lblUnidad.setForeground(COLOR_TEXT_DARK);
        cbUnidad = new JComboBox<>(new String[]{"", "Unidad", "Kilogramo", "Litro", "Paquete", "Gramos"});
        styleComboBox(cbUnidad);
        colUnidad.add(lblUnidad);
        colUnidad.add(Box.createRigidArea(new Dimension(0, 3)));
        colUnidad.add(cbUnidad);

        rowCantUnidad.add(colCant);
        rowCantUnidad.add(colUnidad);

        JLabel lblAdquisicion = new JLabel("Forma de adquisición *");
        lblAdquisicion.setFont(labelFont);
        lblAdquisicion.setForeground(COLOR_TEXT_DARK);
        cbAdquisicion = new JComboBox<>(new String[]{"", "Compra", "Regalo", "Intercambio", "Producción propia"});
        styleComboBox(cbAdquisicion);

        JLabel lblValor = new JLabel("Valor (COP) *");
        lblValor.setFont(labelFont);
        lblValor.setForeground(COLOR_TEXT_DARK);
        txtValor = createRoundedTextField();
        aplicarFiltroNumeros(txtValor);

        JLabel lblFrecuencia = new JLabel("Frecuencia de compra *");
        lblFrecuencia.setFont(labelFont);
        lblFrecuencia.setForeground(COLOR_TEXT_DARK);
        cbFrecuencia = new JComboBox<>(new String[]{"", "Diario", "Semanal", "Quincenal", "Mensual", "Ocasional"});
        styleComboBox(cbFrecuencia);

        JLabel lblLugar = new JLabel("Lugar de compra");
        lblLugar.setFont(labelFont);
        lblLugar.setForeground(COLOR_TEXT_DARK);
        cbLugar = new JComboBox<>(new String[]{"", "No aplica", "Supermercado", "Tienda de barrio", "Plaza de mercado", "Otro"});
        styleComboBox(cbLugar);

        JLabel lblTipo = new JLabel("Tipo de gasto *");
        lblTipo.setFont(labelFont);
        lblTipo.setForeground(COLOR_TEXT_DARK);

        JPanel panelTipo = new JPanel(new GridLayout(1, 2, 8, 0));
        panelTipo.setOpaque(false);

        btnTipoHogar = createCardToggleButton("1 · Gasto del hogar");
        btnTipoPersonal = createCardToggleButton("2 · Gasto personal");

        bgTipoGasto = new ButtonGroup();
        bgTipoGasto.add(btnTipoHogar);
        bgTipoGasto.add(btnTipoPersonal);

        panelTipo.add(btnTipoHogar);
        panelTipo.add(btnTipoPersonal);

        JButton btnGuardarOtro = new RoundedButton(indiceEdicion >= 0 ? "Guardar cambios" : "Guardar y agregar otro", COLOR_HEADER, Color.WHITE);
        JButton btnGuardarVolver = new RoundedButton("Guardar y volver", Color.WHITE, COLOR_HEADER);

        btnGuardarOtro.addActionListener(e -> guardarGasto(indiceEdicion < 0));
        btnGuardarVolver.addActionListener(e -> guardarGasto(false));

        formPanel.add(lblArtículo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(txtArtículo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblCat);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(txtCategoria);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(rowCantUnidad);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblAdquisicion);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(cbAdquisicion);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblValor);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(txtValor);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblFrecuencia);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(cbFrecuencia);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblLugar);
        formPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formPanel.add(cbLugar);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formPanel.add(lblTipo);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(panelTipo);

        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        formPanel.add(btnGuardarOtro);
        formPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        formPanel.add(btnGuardarVolver);

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void cargarDatosEdicion(String[] d) {
        txtArtículo.setText(d[0]);
        txtCantidad.setText(d[1]);
        cbUnidad.setSelectedItem(d[2]);
        cbAdquisicion.setSelectedItem(d[3]);
        txtCategoria.setText(d[4]);
        txtValor.setText(d[5]);
        if (d.length > 6) cbFrecuencia.setSelectedItem(d[6]);
        if (d.length > 7) {
            if (d[7].contains("1")) btnTipoHogar.setSelected(true);
            else btnTipoPersonal.setSelected(true);
        }
    }

    public void setArticuloSeleccionado(String nombre, String categoria) {
        txtArtículo.setText(nombre);
        txtCategoria.setText(categoria);
    }

    private JTextField createRoundedTextField() {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        field.setOpaque(false);
        field.setBackground(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(0, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setBorder(new EmptyBorder(0, 8, 0, 8));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(0, 32));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        combo.setBackground(Color.WHITE);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                super.paintComponent(g2);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? COLOR_HEADER : COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(0, 34));
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

    private void limpiarCampos() {
        txtArtículo.setText("");
        txtCategoria.setText("");
        txtCantidad.setText("");
        txtValor.setText("");
        cbUnidad.setSelectedIndex(0);
        cbAdquisicion.setSelectedIndex(0);
        cbFrecuencia.setSelectedIndex(0);
        cbLugar.setSelectedIndex(0);
        bgTipoGasto.clearSelection();
    }

    private void guardarGasto(boolean agregarOtro) {
        if (txtArtículo.getText().trim().isEmpty() || 
            txtCantidad.getText().trim().isEmpty() || 
            txtValor.getText().trim().isEmpty() || 
            cbUnidad.getSelectedIndex() <= 0 || 
            cbAdquisicion.getSelectedIndex() <= 0 || 
            cbFrecuencia.getSelectedIndex() <= 0 || 
            (!btnTipoHogar.isSelected() && !btnTipoPersonal.isSelected())) {

            JOptionPane.showMessageDialog(this, 
                "Por favor complete todos los campos obligatorios (*).", 
                "Campos Incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String frecuencia = cbFrecuencia.getSelectedItem().toString();
        String tipoGasto = btnTipoHogar.isSelected() ? "1 · Gasto del hogar" : "2 · Gasto personal";

        if (indiceEdicion >= 0) {
            GastosDiaForm.actualizarGasto(
                diaSeleccionado,
                indiceEdicion,
                txtArtículo.getText().trim(),
                txtCantidad.getText().trim(),
                cbUnidad.getSelectedItem().toString(),
                cbAdquisicion.getSelectedItem().toString(),
                txtCategoria.getText().trim(),
                txtValor.getText().trim(),
                frecuencia,
                tipoGasto
            );
            JOptionPane.showMessageDialog(this, "¡Gasto actualizado con éxito!");
        } else {
            GastosDiaForm.agregarGastoAManuscrito(
                diaSeleccionado,
                txtArtículo.getText().trim(),
                txtCantidad.getText().trim(),
                cbUnidad.getSelectedItem().toString(),
                cbAdquisicion.getSelectedItem().toString(),
                txtCategoria.getText().trim(),
                txtValor.getText().trim(),
                frecuencia,
                tipoGasto
            );
            JOptionPane.showMessageDialog(this, "¡Gasto guardado con éxito!");
        }

        if (agregarOtro && indiceEdicion < 0) {
            limpiarCampos();
        } else {
            GastosDiaForm resumen = new GastosDiaForm(nombreHogar, diaSeleccionado);
            resumen.setVisible(true);
            this.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NuevoGastoForm().setVisible(true));
    }
}