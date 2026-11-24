package persistencia;

import negocio.Rol;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Sesión de la aplicación. */
public final class SesionActual {
    private static Rol rol;
    private static int id;
    private static int hogar;

    private SesionActual() {}

    static synchronized void iniciar(Rol nuevoRol, int nuevoId, int nuevoHogar) {
        rol = nuevoRol;
        id = nuevoId;
        hogar = nuevoHogar;
    }

    public static synchronized void salir() {
        rol = null;
        id = 0;
        hogar = 0;
    }

    public static synchronized boolean es(Rol esperado) {
        return esperado == rol && id > 0;
    }

    public static synchronized int id() {
        return id;
    }

    static synchronized Rol rolAutenticado() throws SQLException {
        if (rol == null || id <= 0) {
            throw new SQLException("Inicia sesión para acceder a los datos del programa.");
        }
        return rol;
    }

    static synchronized void exigir(Rol esperado) throws SQLException {
        if (!es(esperado)) {
            throw new SQLException("Esta operación requiere el rol " + esperado + ".");
        }
    }

    public static synchronized void exigirUsuario(Rol esperado, int idEsperado) throws SQLException {
        exigir(esperado);
        if (id != idEsperado) {
            throw new SQLException("La cuenta de esta ventana no coincide con la sesión activa.");
        }
    }

    static synchronized void exigirGastos(int idHogar) throws SQLException {
        exigir(Rol.JEFE_HOGAR);
        if (hogar <= 0 || hogar != idHogar) {
            throw new SQLException("El hogar no pertenece a su cuenta.");
        }
        try (Connection con = ConexionOracle.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM RH_JEFE_ACTIVO WHERE IDENCUESTADO = ? AND IDHOGAR = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, idHogar);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El acceso del jefe de hogar ya no está disponible.");
                }
            }
        }
    }
}
