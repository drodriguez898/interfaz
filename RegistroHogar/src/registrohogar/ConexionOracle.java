package registrohogar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionOracle {

    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String HOST = "127.0.0.1";
    private static final String PUERTO = "1521";
    private static final String SERVICE_NAME = "XEPDB1";
    private static final String USUARIO = "tienda880";
    private static final String CLAVE = "tienda8800";

    private static final String URL = "jdbc:oracle:thin:@//" + HOST + ":" + PUERTO + "/" + SERVICE_NAME;

    public static Connection obtenerConexion() {
        Connection conexion = null;
        try {
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection(URL, USUARIO, CLAVE);
            System.out.println("¡Conexión establecida con éxito a Oracle 21c!");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver JDBC no encontrado.");
        } catch (SQLException e) {
            System.err.println("Error de conexión a Oracle: " + e.getMessage());
        }
        return conexion;
    }

    public static void main(String[] args) {
        Connection con = obtenerConexion();
        if (con != null) {
            System.out.println(">>> CONEXION EXITOSA <<<");
        }
    }
}