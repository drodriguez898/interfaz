package negocio;


public record JefeDisponible(int id, String nombre, String documento) {
    @Override public String toString() {
        return nombre + " (ID " + id + ")";
    }
}
