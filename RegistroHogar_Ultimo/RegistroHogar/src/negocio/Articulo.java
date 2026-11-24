package negocio;


public record Articulo(String nombre, String codigo, String nombreCategoria) {

    public String categoria() {
        if (codigo == null) {
            return nombreCategoria == null ? "" : nombreCategoria;
        }

        return nombreCategoria == null
                ? codigo
                : codigo + " · " + nombreCategoria;
    }
}
