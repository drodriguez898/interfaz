package persistencia;

import negocio.Articulo;
import negocio.RegistroArticulo;
import negocio.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArticuloDAO {

    private static final String COLUMNAS =
            "DESCRIPCION, CODIGOCOLCAP, CATEGORIACOLCAP";
    private static final String CODIGO_MANUAL = "01.9.9.9";
    private static final String CATEGORIA_MANUAL = "Otro artículo";

    public List<Articulo> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM ARTICULO "
                + "WHERE TRIM(DESCRIPCION) IS NOT NULL "
                + "ORDER BY UPPER(DESCRIPCION), IDARTICULO";

        List<Articulo> articulos = new ArrayList<>();

        try (Connection conexion = conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet filas = consulta.executeQuery()) {

            while (filas.next()) {
                articulos.add(leer(filas));
            }
        }

        return articulos;
    }

    public RegistroArticulo registrar(String nombre) throws SQLException {
        SesionActual.exigir(Rol.JEFE_HOGAR);
        String limpio = nombre == null ? "" : nombre.strip();
        if (limpio.isEmpty() || limpio.length() > 200) {
            throw new SQLException("Ingresa un nombre de producto de máximo 200 caracteres.");
        }

        String insertarSql = """
                INSERT INTO ARTICULO (
                    IDARTICULO, DESCRIPCION, CODIGOCOLCAP, CATEGORIACOLCAP, AGREGAMANUAL
                ) VALUES (?, ?, ?, ?, 'S')
                """;
        try (Connection conexion = conectar()) {
            Articulo existente = buscarPorNombre(conexion, limpio);
            if (existente != null) {
                return new RegistroArticulo(existente, true);
            }
            try (PreparedStatement siguiente = conexion.prepareStatement("SELECT SEQ_ARTICULO.NEXTVAL FROM DUAL");
                    PreparedStatement insertar = conexion.prepareStatement(insertarSql);
                    PreparedStatement buscarId = conexion.prepareStatement("SELECT 1 FROM ARTICULO WHERE IDARTICULO = ?")) {
                for (int intento = 0; intento < 10000; intento++) {
                    int id;
                    try (ResultSet resultado = siguiente.executeQuery()) {
                        if (!resultado.next()) {
                            throw new SQLException("No se pudo obtener el identificador del producto.");
                        }
                        id = resultado.getInt(1);
                    }
                    insertar.setInt(1, id);
                    insertar.setString(2, limpio);
                    insertar.setString(3, CODIGO_MANUAL);
                    insertar.setString(4, CATEGORIA_MANUAL);
                    try {
                        insertar.executeUpdate();
                        return new RegistroArticulo(new Articulo(limpio, CODIGO_MANUAL, CATEGORIA_MANUAL), false);
                    } catch (SQLException error) {
                        if (error.getErrorCode() != 1) {
                            throw error;
                        }
                        existente = buscarPorNombre(conexion, limpio);
                        if (existente != null) {
                            return new RegistroArticulo(existente, true);
                        }
                        buscarId.setInt(1, id);
                        try (ResultSet resultado = buscarId.executeQuery()) {
                            if (!resultado.next()) {
                                throw error;
                            }
                        }
                    }
                }
                throw new SQLException("La secuencia de artículos necesita ser revisada en Oracle.");
            }
        }
    }

    private Articulo buscarPorNombre(Connection conexion, String nombre)
            throws SQLException {

        String sql = "SELECT " + COLUMNAS + " FROM ARTICULO "
                + "WHERE UPPER(TRIM(DESCRIPCION)) = UPPER(?)";

        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombre);

            try (ResultSet filas = consulta.executeQuery()) {
                return filas.next() ? leer(filas) : null;
            }
        }
    }

    private Articulo leer(ResultSet fila) throws SQLException {
        return new Articulo(
                fila.getString("DESCRIPCION"),
                fila.getString("CODIGOCOLCAP"),
                fila.getString("CATEGORIACOLCAP")
        );
    }

    private Connection conectar() throws SQLException {
        return ConexionOracle.obtenerConexion();
    }
}
