package negocio;

/** Identidad y rol del usuario autenticado. */
public final class Usuario {
    private int id;
    private int hogar;
    private String documento;
    private String nombre;
    private Rol rol;

    public Usuario(int id, int hogar, String documento, String nombre, Rol rol) {
        this.id = id;
        this.hogar = hogar;
        this.documento = documento;
        this.nombre = nombre;
        this.rol = rol;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getHogar() { return hogar; }
    public void setHogar(int hogar) { this.hogar = hogar; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
