package presentacion;

import negocio.Rol;
import persistencia.GastoDAO;
import persistencia.HogarDAO;
import persistencia.SesionActual;

import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MisHogaresForm extends JFrame {
    private static final int DIAS_CICLO = 14;

    private final int idEncuestadorActual;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Hogar", "Familia", "Ubicación", "Días cerrados"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) {
            return false;
        }

        @Override public Class<?> getColumnClass(int columna) {
            return columna == 0 ? Integer.class : String.class;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel estado = EstiloInterfaz.nota("");
    private final JButton abrir = EstiloInterfaz.boton("Abrir hogar", true);

    public MisHogaresForm() {
        this(SesionActual.es(Rol.ENCUESTADOR) ? SesionActual.id() : 0, "Encuestador");
    }

    public MisHogaresForm(int idEncuestador) {
        this(idEncuestador, "Encuestador");
    }

    public MisHogaresForm(int idEncuestador, String nombre) {
        idEncuestadorActual = idEncuestador;
        setTitle("Mis hogares");
        EstiloInterfaz.tamanoTableta(this);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent evento) {
                cerrarSesion();
            }
        });

        JPanel base = new JPanel(new BorderLayout());
        base.setBackground(EstiloInterfaz.FONDO);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(EstiloInterfaz.TINTA);
        cabecera.add(EstiloInterfaz.encabezado("Registro de hogares", "Encuestador · " + nombre), BorderLayout.CENTER);
        JButton salir = EstiloInterfaz.boton("Cerrar sesión", false);
        JPanel sesion = EstiloInterfaz.filaBotones();
        sesion.setBorder(new EmptyBorder(24, 0, 15, 20));
        sesion.add(salir);
        cabecera.add(sesion, BorderLayout.EAST);
        base.add(cabecera, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(new EmptyBorder(19, 22, 18, 22));
        cuerpo.add(EstiloInterfaz.titulo("Mis hogares asignados"), BorderLayout.NORTH);
        cuerpo.add(EstiloInterfaz.tabla(tabla), BorderLayout.CENTER);
        tabla.getColumnModel().getColumn(0).setMaxWidth(100);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(165);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(280);
        tabla.getColumnModel().getColumn(3).setMaxWidth(140);
        JPanel pie = new JPanel(new BorderLayout(10, 0));
        pie.setOpaque(false);
        pie.add(estado, BorderLayout.CENTER);
        pie.add(abrir, BorderLayout.EAST);
        cuerpo.add(pie, BorderLayout.SOUTH);
        base.add(cuerpo, BorderLayout.CENTER);
        setContentPane(base);

        abrir.setEnabled(false);
        tabla.getSelectionModel().addListSelectionListener(e -> abrir.setEnabled(tabla.getSelectedRow() >= 0));
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2 && tabla.rowAtPoint(evento.getPoint()) >= 0) {
                    abrirHogar();
                }
            }
        });
        tabla.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "abrirHogar");
        tabla.getActionMap().put("abrirHogar", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent evento) {
                abrirHogar();
            }
        });
        abrir.addActionListener(e -> abrirHogar());
        salir.addActionListener(e -> cerrarSesion());
        if (idEncuestadorActual > 0) {
            cargarHogaresDesdeOracle();
        } else {
            estado.setText("Inicia sesión para consultar tus hogares.");
        }
    }

    public int getIdEncuestadorActual() {
        return idEncuestadorActual;
    }

    public void cargarHogaresDesdeOracle() {
        int seleccion = tabla.getSelectedRow();
        Object hogarSeleccionado = seleccion < 0 ? null : modelo.getValueAt(tabla.convertRowIndexToModel(seleccion), 0);
        try {
            List<Object[]> filas = new ArrayList<>();
            for (String[] fila : HogarDAO.listarHogaresPorEncuestador(idEncuestadorActual)) {
                int hogar = Integer.parseInt(fila[0]);
                int cerrados = GastoDAO.diasCerrados(hogar).size();
                String direccion = fila[2] == null ? "" : fila[2].strip();
                String municipio = fila[3] == null ? "" : fila[3].strip();
                String ubicacion = direccion.isEmpty() ? municipio
                        : municipio.isEmpty() ? direccion : direccion + " · " + municipio;
                filas.add(new Object[]{hogar, fila[1], ubicacion, cerrados + " de " + DIAS_CICLO});
            }
            modelo.setRowCount(0);
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                if (modelo.getValueAt(fila, 0).equals(hogarSeleccionado)) {
                    int visible = tabla.convertRowIndexToView(fila);
                    tabla.setRowSelectionInterval(visible, visible);
                    break;
                }
            }
            estado.setText(filas.isEmpty() ? "Aún no tienes hogares asignados."
                    : filas.size() + (filas.size() == 1 ? " hogar asignado" : " hogares asignados"));
        } catch (SQLException ex) {
            estado.setText("No se pudieron consultar los hogares.");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Consultar hogares", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirHogar() {
        int seleccion = tabla.getSelectedRow();
        if (seleccion < 0) {
            return;
        }
        int fila = tabla.convertRowIndexToModel(seleccion);
        DetalleHogarForm detalle = new DetalleHogarForm((Integer) modelo.getValueAt(fila, 0),
                (String) modelo.getValueAt(fila, 1), (String) modelo.getValueAt(fila, 2), this);
        detalle.setVisible(true);
        setVisible(false);
    }

    private void cerrarSesion() {
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar la sesión?", "Cerrar sesión",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            SesionActual.salir();
            new AccesoForm().setVisible(true);
            dispose();
        }
    }

    public void registrarNuevoHogar(String familia, String direccion, String municipio) {
        cargarHogaresDesdeOracle();
    }

}
