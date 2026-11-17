package presentacion;

import negocio.DatosInformeEncuesta;
import negocio.EncuestaResumen;
import negocio.GastoRevision;
import persistencia.CasosDAO;
import persistencia.InformeEncuesta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.List;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Panel de revisión de encuestas del administrador. */
final class SupervisorForm extends JPanel {
    private final int idAdministrador;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID encuesta", "Hogar", "Familia", "Estado", "Administrador asignado"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final JLabel seleccion = EstiloInterfaz.nota("Selecciona una encuesta.");
    private final JLabel conteo = EstiloInterfaz.nota("");

    private final JButton ver = EstiloInterfaz.boton("Ver gastos", false);
    private final JButton corregir = EstiloInterfaz.boton("Corregir gasto", false);
    private final JButton cerrar = EstiloInterfaz.boton("Cerrar encuesta", true);
    private final JButton informe = EstiloInterfaz.boton("Generar informe", true);

    SupervisorForm(int idAdministrador) {
        super(new BorderLayout(0, 14));
        this.idAdministrador = idAdministrador;
        setOpaque(false);
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setOpaque(false);
        encabezado.add(EstiloInterfaz.titulo("Revisión de encuestas"), BorderLayout.WEST);
        encabezado.add(conteo, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        JScrollPane listado = EstiloInterfaz.tabla(tabla);
        tabla.setRowHeight(40);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(70);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(190);
        tabla.getColumnModel().getColumn(3).setCellRenderer(new EstadoRenderer());
        add(listado, BorderLayout.CENTER);

        JPanel acciones = EstiloInterfaz.tarjeta(new BorderLayout(0, 12));
        seleccion.setFont(EstiloInterfaz.fuente(Font.BOLD, 13));
        acciones.add(seleccion, BorderLayout.NORTH);
        JPanel botones = new JPanel(new GridLayout(2, 2, 8, 8));
        botones.setOpaque(false);
        botones.add(ver);
        botones.add(corregir);
        botones.add(cerrar);
        botones.add(informe);
        acciones.add(botones, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);

        tabla.getSelectionModel().addListSelectionListener(e -> actualizarAcciones());
        ver.addActionListener(e -> ejecutarSobreEncuesta(this::verGastos));
        corregir.addActionListener(e -> ejecutarSobreEncuesta(this::corregirGasto));
        cerrar.addActionListener(e -> ejecutarSobreEncuesta(this::cerrar));
        informe.addActionListener(e -> ejecutarSobreEncuesta(this::generarInforme));
        actualizarAcciones();
    }

    private void actualizarAcciones() {
        int fila = tabla.getSelectedRow();
        Object valor = fila < 0 ? null : modelo.getValueAt(tabla.convertRowIndexToModel(fila), 3);
        String estado = valor == null ? "" : valor.toString().strip().toUpperCase(Locale.ROOT);
        if (fila < 0) {
            seleccion.setText("Selecciona una encuesta.");
        } else {
            int indice = tabla.convertRowIndexToModel(fila);
            seleccion.setText("Encuesta " + modelo.getValueAt(indice, 0) + " · "
                    + modelo.getValueAt(indice, 2));
        }
        ver.setEnabled(fila >= 0);
        corregir.setEnabled(fila >= 0 && CasosDAO.puedeCorregir(estado));
        cerrar.setEnabled("TRANSMITIDO".equals(estado));
        informe.setEnabled("CERRADO".equals(estado));
    }

    void cargar() {
        int filaSeleccionada = tabla.getSelectedRow();
        Object seleccion = filaSeleccionada < 0 ? null
                : modelo.getValueAt(tabla.convertRowIndexToModel(filaSeleccionada), 0);
        try {
            List<EncuestaResumen> encuestas = CasosDAO.listarEncuestas(idAdministrador);
            modelo.setRowCount(0);
            conteo.setText(encuestas.size() + (encuestas.size() == 1 ? " encuesta" : " encuestas"));
            for (EncuestaResumen encuesta : encuestas) {
                modelo.addRow(new Object[]{encuesta.id(), encuesta.hogar(), encuesta.familia(),
                    encuesta.estado(), encuesta.administrador()});
            }
            for (int fila = 0; fila < modelo.getRowCount(); fila++) {
                if (modelo.getValueAt(fila, 0).equals(seleccion)) {
                    int visible = tabla.convertRowIndexToView(fila);
                    tabla.setRowSelectionInterval(visible, visible);
                    break;
                }
            }
        } catch (SQLException ex) {
            error(ex);
        }
        actualizarAcciones();
    }

    @FunctionalInterface
    private interface AccionEncuesta {
        void ejecutar(int caso) throws SQLException;
    }

    private void ejecutarSobreEncuesta(AccionEncuesta accion) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una encuesta.");
            return;
        }
        int id = (Integer) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        try {
            accion.ejecutar(id);
        } catch (SQLException ex) {
            error(ex);
        }
    }

    private List<GastoRevision> listarGastos(int encuesta) throws SQLException {
        return CasosDAO.listarGastosRevision(idAdministrador, encuesta);
    }

    private void verGastos(int encuesta) throws SQLException {
        List<GastoRevision> gastos = listarGastos(encuesta);
        DefaultTableModel datos = new DefaultTableModel(
                new Object[]{"Día", "Artículo", "Cantidad", "Unidad", "Valor (COP)"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        for (GastoRevision gasto : gastos) {
            datos.addRow(new Object[]{gasto.dia(), gasto.articulo(), gasto.cantidad(),
                    gasto.unidad(), gasto.valor()});
        }
        JTable tablaGastos = new JTable(datos);
        JScrollPane scroll = EstiloInterfaz.tabla(tablaGastos);
        scroll.setPreferredSize(new java.awt.Dimension(710, 360));
        JPanel contenido = new JPanel(new BorderLayout(0, 10));
        contenido.setBackground(EstiloInterfaz.FONDO);
        contenido.setBorder(new EmptyBorder(14, 14, 14, 14));
        contenido.add(EstiloInterfaz.nota(gastos.isEmpty() ? "No hay gastos registrados."
                : gastos.size() + (gastos.size() == 1 ? " gasto registrado" : " gastos registrados")),
                BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, contenido, "Gastos de la encuesta " + encuesta,
                JOptionPane.PLAIN_MESSAGE);
    }

    private void corregirGasto(int encuesta) throws SQLException {
        if (CorreccionGastoForm.mostrar(this, encuesta, idAdministrador, listarGastos(encuesta))) {
            cargar();
            JOptionPane.showMessageDialog(this, "Corrección guardada.");
        }
    }

    private void cerrar(int encuesta) throws SQLException {
        if (JOptionPane.showConfirmDialog(this, "¿Cerrar esta encuesta?",
                "Confirmar cierre", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        CasosDAO.cerrar(encuesta, idAdministrador);
        cargar();
        JOptionPane.showMessageDialog(this, "Encuesta cerrada.");
    }

    private void generarInforme(int encuesta) throws SQLException {
        DatosInformeEncuesta datos = InformeEncuesta.cargar(encuesta, idAdministrador);
        JFileChooser archivos = new JFileChooser();
        archivos.setDialogTitle("Guardar informe de encuesta");
        archivos.setFileFilter(new FileNameExtensionFilter("Informe PDF", "pdf"));
        archivos.setAcceptAllFileFilterUsed(false);
        archivos.setSelectedFile(new File("Encuesta_" + encuesta + ".pdf"));
        if (archivos.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path archivo = archivos.getSelectedFile().toPath().toAbsolutePath();
        if (!archivo.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            archivo = archivo.resolveSibling(archivo.getFileName() + ".pdf");
        }
        boolean reemplazar = Files.exists(archivo);
        if (reemplazar && JOptionPane.showConfirmDialog(this, "¿Reemplazar " + archivo.getFileName() + "?",
                "Guardar informe", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            guardarArchivo(archivo, datos, reemplazar);
            JOptionPane.showMessageDialog(this, "Informe guardado en\n" + archivo);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el informe. " + ex.getMessage(),
                    "Guardar informe", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarArchivo(Path archivo, DatosInformeEncuesta datos, boolean reemplazar) throws IOException {
        Path provisional = Files.createTempFile(archivo.getParent(), ".informe-", ".tmp");
        try {
            InformeEncuestaPdf.guardar(datos, provisional);
            if (reemplazar) {
                try {
                    Files.move(provisional, archivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(provisional, archivo, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.move(provisional, archivo);
            }
        } finally {
            Files.deleteIfExists(provisional);
        }
    }

    private static final class EstadoRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            String estado = valor == null ? "" : valor.toString().strip().toUpperCase(Locale.ROOT);
            setText(switch (estado) {
                case "EN_PROGRESO" -> "En progreso";
                case "EN_CORRECCION" -> "En corrección";
                case "TRANSMITIDO" -> "Transmitida";
                case "CERRADO" -> "Cerrada";
                default -> valor == null ? "En progreso" : valor.toString();
            });
            setForeground(seleccionado ? EstiloInterfaz.TINTA : switch (estado) {
                case "TRANSMITIDO" -> new Color(17, 99, 91);
                case "CERRADO" -> new Color(37, 78, 124);
                case "EN_CORRECCION" -> new Color(150, 93, 20);
                default -> EstiloInterfaz.SECUNDARIO;
            });
            setBackground(seleccionado ? tabla.getSelectionBackground()
                    : fila % 2 == 0 ? Color.WHITE : new Color(246, 250, 252));
            setFont(EstiloInterfaz.fuente(Font.BOLD, 13));
            setBorder(new EmptyBorder(0, 9, 0, 9));
            return this;
        }
    }

    private void error(SQLException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }
}
