package presentacion;

import negocio.ObservacionPendiente;
import negocio.Rol;
import persistencia.CasosDAO;
import persistencia.GastoDAO;
import persistencia.HogarDAO;
import persistencia.SesionActual;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class JefeHogarForm extends JFrame {
    private final int hogar;
    private final JComboBox<Integer> dias = new JComboBox<>();
    private final JButton abrir = EstiloInterfaz.boton("Abrir gastos", true);
    private final JLabel familia = EstiloInterfaz.titulo("Mi hogar");
    private final JLabel avance = EstiloInterfaz.nota("");
    private final JProgressBar progreso = new JProgressBar(0, 14);

    public JefeHogarForm(int idEncuestado, int hogar, String nombre) {
        if (!SesionActual.es(Rol.JEFE_HOGAR) || SesionActual.id() != idEncuestado) {
            throw new SecurityException("La cuenta de esta ventana no coincide con la sesión activa.");
        }
        this.hogar = hogar;
        setTitle("Jefe de hogar - " + nombre);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel base = new JPanel(new BorderLayout());
        base.setBackground(EstiloInterfaz.FONDO);
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(EstiloInterfaz.TINTA);
        cabecera.add(EstiloInterfaz.encabezado("Mi hogar", "Jefe de hogar · " + nombre), BorderLayout.CENTER);
        JButton salir = EstiloInterfaz.boton("Cerrar sesión", false);
        JPanel salida = EstiloInterfaz.filaBotones();
        salida.setBorder(new EmptyBorder(24, 0, 15, 20));
        salida.add(salir);
        cabecera.add(salida, BorderLayout.EAST);
        base.add(cabecera, BorderLayout.NORTH);

        try {
            String nombreFamilia = HogarDAO.consultarNombreFamilia(hogar);
            familia.setText(nombreFamilia == null || nombreFamilia.isBlank()
                    ? "Hogar " + hogar : "Familia " + nombreFamilia);
        } catch (SQLException ex) {
            familia.setText("Hogar " + hogar);
            error(ex);
        }

        JPanel resumen = EstiloInterfaz.tarjeta(new BorderLayout(0, 10));
        resumen.add(familia, BorderLayout.NORTH);
        progreso.setPreferredSize(new Dimension(200, 22));
        progreso.setMinimumSize(new Dimension(100, 22));
        progreso.setStringPainted(true);
        progreso.setForeground(new java.awt.Color(0, 105, 97));
        progreso.setFont(EstiloInterfaz.fuente(Font.BOLD, 12));
        JPanel estado = new JPanel(new GridLayout(2, 1, 0, 7));
        estado.setOpaque(false);
        estado.add(avance);
        estado.add(progreso);
        resumen.add(estado, BorderLayout.CENTER);
        resumen.setPreferredSize(new Dimension(600, 145));

        JPanel seleccion = EstiloInterfaz.tarjeta(new GridLayout(0, 1, 0, 10));
        seleccion.add(EstiloInterfaz.titulo("Gastos diarios"));
        seleccion.add(EstiloInterfaz.campo("Días disponibles", dias));
        seleccion.add(abrir);

        JPanel correcciones = EstiloInterfaz.tarjeta(new BorderLayout(0, 10));
        correcciones.add(EstiloInterfaz.titulo("Correcciones"), BorderLayout.NORTH);
        JButton observaciones = EstiloInterfaz.boton("Ver correcciones solicitadas", false);
        correcciones.add(observaciones, BorderLayout.CENTER);

        JPanel tarjetas = new JPanel();
        tarjetas.setOpaque(false);
        tarjetas.setLayout(new BoxLayout(tarjetas, BoxLayout.Y_AXIS));
        for (JPanel tarjeta : new JPanel[]{resumen, seleccion, correcciones}) {
            tarjeta.setAlignmentX(LEFT_ALIGNMENT);
            tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getPreferredSize().height));
            tarjetas.add(tarjeta);
            tarjetas.add(Box.createVerticalStrut(12));
        }
        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setBackground(EstiloInterfaz.FONDO);
        contenido.setBorder(new EmptyBorder(18, 22, 18, 22));
        JScrollPane scroll = new JScrollPane(tarjetas);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        contenido.add(scroll, BorderLayout.CENTER);
        base.add(contenido, BorderLayout.CENTER);
        setContentPane(base);
        EstiloInterfaz.tamanoTableta(this);
        setLocationRelativeTo(null);

        abrir.addActionListener(e -> {
            Integer dia = (Integer) dias.getSelectedItem();
            if (dia != null) {
                abrirDia(dia, null);
            }
        });
        observaciones.addActionListener(e -> mostrarCorrecciones());
        salir.addActionListener(e -> {
            SesionActual.salir();
            new AccesoForm().setVisible(true);
            dispose();
        });
    }

    @Override public void setVisible(boolean visible) {
        if (visible) {
            actualizarDias();
        }
        super.setVisible(visible);
    }

    private void actualizarDias() {
        Integer seleccionado = (Integer) dias.getSelectedItem();
        dias.removeAllItems();
        abrir.setEnabled(false);
        dias.setEnabled(false);
        try {
            Set<Integer> cerrados = GastoDAO.diasCerrados(hogar);
            Integer[] disponibles = diasDisponibles(cerrados);
            for (int dia : disponibles) {
                dias.addItem(dia);
            }
            int cantidad = 14 - disponibles.length;
            if (seleccionado != null && !cerrados.contains(seleccionado)) {
                dias.setSelectedItem(seleccionado);
            }
            progreso.setValue(cantidad);
            progreso.setString(cantidad + " de 14 días cerrados");
            boolean disponible = dias.getItemCount() > 0;
            dias.setEnabled(disponible);
            abrir.setEnabled(disponible);
            avance.setText(disponible ? "Selecciona un día disponible." : "Todos los días están cerrados.");
        } catch (SQLException ex) {
            progreso.setValue(0);
            progreso.setString("Sin información");
            avance.setText("No se pudo consultar el estado de los días.");
            error(ex);
        }
    }

    static Integer[] diasDisponibles(Set<Integer> cerrados) {
        java.util.ArrayList<Integer> disponibles = new java.util.ArrayList<>();
        for (int dia = 1; dia <= 14; dia++) {
            if (!cerrados.contains(dia)) {
                disponibles.add(dia);
            }
        }
        return disponibles.toArray(Integer[]::new);
    }

    private void abrirDia(int dia, Integer gasto) {
        try {
            if (GastoDAO.diasCerrados(hogar).contains(dia)) {
                JOptionPane.showMessageDialog(this, "El día " + dia + " ya está cerrado.");
                actualizarDias();
                return;
            }
            GastosDiaForm ventana = new GastosDiaForm(hogar, familia.getText(), dia, this, true);
            ventana.setVisible(true);
            setVisible(false);
            if (gasto != null) {
                ventana.abrirGasto(gasto);
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void mostrarCorrecciones() {
        try {
            List<ObservacionPendiente> pendientes = CasosDAO.listarObservacionesPendientes(hogar);
            if (pendientes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tu hogar no tiene solicitudes pendientes.\n"
                        + "Las correcciones del administrador se guardan directamente en el gasto.",
                        "Correcciones solicitadas", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            JComboBox<ObservacionPendiente> opciones =
                    new JComboBox<>(pendientes.toArray(ObservacionPendiente[]::new));
            JTextArea comentario = new JTextArea(4, 38);
            comentario.setEditable(false);
            comentario.setLineWrap(true);
            comentario.setWrapStyleWord(true);
            JLabel indicacion = EstiloInterfaz.nota("");
            Runnable mostrar = () -> {
                ObservacionPendiente nota = (ObservacionPendiente) opciones.getSelectedItem();
                comentario.setText(nota.texto());
                comentario.setCaretPosition(0);
                indicacion.setText(nota.diaCerrado()
                        ? "<html>El día está cerrado. Informa al encuestador para que el administrador lo revise.</html>"
                        : "<html>Abre el gasto, corrígelo y cierra el día.</html>");
            };
            opciones.addActionListener(e -> mostrar.run());
            mostrar.run();
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.add(EstiloInterfaz.campo("Solicitud pendiente", opciones), BorderLayout.NORTH);
            panel.add(new JScrollPane(comentario), BorderLayout.CENTER);
            panel.add(indicacion, BorderLayout.SOUTH);
            String[] botones = {"Abrir gasto", "Cerrar"};
            if (JOptionPane.showOptionDialog(this, panel, "Correcciones solicitadas", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, botones, botones[0]) == 0) {
                ObservacionPendiente nota = (ObservacionPendiente) opciones.getSelectedItem();
                abrirDia(nota.dia(), nota.gasto());
            }
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Oracle", JOptionPane.ERROR_MESSAGE);
    }
}
