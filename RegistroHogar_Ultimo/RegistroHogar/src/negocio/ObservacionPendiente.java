package negocio;


public record ObservacionPendiente(int id, int dia, int gasto, String articulo, String texto, boolean diaCerrado) {
    @Override public String toString() {
        return "Día " + dia + " · " + (articulo == null ? "Gasto " + gasto : articulo);
    }
}
