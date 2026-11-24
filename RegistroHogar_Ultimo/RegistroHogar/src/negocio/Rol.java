package negocio;


public enum Rol {
    ENCUESTADOR("Encuestador"),
    JEFE_HOGAR("Jefe de hogar"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override public String toString() {
        return etiqueta;
    }
}
