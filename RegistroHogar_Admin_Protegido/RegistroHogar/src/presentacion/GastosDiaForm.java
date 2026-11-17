package presentacion;

import persistencia.GastoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GastosDiaForm extends JFrame {
    private final String nombreHogar;
    private final int idHogar;
    private final int dia;
    private DetalleHogarForm detalleHogarForm;
    private JFrame ventanaJefe;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Artículo", "Cantidad", "Unidad", "Adquisición", "Categoría", "Valor (COP)"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
        @Override public Class<?> getColumnClass(int columna) {
            return columna == 5 ? BigDecimal.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final CardLayout vistaLista = new CardLayout();
    private final JPanel contenidoLista = new JPanel(vistaLista);
    private final JLabel lblTotal = EstiloInterfaz.titulo("$ 0");
    private final JLabel lblCantidad = EstiloInterfaz.nota("");
    private final JLabel lblNota = EstiloInterfaz.nota("");
    private final JButton btnAgregarGasto = EstiloInterfaz.boton("Agregar gasto", true);
    private final JButton btnCerrarDia;
    private final JButton btnDetalle = EstiloInterfaz.boton("Ver detalle", false);
    private List<String[]> gastos = List.of();

    public GastosDiaForm() {
        this("Hogar 45091", 1, null);
    }

    public GastosDiaForm(String nombreHogar, int dia) {
        this(nombreHogar, dia, null);
    }

    public GastosDiaForm(String nombreHogar, int dia, DetalleHogarForm detalleHogarForm) {
        this(0, nombreHogar, dia, detalleHogarForm);
    }

    public GastosDiaForm(int idHogar, String nombreHogar, int dia, DetalleHogarForm detalleHogarForm) {
        this.idHogar = idHogar;
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.detalleHogarForm = detalleHogarForm;
        this.btnCerrarDia = EstiloInterfaz.boton("Cerrar día " + dia, false);
        initComponents();
        cargarGastos();
    }

    public GastosDiaForm(int idHogar, String nombreHogar, int dia, JFrame padre, boolean jefe) {
        this.idHogar = idHogar;
        this.nombreHogar = nombreHogar;
        this.dia = dia;
        this.ventanaJefe = padre;
        this.btnCerrarDia = EstiloInterfaz.boton("Cerrar día " + dia, false);
        initComponents();
        cargarGastos();
    }

    public int getIdHogar() { return idHogar; }

    private void initComponents() {
        setTitle("Gastos del día " + dia);
        EstiloInterfaz.tamanoTableta(this);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent evento) {
                volver();
            }
        });

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(EstiloInterfaz.FONDO);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(EstiloInterfaz.TINTA);
        cabecera.add(EstiloInterfaz.encabezado("Gastos del día " + dia, nombreHogar), BorderLayout.CENTER);
        JPanel navegacion = EstiloInterfaz.filaBotones();
        navegacion.setBorder(new EmptyBorder(25, 0, 15, 22));
        JButton volver = EstiloInterfaz.boton("Volver", false);
        volver.addActionListener(e -> volver());
        navegacion.add(volver);
        cabecera.add(navegacion, BorderLayout.EAST);
        main.add(cabecera, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setBackground(EstiloInterfaz.FONDO);
        cuerpo.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel resumen = EstiloInterfaz.tarjeta(new BorderLayout(12, 0));
        JPanel textoResumen = new JPanel(new GridLayout(0, 1, 0, 4));
        textoResumen.setOpaque(false);
        textoResumen.add(EstiloInterfaz.nota("Total registrado (COP)"));
        textoResumen.add(lblTotal);
        resumen.add(textoResumen, BorderLayout.WEST);
        resumen.add(lblCantidad, BorderLayout.EAST);
        cuerpo.add(resumen, BorderLayout.NORTH);

        JPanel listado = EstiloInterfaz.tarjeta(new BorderLayout(0, 12));
        listado.add(EstiloInterfaz.titulo("Gastos registrados"), BorderLayout.NORTH);
        JScrollPane scroll = EstiloInterfaz.tabla(tabla);
        tabla.setRowHeight(42);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(210);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(5).setCellRenderer(new ValorRenderer());
        contenidoLista.add(scroll, "lista");
        JPanel vacio = new JPanel(new GridBagLayout());
        vacio.setBackground(Color.WHITE);
        JLabel aviso = EstiloInterfaz.nota("Todavía no hay gastos registrados para este día.");
        aviso.setHorizontalAlignment(SwingConstants.CENTER);
        vacio.add(aviso);
        contenidoLista.add(vacio, "vacio");
        listado.add(contenidoLista, BorderLayout.CENTER);
        cuerpo.add(listado, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(10, 10));
        pie.setOpaque(false);
        pie.add(lblNota, BorderLayout.NORTH);
        JPanel acciones = EstiloInterfaz.filaBotones();
        acciones.add(btnDetalle);
        if (ventanaJefe != null) {
            acciones.add(btnAgregarGasto);
            acciones.add(btnCerrarDia);
        }
        pie.add(acciones, BorderLayout.SOUTH);
        cuerpo.add(pie, BorderLayout.SOUTH);
        main.add(cuerpo, BorderLayout.CENTER);
        setContentPane(main);

        btnDetalle.setEnabled(false);
        tabla.getSelectionModel().addListSelectionListener(e -> btnDetalle.setEnabled(tabla.getSelectedRow() >= 0));
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.rowAtPoint(e.getPoint()) >= 0) {
                    abrirSeleccion();
                }
            }
        });
        tabla.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "abrirGasto");
        tabla.getActionMap().put("abrirGasto", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                abrirSeleccion();
            }
        });
        btnDetalle.addActionListener(e -> abrirSeleccion());
        btnAgregarGasto.addActionListener(e -> {
            NuevoGastoForm form = new NuevoGastoForm(nombreHogar, dia);
            form.setVentanaGastos(this);
            form.setVisible(true);
            setVisible(false);
        });
        btnCerrarDia.addActionListener(e -> cerrarDia());
    }

    private void cerrarDia() {
        if (ventanaJefe != null) {
            if (JOptionPane.showConfirmDialog(this, "¿Cerrar el día " + dia + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                GastoDAO.cerrarDia(idHogar, dia);
                JOptionPane.showMessageDialog(this, "Día cerrado.");
                ventanaJefe.setVisible(true);
                dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            CerrarDiaForm form = new CerrarDiaForm(nombreHogar, dia, detalleHogarForm);
            form.setVisible(true);
            dispose();
        }
    }

    private void volver() {
        if (ventanaJefe != null && ventanaJefe.isDisplayable()) {
            ventanaJefe.setVisible(true);
        } else if (detalleHogarForm != null && detalleHogarForm.isDisplayable()) {
            detalleHogarForm.setVisible(true);
        } else {
            new AccesoForm().setVisible(true);
        }
        dispose();
    }

    public void cargarGastos() {
        modelo.setRowCount(0);
        gastos = List.of();
        BigDecimal total = BigDecimal.ZERO;
        try {
            gastos = GastoDAO.listar(idHogar, dia);
            if (ventanaJefe != null) {
                String motivo = GastoDAO.motivoSoloLectura(idHogar, dia);
                btnAgregarGasto.setEnabled(motivo.isEmpty());
                btnCerrarDia.setEnabled(motivo.isEmpty());
                lblNota.setText(motivo.isEmpty() ? "Puedes cerrar el día aunque no haya gastos." : motivo);
            } else {
                lblNota.setText("Selecciona un gasto para consultar su detalle.");
            }
            for (String[] gasto : gastos) {
                BigDecimal valor = gasto[5] == null ? BigDecimal.ZERO : new BigDecimal(gasto[5]);
                total = total.add(valor);
                modelo.addRow(new Object[]{gasto[0], gasto[1], gasto[2], gasto[3], gasto[4], valor});
            }
            vistaLista.show(contenidoLista, gastos.isEmpty() ? "vacio" : "lista");
            lblCantidad.setText(gastos.size() + (gastos.size() == 1 ? " gasto" : " gastos"));
            lblTotal.setText("$ " + NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO")).format(total));
        } catch (SQLException | NumberFormatException ex) {
            btnAgregarGasto.setEnabled(false);
            btnCerrarDia.setEnabled(false);
            btnDetalle.setEnabled(false);
            gastos = List.of();
            modelo.setRowCount(0);
            lblTotal.setText("—");
            lblCantidad.setText("Sin información");
            lblNota.setText("No se pudieron consultar los gastos.");
            vistaLista.show(contenidoLista, "vacio");
            JOptionPane.showMessageDialog(this, "No se pudieron cargar los gastos. " + ex.getMessage(),
                    "Oracle", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila >= 0) {
            abrirDetalle(tabla.convertRowIndexToModel(fila));
        }
    }

    void abrirGasto(int id) {
        for (int i = 0; i < gastos.size(); i++) {
            if (Integer.toString(id).equals(gastos.get(i)[8])) {
                abrirDetalle(i);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "El gasto señalado ya no aparece en este día.");
    }

    private void abrirDetalle(int indice) {
        DetalleGastoForm detalle = new DetalleGastoForm(this, nombreHogar, dia, indice, gastos.get(indice));
        detalle.setVisible(true);
        setVisible(false);
    }

    private static final class ValorRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 9, 0, 9));
            setBackground(seleccionado ? tabla.getSelectionBackground()
                    : fila % 2 == 0 ? Color.WHITE : new Color(246, 250, 252));
            setForeground(EstiloInterfaz.TINTA);
            if (valor instanceof BigDecimal monto) {
                setText("$ " + NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO")).format(monto));
            }
            return this;
        }
    }
}
