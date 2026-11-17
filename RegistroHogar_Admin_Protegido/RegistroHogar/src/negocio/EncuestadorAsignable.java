package negocio;


public record EncuestadorAsignable(int id, String nombre) {
    @Override public String toString() {
        return nombre + " (ID " + id + ")";
    }
}
