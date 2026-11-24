package persistencia;

import negocio.Rol;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Properties;

final class ConexionOracle {
    private static final String URL_ORIGINAL = "jdbc:oracle:thin:@192.168.254.215:1521:ORCL";
    private static final String ESQUEMA_ORIGINAL = "PROYECTO1097100880";

    private ConexionOracle() {}

    static Connection autenticar() throws SQLException {
        return conectarAplicacion("RH_APP_ACCESO", "acceso.clave");
    }

    static Connection obtenerConexion() throws SQLException {
        return switch (SesionActual.rolAutenticado()) {
            case ADMINISTRADOR -> conectarAplicacion("RH_APP_ADMIN", "administrador.clave");
            case ENCUESTADOR -> conectarAplicacion("RH_APP_ENC", "encuestador.clave");
            case JEFE_HOGAR -> conectarAplicacion("RH_APP_JEFE", "jefe.clave");
        };
    }

    static Connection conectarConClave(String clave) throws SQLException {
        Properties config = cargarConfiguracion();
        return DriverManager.getConnection(url(config), esquema(config), clave);
    }

    private static Connection conectarAplicacion(String usuario, String propiedad) throws SQLException {
        Properties config = cargarConfiguracion();
        String clave = config.getProperty(propiedad);
        if (clave == null || clave.isBlank()) {
            throw new SQLException("Falta configurar la contraseña de Oracle para " + usuario + ".");
        }
        String propietario = esquema(config);
        Connection con = DriverManager.getConnection(url(config), usuario, clave);
        try (Statement st = con.createStatement()) {
            st.execute("ALTER SESSION SET CURRENT_SCHEMA = " + propietario);
            return con;
        } catch (SQLException ex) {
            try {
                con.close();
            } catch (SQLException cierre) {
                ex.addSuppressed(cierre);
            }
            throw ex;
        }
    }

    private static Properties cargarConfiguracion() throws SQLException {
        String ruta = System.getenv("RH_ORACLE_CONFIG");
        Path archivo = ruta == null || ruta.isBlank()
                ? Path.of(System.getProperty("user.home"), "conexiones-oracle.properties")
                : Path.of(ruta);
        Properties config = new Properties();
        try (Reader lector = Files.newBufferedReader(archivo)) {
            config.load(lector);
            return config;
        } catch (IOException | IllegalArgumentException ex) {
            throw new SQLException("Revisa el archivo de configuración de Oracle: " + archivo, ex);
        }
    }

    private static String url(Properties config) {
        return config.getProperty("url", URL_ORIGINAL).strip();
    }

    private static String esquema(Properties config) throws SQLException {
        String nombre = config.getProperty("esquema", ESQUEMA_ORIGINAL).strip().toUpperCase(Locale.ROOT);
        if (!nombre.matches("[A-Z][A-Z0-9_$#]{0,127}")) {
            throw new SQLException("El nombre del esquema Oracle es inválido.");
        }
        return nombre;
    }
}
