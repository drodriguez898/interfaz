package negocio;

import java.math.BigDecimal;

public record GastoRevision(int id, int dia, String articulo, BigDecimal cantidad, String unidad, BigDecimal valor, Integer articuloId) {
    @Override public String toString() {
        return "Día " + dia + " · Gasto " + id + " · " + (articulo == null ? "Sin artículo" : articulo)
                + " · " + (cantidad == null ? "—" : cantidad.toPlainString())
                + (unidad == null ? "" : " " + unidad)
                + (valor == null ? "" : " · $" + valor.toPlainString());
    }
}
