package presentacion;

import negocio.UsuarioResumen;
import persistencia.RolesDAO;
import persistencia.SesionActual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.border.EmptyBorder;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdministradorForm extends JFrame {
    private final int idAdministrador;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Rol", "ID", "Nombre", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public AdministradorForm(int id, String nombre) {
        idAdministrador = id;
        setTitle("Administrador - " + nombre);
        EstiloInterfaz.tamanoTableta(this);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EstiloInterfaz.FONDO);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(EstiloInterfaz.TINTA);
        cabecera.add(EstiloInterfaz.encabezado("Registro de hogares", "Administración · " + nombre),
                BorderLayout.CENTER);
        JButton salir = EstiloInterfaz.boton("Cerrar sesión", false);
        JPanel salida = EstiloInterfaz.filaBotones();
        salida.setBorder(new EmptyBorder(24, 0, 15, 20));
        salida.add(salir);
        cabecera.add(salida, BorderLayout.EAST);
        panel.add(cabecera, BorderLayout.NORTH);
        salir.addActionListener(e -> {
            SesionActual.salir();
            new AccesoForm().setVisible(true);
            dispose();
        });

        CoordinadorForm hogares = new CoordinadorForm(this, id);
        SupervisorForm encuestas = new SupervisorForm(id);
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(EstiloInterfaz.fuente(Font.BOLD, 14));
        pestanas.setForeground(EstiloInterfaz.TINTA);
        pestanas.setBackground(Color.WHITE);
        pestanas.addTab("Hogares", conMargen(hogares));
        pestanas.addTab("Usuarios", conMargen(crearPanelUsuarios()));
        pestanas.addTab("Revisión de encuestas", conMargen(encuestas));
        JPanel cuerpo = new JPanel(new BorderLayout());
        cuerpo.setBackground(EstiloInterfaz.FONDO);
        cuerpo.setBorder(new EmptyBorder(15, 22, 15, 22));
        cuerpo.add(pestanas);
        panel.add(cuerpo, BorderLayout.CENTER);
        add(panel);
        pestanas.addChangeListener(e -> {
            switch (pestanas.getSelectedIndex()) {
                case 0 -> hogares.cargar();
                case 1 -> cargar();
                case 2 -> encuestas.cargar();
            }
        });
        hogares.cargar();
    }

    private JPanel conMargen(JPanel contenido) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EstiloInterfaz.FONDO);
        panel.setBorder(new EmptyBorder(14, 12, 10, 12));
        panel.add(contenido);
        return panel;
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);
        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(EstiloInterfaz.titulo("Cuentas registradas"), BorderLayout.WEST);
        JButton crear = EstiloInterfaz.boton("Crear encuestador", true);
        barra.add(crear, BorderLayout.EAST);
        panel.add(barra, BorderLayout.NORTH);
        panel.add(EstiloInterfaz.tabla(tabla), BorderLayout.CENTER);

        JButton eliminar = EstiloInterfaz.boton("Eliminar usuario", false);
        eliminar.setForeground(new Color(170, 35, 45));
        eliminar.setVisible(false);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            String rol = fila < 0 ? "" : String.valueOf(
                    modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0));
            eliminar.setVisible("Encuestador".equals(rol) || "Jefe de hogar".equals(rol));
        });
        JPanel acciones = EstiloInterfaz.filaBotones();
        acciones.add(eliminar);
        panel.add(acciones, BorderLayout.SOUTH);
        eliminar.addActionListener(e -> eliminarSeleccionado());

        crear.addActionListener(e -> new RegistroRolForm(this, idAdministrador, this::cargar).setVisible(true));
        return panel;
    }

    private void cargar() {
        modelo.setRowCount(0);
        try {
            for (UsuarioResumen usuario : RolesDAO.listarUsuarios(idAdministrador)) {
                modelo.addRow(new Object[]{usuario.rol(), usuario.id(), usuario.nombre(), usuario.estado()});
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona el usuario que deseas eliminar.");
            return;
        }
        int indice = tabla.convertRowIndexToModel(fila);
        UsuarioResumen usuario = new UsuarioResumen(
                (Integer) modelo.getValueAt(indice, 1), (String) modelo.getValueAt(indice, 2),
                (String) modelo.getValueAt(indice, 0), (String) modelo.getValueAt(indice, 3));
        if ("Administrador".equals(usuario.rol())) {
            JOptionPane.showMessageDialog(this, "La cuenta de administrador no se puede eliminar.");
            return;
        }
        String mensaje = "¿Estás seguro de eliminar al " + usuario.rol() + " " + usuario.nombre()
                + "?\nSe conservarán los hogares y registros históricos relacionados.";
        if (JOptionPane.showConfirmDialog(this, mensaje, "Eliminar usuario",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            RolesDAO.eliminarUsuario(idAdministrador, usuario);
            cargar();
            JOptionPane.showMessageDialog(this, "El acceso del usuario fue eliminado.");
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
