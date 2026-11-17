package presentacion;

import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

final class UnidadMedidaPanel extends JPanel {
    private final JComboBox<String> tipo = new JComboBox<>(
            new String[]{"Selecciona", "Líquido", "Peso", "Unidades", "Servicio"});
    private final JComboBox<String> unidad = new JComboBox<>();

    UnidadMedidaPanel() {
        super(new GridLayout(1, 2, 10, 0));
        setOpaque(false);
        add(EstiloInterfaz.campo("Tipo de medida *", tipo));
        add(EstiloInterfaz.campo("Unidad *", unidad));
        tipo.addActionListener(e -> actualizarOpciones());
        actualizarOpciones();
    }

    private void actualizarOpciones() {
        unidad.removeAllItems();
        unidad.addItem("");
        switch (tipo.getSelectedIndex()) {
            case 1 -> {
                unidad.addItem("Litro");
                unidad.addItem("Mililitro");
            }
            case 2 -> {
                unidad.addItem("Kilogramo");
                unidad.addItem("Gramos");
            }
            case 3 -> {
                unidad.addItem("Unidad");
                unidad.addItem("Paquete");
            }
            case 4 -> unidad.addItem("Servicio");
            default -> { }
        }
        unidad.setEnabled(tipo.getSelectedIndex() > 0);
    }

    String valor() {
        Object seleccionado = unidad.getSelectedItem();
        return seleccionado == null ? "" : seleccionado.toString();
    }

    void seleccionar(String valor) {
        if (valor == null || valor.isBlank()) {
            tipo.setSelectedIndex(0);
            return;
        }
        int indice = switch (valor.toLowerCase(java.util.Locale.ROOT).strip()) {
            case "litro", "litros", "mililitro", "mililitros", "l", "ml" -> 1;
            case "kilogramo", "kilogramos", "gramo", "gramos", "kg", "g" -> 2;
            case "unidad", "unidades", "paquete", "paquetes" -> 3;
            case "servicio" -> 4;
            default -> 5;
        };
        if (indice == 5) {
            if (tipo.getItemCount() == 5) {
                tipo.addItem("Unidad anterior");
            }
            tipo.setSelectedIndex(5);
            unidad.addItem(valor);
            unidad.setEnabled(true);
        } else {
            tipo.setSelectedIndex(indice);
            if (((javax.swing.DefaultComboBoxModel<String>) unidad.getModel()).getIndexOf(valor) < 0) {
                unidad.addItem(valor);
            }
        }
        unidad.setSelectedItem(valor);
    }

    void limpiar() {
        tipo.setSelectedIndex(0);
        if (tipo.getItemCount() > 5) {
            tipo.removeItemAt(5);
        }
    }
}
