package presentacion;

import negocio.EncuestadorAsignable;
import negocio.HogarAsignable;
import negocio.JefeDisponible;
import persistencia.HogarDAO;
import persistencia.RolesDAO;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Hogares y sus cuentas de jefe de hogar en el panel de administración. */
final class CoordinadorForm extends JPanel {
    private final int idAdministrador;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID hogar", "Familia", "Municipio", "Encuestador", "Cuenta del jefe"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private List<HogarAsignable> hogares = List.of();
    private final JButton asignar = EstiloInterfaz.boton("Asignar encuestador", false);
    private final JButton acceso = EstiloInterfaz.boton("Configurar acceso del jefe", false);
    private final JButton eliminar = EstiloInterfaz.boton("Eliminar hogar", false);

    CoordinadorForm(JFrame ventana, int idAdministrador) {
        super(new BorderLayout(0, 14));
        this.idAdministrador = idAdministrador;
        setOpaque(false);

        JPanel barra = new JPanel(new BorderLayout(8, 0));
        barra.setOpaque(false);
        barra.add(EstiloInterfaz.titulo("Hogares registrados"), BorderLayout.WEST);
        JButton registrar = EstiloInterfaz.boton("Registrar hogar", true);
        barra.add(registrar, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);
        add(EstiloInterfaz.tabla(tabla), BorderLayout.CENTER);
        tabla.getColumnModel().getColumn(0).setMaxWidth(105);

        JPanel acciones = EstiloInterfaz.filaBotones();
        eliminar.setForeground(new Color(170, 35, 45));
        acciones.add(eliminar);
        acciones.add(acceso);
        acciones.add(asignar);
        add(acciones, BorderLayout.SOUTH);

        registrar.addActionListener(e -> {
            try {
                RegistroHogarForm.mostrar(ventana, idAdministrador, nuevo -> {
                    cargar();
                    seleccionarHogar(nuevo);
                });
            } catch (SQLException ex) {
                mostrarError(ex);
            }
        });
        asignar.addActionListener(e -> asignar());
        eliminar.addActionListener(e -> eliminarHogar());
        acceso.addActionListener(e -> configurarAcceso());
        tabla.getSelectionModel().addListSelectionListener(e -> actualizarAcciones());
        actualizarAcciones();
    }

    void cargar() {
        HogarAsignable anterior = hogarSeleccionado();
        int seleccionado = anterior == null ? -1 : anterior.id();
        try {
            List<HogarAsignable> nuevos = HogarDAO.listarHogaresParaAsignar();
            modelo.setRowCount(0);
            hogares = nuevos;
            for (HogarAsignable hogar : hogares) {
                modelo.addRow(new Object[]{hogar.id(), hogar.familia(), hogar.municipio(),
                    hogar.encuestador() == null ? "Sin asignar" : hogar.encuestador(),
                    hogar.jefeConCuenta() ? "Creada" : "Pendiente"});
            }
            seleccionarHogar(seleccionado);
        } catch (SQLException ex) {
            mostrarError(ex);
        }
        actualizarAcciones();
    }

    private void seleccionarHogar(int id) {
        for (int fila = 0; fila < hogares.size(); fila++) {
            if (hogares.get(fila).id() == id) {
                int visible = tabla.convertRowIndexToView(fila);
                tabla.setRowSelectionInterval(visible, visible);
                tabla.scrollRectToVisible(tabla.getCellRect(visible, 0, true));
                return;
            }
        }
    }

    private HogarAsignable hogarSeleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : hogares.get(tabla.convertRowIndexToModel(fila));
    }

    private void actualizarAcciones() {
        HogarAsignable hogar = hogarSeleccionado();
        asignar.setEnabled(hogar != null);
        eliminar.setEnabled(hogar != null);
        asignar.setText(hogar != null && hogar.idEncuestador() != null
                ? "Cambiar encuestador" : "Asignar encuestador");
        acceso.setEnabled(hogar != null && !hogar.jefeConCuenta());
    }

    private void eliminarHogar() {
        HogarAsignable hogar = hogarSeleccionado();
        if (hogar == null) {
            return;
        }
        String mensaje = "¿Eliminar el hogar " + hogar.familia() + " (ID " + hogar.id() + ")?\n\n"
                + "Se eliminarán sus integrantes y la cuenta del jefe, sus encuestas, días, "
                + "gastos, observaciones y visitas.\nEsta acción no se puede deshacer.";
        if (JOptionPane.showConfirmDialog(this, mensaje, "Eliminar hogar",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            HogarDAO.eliminarHogar(idAdministrador, hogar.id(), hogar.familia());
            cargar();
            JOptionPane.showMessageDialog(this, "Hogar eliminado.");
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void asignar() {
        HogarAsignable hogar = hogarSeleccionado();
        if (hogar == null) {
            return;
        }
        try {
            List<EncuestadorAsignable> encuestadores = HogarDAO.listarEncuestadoresParaAsignar();
            if (encuestadores.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Crea una cuenta de encuestador en Usuarios.");
                return;
            }

            JComboBox<EncuestadorAsignable> opciones = new JComboBox<>(encuestadores.toArray(EncuestadorAsignable[]::new));
            opciones.setSelectedIndex(-1);
            for (EncuestadorAsignable encuestador : encuestadores) {
                if (Integer.valueOf(encuestador.id()).equals(hogar.idEncuestador())) {
                    opciones.setSelectedItem(encuestador);
                    break;
                }
            }
            Object[] campos = {EstiloInterfaz.nota(hogar.familia()), EstiloInterfaz.campo("Encuestador", opciones)};
            if (JOptionPane.showConfirmDialog(this, campos, asignar.getText(), JOptionPane.OK_CANCEL_OPTION)
                    != JOptionPane.OK_OPTION) {
                return;
            }
            EncuestadorAsignable elegido = (EncuestadorAsignable) opciones.getSelectedItem();
            if (elegido == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un encuestador.");
                return;
            }
            if (!Integer.valueOf(elegido.id()).equals(hogar.idEncuestador())) {
                HogarDAO.asignarHogar(idAdministrador, hogar.id(), elegido.id(), hogar.idEncuestador());
                cargar();
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void configurarAcceso() {
        HogarAsignable hogar = hogarSeleccionado();
        if (hogar == null || hogar.jefeConCuenta()) {
            return;
        }
        try {
            List<JefeDisponible> candidatos = RolesDAO.listarJefesSinCuenta(idAdministrador, hogar.id());
            if (candidatos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Este hogar no tiene una persona registrada que pueda recibir la cuenta.");
                return;
            }

            JComboBox<JefeDisponible> personas = new JComboBox<>(candidatos.toArray(JefeDisponible[]::new));
            JTextField documento = new JTextField(20);
            JPasswordField clave = new JPasswordField(20);
            JPasswordField confirmar = new JPasswordField(20);
            personas.setSelectedIndex(-1);
            personas.addActionListener(e -> {
                JefeDisponible jefe = (JefeDisponible) personas.getSelectedItem();
                String guardado = jefe == null ? null : jefe.documento();
                documento.setText(guardado == null ? "" : guardado);
                documento.setEditable(guardado == null);
            });
            if (candidatos.size() == 1) {
                personas.setSelectedIndex(0);
            }

            JPanel campos = new JPanel(new GridLayout(0, 1, 0, 8));
            campos.add(EstiloInterfaz.nota(hogar.familia()));
            campos.add(EstiloInterfaz.campo("Jefe de hogar", personas));
            campos.add(EstiloInterfaz.campo("Documento", documento));
            campos.add(EstiloInterfaz.campo("Contraseña · mínimo 8 caracteres", clave));
            campos.add(EstiloInterfaz.campo("Confirmar contraseña", confirmar));
            while (JOptionPane.showConfirmDialog(this, campos, "Configurar acceso del jefe",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                char[] secreto = clave.getPassword();
                char[] confirmacion = confirmar.getPassword();
                try {
                    JefeDisponible jefe = (JefeDisponible) personas.getSelectedItem();
                    if (jefe == null || !Arrays.equals(secreto, confirmacion)) {
                        throw new IllegalArgumentException(jefe == null ? "Selecciona al jefe de hogar."
                                : "Las contraseñas no coinciden.");
                    }
                    RolesDAO.registrarJefe(idAdministrador, hogar.id(), jefe.id(), documento.getText().trim(),
                            new String(secreto));
                    cargar();
                    JOptionPane.showMessageDialog(this, "La cuenta ya puede iniciar sesión.");
                    return;
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    Arrays.fill(secreto, '\0');
                    Arrays.fill(confirmacion, '\0');
                    clave.setText("");
                    confirmar.setText("");
                }
            }
        } catch (SQLException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }
}
