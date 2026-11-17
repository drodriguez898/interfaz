package presentacion;

import negocio.Articulo;
import negocio.CorreccionGasto;
import negocio.DatosEditables;
import negocio.GastoRevision;
import persistencia.ArticuloDAO;
import persistencia.CasosDAO;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

final class CorreccionGastoForm {
    private CorreccionGastoForm() {}

    static boolean mostrar(Component padre, int encuesta, int administrador, List<GastoRevision> gastos)
            throws SQLException {
        if (gastos.isEmpty()) {
            JOptionPane.showMessageDialog(padre, "Esta encuesta todavía no tiene gastos registrados.");
            return false;
        }
        JComboBox<GastoRevision> seleccion = new JComboBox<>(gastos.toArray(GastoRevision[]::new));
        seleccion.setPrototypeDisplayValue(new GastoRevision(1234, 14, "Nombre del artículo registrado", null, null, null, null));
        if (JOptionPane.showConfirmDialog(padre, EstiloInterfaz.campo("Gasto que se va a corregir", seleccion),
                "Corregir gasto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return false;
        }

        GastoRevision gasto = (GastoRevision) seleccion.getSelectedItem();
        DatosEditables original = CasosDAO.cargarCorreccion(encuesta, administrador, gasto.id());
        if (!Objects.equals(gasto.articuloId(), original.articuloId())) {
            throw new SQLException("El gasto cambió. Vuelve a abrirlo para corregirlo.");
        }
        String pendientes = CasosDAO.notasPendientesDelGasto(encuesta, administrador, gasto.id());
        JComboBox<String> articulos = new JComboBox<>();
        String nombreOriginal = gasto.articulo();
        if (nombreOriginal == null) {
            articulos.addItem("Sin artículo");
        }
        for (Articulo articulo : new ArticuloDAO().listar()) {
            articulos.addItem(articulo.nombre());
        }
        if (nombreOriginal != null && ((DefaultComboBoxModel<String>) articulos.getModel()).getIndexOf(nombreOriginal) < 0) {
            articulos.addItem(nombreOriginal);
        }
        articulos.setSelectedItem(nombreOriginal == null ? "Sin artículo" : nombreOriginal);
        articulos.setPrototypeDisplayValue("Artículo seleccionado del catálogo");

        JTextField cantidad = new JTextField(numero(original.cantidad()), 12);
        UnidadMedidaPanel unidad = new UnidadMedidaPanel();
        unidad.seleccionar(original.unidad());
        JTextField valor = new JTextField(numero(original.valor()), 12);
        JTextField adquisicion = new JTextField(original.adquisicion(), 20);
        JTextField frecuencia = new JTextField(original.frecuencia(), 18);
        JTextField tipo = new JTextField(original.tipo(), 20);
        JTextField lugar = new JTextField(original.lugar(), 20);
        JTextArea nota = new JTextArea(3, 30);
        nota.setLineWrap(true);
        nota.setWrapStyleWord(true);

        JPanel datos = new JPanel(new GridLayout(0, 2, 12, 8));
        datos.add(EstiloInterfaz.campo("Cantidad", cantidad));
        datos.add(unidad);
        datos.add(EstiloInterfaz.campo("Valor en COP", valor));
        datos.add(EstiloInterfaz.campo("Forma de adquisición", adquisicion));
        datos.add(EstiloInterfaz.campo("Frecuencia", frecuencia));
        datos.add(EstiloInterfaz.campo("Tipo de gasto", tipo));
        datos.add(EstiloInterfaz.campo("Lugar de compra", lugar));

        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.add(EstiloInterfaz.nota("Día " + gasto.dia() + " · Gasto " + gasto.id()), BorderLayout.NORTH);
        cabecera.add(EstiloInterfaz.campo("Artículo", articulos), BorderLayout.CENTER);
        JPanel observacion = new JPanel(new BorderLayout(0, 4));
        observacion.add(EstiloInterfaz.nota("Nota de la corrección (opcional)"), BorderLayout.NORTH);
        observacion.add(new JScrollPane(nota), BorderLayout.CENTER);
        if (!pendientes.isBlank()) {
            JTextArea solicitudes = new JTextArea(pendientes, 3, 30);
            solicitudes.setEditable(false);
            solicitudes.setLineWrap(true);
            solicitudes.setWrapStyleWord(true);
            JPanel historial = new JPanel(new BorderLayout(0, 4));
            historial.add(EstiloInterfaz.nota("Al guardar se resuelven estas solicitudes del gasto."),
                    BorderLayout.NORTH);
            historial.add(new JScrollPane(solicitudes), BorderLayout.CENTER);
            cabecera.add(historial, BorderLayout.SOUTH);
        }
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(datos, BorderLayout.CENTER);
        panel.add(observacion, BorderLayout.SOUTH);

        String[] opciones = {"Guardar corrección", "Cancelar"};
        while (JOptionPane.showOptionDialog(padre, panel, "Corregir gasto", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]) == 0) {
            try {
                String articulo = (String) articulos.getSelectedItem();
                if (nombreOriginal == null && Objects.equals("Sin artículo", articulo)) {
                    articulo = null;
                }
                if (unidad.valor().isBlank()) {
                    throw new SQLException("Selecciona el tipo de medida y la unidad.");
                }
                CorreccionGasto cambios = new CorreccionGasto(articulo, cantidad.getText(), unidad.valor(),
                        adquisicion.getText(), valor.getText(), frecuencia.getText(), tipo.getText(),
                        lugar.getText(), nota.getText());
                CasosDAO.corregir(encuesta, administrador, gasto, original, cambios);
                return true;
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(padre, ex.getMessage(), "Corregir gasto", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private static String numero(BigDecimal valor) {
        return valor == null ? "" : valor.toPlainString().replace('.', ',');
    }
}
