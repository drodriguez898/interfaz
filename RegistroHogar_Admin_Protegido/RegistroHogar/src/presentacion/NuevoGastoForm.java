package presentacion;

import negocio.DatosEditables;
import persistencia.GastoDAO;

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
import java.sql.SQLException;

public class NuevoGastoForm extends JFrame {

    private static final Color COLOR_HEADER = EstiloInterfaz.TINTA;
    private static final Color COLOR_BG = EstiloInterfaz.FONDO;
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 55);
    private static final Color COLOR_BORDER = new Color(200, 205, 215);

    private JTextField txtArtículo;
    private JTextField txtCategoria;
    private JTextField txtCantidad;
    private UnidadMedidaPanel unidadMedida;
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
    private Integer idGastoEdicion;
    private DatosEditables original;

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
    
        private GastosDiaForm ventanaGastos;

            void setVentanaGastos(GastosDiaForm ventanaGastos) {
            this.ventanaGastos = ventanaGastos;
            if (indiceEdicion >= 0 && idGastoEdicion == null) {
                // El identificador se recibe junto con los datos desde el detalle.
                throw new IllegalStateException("Falta el ID del gasto a editar");
            }
        }

    void setEdicion(int idGasto, DatosEditables datos) {
        idGastoEdicion = idGasto;
        original = datos;
        if (datos.cantidad() != null) {
            txtCantidad.setText(datos.cantidad().toPlainString().replace('.', ','));
        }
        if (datos.valor() != null) {
            txtValor.setText(datos.valor().toPlainString().replace('.', ','));
        }
        unidadMedida.seleccionar(datos.unidad());
        seleccionarValor(cbAdquisicion, datos.adquisicion());
        seleccionarValor(cbFrecuencia, datos.frecuencia());
        seleccionarValor(cbLugar, datos.lugar());
        if (datos.tipo() != null) {
            if (datos.tipo().startsWith("1")) btnTipoHogar.setSelected(true);
            else if (datos.tipo().startsWith("2")) btnTipoPersonal.setSelected(true);
        }
    }

    private void seleccionarValor(JComboBox<String> control, String valor) {
        if (valor == null) {
            control.setSelectedIndex(0);
            return;
        }
        DefaultComboBoxModel<String> opciones = (DefaultComboBoxModel<String>) control.getModel();
        if (opciones.getIndexOf(valor) < 0) {
            opciones.addElement(valor);
        }
        control.setSelectedItem(valor);
    }

        private void volverAGastos() {
            if (ventanaGastos != null && ventanaGastos.isDisplayable()) {
                ventanaGastos.cargarGastos();
                ventanaGastos.setVisible(true);
            } else {
                new GastosDiaForm(nombreHogar, diaSeleccionado).setVisible(true);
            }
            dispose();
        }

    private void initComponents() {
        setTitle(indiceEdicion >= 0 ? "Editar gasto" : "Nuevo gasto");
        EstiloInterfaz.tamanoTableta(this);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evento) {
                volverAGastos();
            }
        });

        JPanel principal = new JPanel(new BorderLayout());
        principal.setBackground(COLOR_BG);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(COLOR_HEADER);
        cabecera.add(EstiloInterfaz.encabezado(
                indiceEdicion >= 0 ? "Editar gasto" : "Nuevo gasto",
                nombreHogar + " · Día " + diaSeleccionado), BorderLayout.CENTER);
        JPanel navegacion = EstiloInterfaz.filaBotones();
        navegacion.setBorder(new EmptyBorder(25, 0, 15, 22));
        JButton volver = EstiloInterfaz.boton("Volver", false);
        volver.addActionListener(e -> volverAGastos());
        navegacion.add(volver);
        cabecera.add(navegacion, BorderLayout.EAST);
        principal.add(cabecera, BorderLayout.NORTH);

        txtArtículo = createRoundedTextField();
        txtArtículo.setEditable(false);
        txtArtículo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        txtArtículo.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                abrirBuscador();
            }
        });
        JButton buscarArticulo = EstiloInterfaz.boton("Buscar artículo", false);
        buscarArticulo.addActionListener(e -> abrirBuscador());
        JPanel articulo = new JPanel(new BorderLayout(8, 0));
        articulo.setOpaque(false);
        articulo.add(txtArtículo, BorderLayout.CENTER);
        articulo.add(buscarArticulo, BorderLayout.EAST);

        txtCategoria = createRoundedTextField();
        txtCategoria.setEditable(false);
        txtCantidad = createRoundedTextField();
        txtValor = createRoundedTextField();
        aplicarFiltroNumeros(txtCantidad);
        aplicarFiltroNumeros(txtValor);
        unidadMedida = new UnidadMedidaPanel();

        cbAdquisicion = new JComboBox<>(new String[]{"", "Compra", "Regalo", "Intercambio", "Producción propia"});
        cbFrecuencia = new JComboBox<>(new String[]{"", "Diario", "Semanal", "Quincenal", "Mensual", "Ocasional"});
        cbLugar = new JComboBox<>(new String[]{"", "No aplica", "Supermercado", "Tienda de barrio", "Plaza de mercado", "Otro"});
        styleComboBox(cbAdquisicion);
        styleComboBox(cbFrecuencia);
        styleComboBox(cbLugar);

        btnTipoHogar = createCardToggleButton("Gasto del hogar");
        btnTipoPersonal = createCardToggleButton("Gasto personal");
        bgTipoGasto = new ButtonGroup();
        bgTipoGasto.add(btnTipoHogar);
        bgTipoGasto.add(btnTipoPersonal);
        JPanel tipos = new JPanel(new GridLayout(1, 2, 8, 0));
        tipos.setOpaque(false);
        tipos.add(btnTipoHogar);
        tipos.add(btnTipoPersonal);

        JPanel campos = EstiloInterfaz.tarjeta(new GridBagLayout());
        agregarCampo(campos, EstiloInterfaz.campo("Artículo *", articulo), 0, 0, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Categoría COICOP", txtCategoria), 1, 0, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Cantidad *", txtCantidad), 0, 1, 1);
        agregarCampo(campos, unidadMedida, 1, 1, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Forma de adquisición *", cbAdquisicion), 0, 2, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Valor en COP *", txtValor), 1, 2, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Frecuencia de compra *", cbFrecuencia), 0, 3, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Lugar de compra", cbLugar), 1, 3, 1);
        agregarCampo(campos, EstiloInterfaz.campo("Tipo de gasto *", tipos), 0, 4, 2);

        JScrollPane scroll = new JScrollPane(campos);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setBackground(COLOR_BG);
        cuerpo.setBorder(new EmptyBorder(18, 22, 18, 22));
        cuerpo.add(scroll, BorderLayout.CENTER);

        JPanel acciones = EstiloInterfaz.filaBotones();
        JButton guardarVolver = EstiloInterfaz.boton("Guardar y volver", false);
        JButton guardarOtro = EstiloInterfaz.boton(
                indiceEdicion >= 0 ? "Guardar cambios" : "Guardar y agregar otro", true);
        guardarVolver.addActionListener(e -> guardarGasto(false));
        guardarOtro.addActionListener(e -> guardarGasto(indiceEdicion < 0));
        acciones.add(guardarVolver);
        acciones.add(guardarOtro);
        cuerpo.add(acciones, BorderLayout.SOUTH);
        principal.add(cuerpo, BorderLayout.CENTER);
        setContentPane(principal);
    }

    private void agregarCampo(JPanel panel, JComponent componente, int columna, int fila, int ancho) {
        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.gridx = columna;
        restricciones.gridy = fila;
        restricciones.gridwidth = ancho;
        restricciones.weightx = ancho;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.anchor = GridBagConstraints.NORTHWEST;
        restricciones.insets = new Insets(7, 7, 7, 7);
        panel.add(componente, restricciones);
    }

    private void abrirBuscador() {
        BuscarArticuloForm buscador = new BuscarArticuloForm(this, nombreHogar, diaSeleccionado);
        buscador.setVisible(true);
        setVisible(false);
    }

    private void cargarDatosEdicion(String[] d) {
        txtArtículo.setText(d[0]);
        txtCantidad.setText(d[1]);
        unidadMedida.seleccionar(d[2]);
        cbAdquisicion.setSelectedItem(d[3]);
        txtCategoria.setText(d[4]);
        txtValor.setText(d[5] == null ? "" : d[5].replace('.', ','));
        if (d.length > 6) cbFrecuencia.setSelectedItem(d[6]);
        if (d.length > 7 && d[7] != null) {
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
        field.setFont(EstiloInterfaz.fuente(Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(0, 32));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        field.setBorder(new EmptyBorder(0, 8, 0, 8));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setPreferredSize(new Dimension(0, 32));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        combo.setBackground(Color.WHITE);
        combo.setFont(EstiloInterfaz.fuente(Font.PLAIN, 12));
    }

    private void aplicarFiltroNumeros(JTextField tf) {
        ((AbstractDocument) tf.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string != null && string.matches("[0-9.,]+")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text != null && text.matches("[0-9.,]*")) {
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
        btn.setFont(EstiloInterfaz.fuente(Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(0, 34));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void limpiarCampos() {
        txtArtículo.setText("");
        txtCategoria.setText("");
        txtCantidad.setText("");
        txtValor.setText("");
        unidadMedida.limpiar();
        cbAdquisicion.setSelectedIndex(0);
        cbFrecuencia.setSelectedIndex(0);
        cbLugar.setSelectedIndex(0);
        bgTipoGasto.clearSelection();
    }

    private void guardarGasto(boolean agregarOtro) {
        if (txtArtículo.getText().trim().isEmpty() || 
            txtCantidad.getText().trim().isEmpty() || 
            txtValor.getText().trim().isEmpty() || 
            unidadMedida.valor().isEmpty() || 
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

        try {
            if (ventanaGastos==null) throw new SQLException("Abre el gasto desde un hogar registrado.");
            GastoDAO.guardar(ventanaGastos.getIdHogar(),diaSeleccionado,idGastoEdicion,
                txtArtículo.getText().trim(),txtCantidad.getText().trim(),
                unidadMedida.valor(),cbAdquisicion.getSelectedItem().toString(),
                txtValor.getText().trim(),frecuencia,tipoGasto,
                cbLugar.getSelectedItem()==null?null:cbLugar.getSelectedItem().toString(), original);
            JOptionPane.showMessageDialog(this,indiceEdicion>=0?"Gasto actualizado en Oracle.":"Gasto guardado en Oracle.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,"No se guardó el gasto:\n"+ex.getMessage(),"Oracle",JOptionPane.ERROR_MESSAGE);
            return;
        }

            if (agregarOtro && indiceEdicion < 0) {
                limpiarCampos();
            } else {
                volverAGastos();
            }
    }

}
