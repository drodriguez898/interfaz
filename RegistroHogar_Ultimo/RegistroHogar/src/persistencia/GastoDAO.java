package persistencia;

import negocio.DatosEditables;
import negocio.Importes;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistencia de gastos por hogar y número de día.
 */
public class GastoDAO {

    private static Connection conectar() throws SQLException {
        return ConexionOracle.obtenerConexion();
    }

    // ==========================================
    // LISTAR GASTOS DE UN HOGAR Y DÍA
    // ==========================================

    public static List<String[]> listar(int hogar, int numeroDia)
            throws SQLException {

        String sql =
            "SELECT g.IDGASTO, a.DESCRIPCION, g.CANTIDAD, "
            + "g.UNIDADMEDIDA, g.FORMAADQUISICION, "
            + "a.CODIGOCOLCAP, a.CATEGORIACOLCAP, "
            + "g.VALOR, g.FRECUENCIACOMPRA, g.TIPOGASTO, g.IDARTICULO "
            + "FROM GASTO g "
            + "JOIN DIACICLO d ON d.IDDIA = g.IDDIA "
            + "JOIN ENCUESTA e ON e.IDENCUESTA = d.IDENCUESTA "
            + "LEFT JOIN ARTICULO a ON a.IDARTICULO = g.IDARTICULO "
             + "WHERE e.IDHOGAR = ? AND d.NUMERODIA = ? "
            + "AND e.IDENCUESTA = (SELECT MAX(actual.IDENCUESTA) FROM ENCUESTA actual WHERE actual.IDHOGAR = e.IDHOGAR) "
            + "ORDER BY g.IDGASTO";

        List<String[]> filas = new ArrayList<>();

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            HogarDAO.verificarConsulta(con, hogar);
            ps.setInt(1, hogar);
            ps.setInt(2, numeroDia);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    String codigo = rs.getString(6);
                    String categoria = rs.getString(7);

                    String etiqueta =
                        (codigo == null ? "" : codigo)
                        + (categoria == null
                            ? ""
                            : " · " + categoria);

                    filas.add(new String[]{
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        etiqueta,
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10),
                        rs.getString(1),
                        rs.getString(11)
                    });
                }
            }
        }

        return filas;
    }

    // ==========================================
    // CONSULTAR DÍAS CERRADOS
    // ==========================================

    public static Set<Integer> diasCerrados(int hogar)
            throws SQLException {

        Set<Integer> cerrados = new HashSet<>();

        String sql =
            "SELECT d.NUMERODIA "
            + "FROM DIACICLO d "
            + "JOIN ENCUESTA e "
            + "ON e.IDENCUESTA = d.IDENCUESTA "
             + "WHERE e.IDHOGAR = ? "
            + "AND e.IDENCUESTA = (SELECT MAX(actual.IDENCUESTA) FROM ENCUESTA actual WHERE actual.IDHOGAR = e.IDHOGAR) "
            + "AND UPPER(TRIM(d.ESTADO)) = 'CERRADO'";

        try (Connection con = conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            HogarDAO.verificarConsulta(con, hogar);
            ps.setInt(1, hogar);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    cerrados.add(rs.getInt(1));
                }
            }
        }

        return cerrados;
    }

    // ==========================================
    // COMPROBAR SI UN DÍA ESTÁ CERRADO
    // ==========================================

    public static boolean estaCerrado(int hogar, int dia)
            throws SQLException {

        return diasCerrados(hogar).contains(dia);
    }

    public static String motivoSoloLectura(int hogar, int dia) throws SQLException {
        SesionActual.exigirGastos(hogar);
        String sql = """
                SELECT e.ESTADO, d.ESTADO FROM ENCUESTA e
                LEFT JOIN DIACICLO d ON d.IDENCUESTA = e.IDENCUESTA AND d.NUMERODIA = ?
                WHERE e.IDHOGAR = ? ORDER BY e.IDENCUESTA DESC, d.IDDIA DESC
                """;
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, dia);
            ps.setInt(2, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String diaEstado = rs.getString(2);
                    if (diaEstado != null && "CERRADO".equalsIgnoreCase(diaEstado.strip())) {
                        return "Día cerrado · Solo lectura";
                    }
                    String encuesta = rs.getString(1);
                    if (encuesta != null && ("TRANSMITIDO".equalsIgnoreCase(encuesta.strip())
                            || "CERRADO".equalsIgnoreCase(encuesta.strip()))) {
                        return "Encuesta enviada o cerrada · Solo lectura";
                    }
                }
                return "";
            }
        }
    }

    // ==========================================
    // CERRAR DÍA
    // PERMITE DÍAS SIN GASTOS
    // ==========================================

    public static void cerrarDia(int hogar, int dia)
            throws SQLException {

        // Verificar que el usuario sea el jefe
        // del hogar correspondiente.
        SesionActual.exigirGastos(hogar);

        if (hogar <= 0 || dia < 1 || dia > 14) {
            throw new SQLException(
                "Selecciona un hogar y un día válidos."
            );
        }

        try (Connection con = conectar()) {

            con.setAutoCommit(false);

            try {

                // Comprobar que el caso siga editable.
                verificarCasoEditable(con, hogar);

                // Crear el día si todavía no existe.
                // Esto permite cerrar días con 0 gastos.
                int idDia = obtenerDia(
                    con,
                    hogar,
                    dia,
                    true
                );

                // Comprobar que el día no esté cerrado.
                comprobarAbierto(con, idDia);

                // Procedimiento Oracle: ejecuta UPDATE y activa RH_DIA_CIERRE.
                try (java.sql.CallableStatement llamada = con.prepareCall("{call RH_CERRAR_DIA_PROC(?)}")) {
                    llamada.setInt(1, idDia);
                    llamada.execute();
                }

                con.commit();

            } catch (SQLException ex) {

                con.rollback();
                throw ex;
            }
        }
    }

    // ==========================================
    // COMPROBAR QUE EL DÍA ESTÁ ABIERTO
    // ==========================================

    private static void comprobarAbierto(
            Connection con,
            int idDia
    ) throws SQLException {

        String sql =
            "SELECT ESTADO "
            + "FROM DIACICLO "
            + "WHERE IDDIA = ? "
            + "FOR UPDATE";

        try (PreparedStatement ps =
                 con.prepareStatement(sql)) {

            ps.setInt(1, idDia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "El día ya no existe."
                    );
                }

                String estado = rs.getString(1);

                if (estado != null
                        && estado.trim().equalsIgnoreCase("CERRADO")) {

                    throw new SQLException(
                        "El día está cerrado. "
                        + "No se pueden modificar sus gastos."
                    );
                }
            }
        }
    }

    // ==========================================
    // GUARDAR O EDITAR GASTO
    // ==========================================

    static Importes leerImportes(String cantidad, String valor) throws SQLException {
        BigDecimal cant;
        BigDecimal importe;
        try {
            cant = new BigDecimal(cantidad.trim().replace(",", "."));
            String entrada = valor.trim();
            if (entrada.matches("[0-9]{1,3}(\\.[0-9]{3})+(,[0-9]+)?")) {
                entrada = entrada.replace(".", "");
            }
            importe = new BigDecimal(entrada.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new SQLException("La cantidad o el valor no es un número válido.", ex);
        }
        if (cant.signum() <= 0 || importe.signum() <= 0) {
            throw new SQLException("La cantidad y el valor deben ser mayores que cero.");
        }
        if (cant.scale() > 2 || importe.scale() > 2) {
            throw new SQLException("La cantidad y el valor admiten máximo dos decimales.");
        }
        if (cant.precision() - cant.scale() > 8 || importe.precision() - importe.scale() > 10) {
            throw new SQLException("La cantidad o el valor supera el tamaño permitido.");
        }
        return new Importes(cant, importe);
    }

    static DatosEditables leerEdicion(Connection con, int gasto) throws SQLException {
        String sql = """
                SELECT IDDIA, IDARTICULO, CANTIDAD, UNIDADMEDIDA, FORMAADQUISICION, VALOR,
                    FRECUENCIACOMPRA, TIPOGASTO, LUGARCOMPRA
                FROM GASTO WHERE IDGASTO = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, gasto);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El gasto ya no está disponible.");
                }
                int dia = rs.getInt(1);
                int articulo = rs.getInt(2);
                Integer articuloId = rs.wasNull() ? null : articulo;
                return new DatosEditables(dia, articuloId, rs.getBigDecimal(3), rs.getString(4),
                        rs.getString(5), rs.getBigDecimal(6), rs.getString(7), rs.getString(8), rs.getString(9));
            }
        }
    }

    public static DatosEditables cargarEdicionJefe(int hogar, int dia, int gasto) throws SQLException {
        SesionActual.exigirGastos(hogar);
        try (Connection con = conectar()) {
            DatosEditables datos = leerEdicion(con, gasto);
            if (datos.diaId() != obtenerDia(con, hogar, dia, false)) {
                throw new SQLException("El gasto no pertenece a este hogar y día.");
            }
            return datos;
        }
    }

    static void verificarEdicion(Connection con, int gasto, DatosEditables anterior) throws SQLException {
        if (anterior != null && !anterior.equals(leerEdicion(con, gasto))) {
            throw new SQLException("Este gasto cambió. Vuelve a abrirlo antes de modificarlo.");
        }
    }

    static void actualizarGasto(Connection con, int gasto, DatosEditables datos) throws SQLException {
        String sql = """
                UPDATE GASTO SET IDARTICULO = ?, CANTIDAD = ?, UNIDADMEDIDA = ?, FORMAADQUISICION = ?,
                    VALOR = ?, FRECUENCIACOMPRA = ?, TIPOGASTO = ?, LUGARCOMPRA = ?
                WHERE IDGASTO = ? AND IDDIA = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            fijarDatos(ps, 1, datos);
            ps.setInt(9, gasto);
            ps.setInt(10, datos.diaId());
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se encontró el gasto de este día.");
            }
        }
    }

    private static void fijarDatos(PreparedStatement ps, int inicio, DatosEditables datos) throws SQLException {
        ps.setObject(inicio, datos.articuloId(), Types.NUMERIC);
        ps.setBigDecimal(inicio + 1, datos.cantidad());
        ps.setString(inicio + 2, datos.unidad());
        ps.setString(inicio + 3, datos.adquisicion());
        ps.setBigDecimal(inicio + 4, datos.valor());
        ps.setString(inicio + 5, datos.frecuencia());
        ps.setString(inicio + 6, datos.tipo());
        ps.setString(inicio + 7, datos.lugar());
    }

    public static void guardar(int hogar, int dia, Integer idGasto, String articulo, String cantidad,
            String unidad, String adquisicion, String valor, String frecuencia, String tipo, String lugar)
            throws SQLException {
        guardar(hogar, dia, idGasto, articulo, cantidad, unidad, adquisicion, valor, frecuencia, tipo, lugar, null);
    }

    public static void guardar(int hogar, int dia, Integer idGasto, String articulo, String cantidad,
            String unidad, String adquisicion, String valor, String frecuencia, String tipo, String lugar,
            DatosEditables anterior) throws SQLException {
        SesionActual.exigirGastos(hogar);
        if (hogar <= 0 || dia < 1 || dia > 14) {
            throw new SQLException("Selecciona un hogar y un día válidos.");
        }
        if (articulo == null || articulo.isBlank() || cantidad == null || valor == null
                || unidad == null || unidad.isBlank() || adquisicion == null || adquisicion.isBlank()
                || frecuencia == null || frecuencia.isBlank() || tipo == null || tipo.isBlank()) {
            throw new SQLException("Complete los campos obligatorios del gasto.");
        }
        Importes importes = leerImportes(cantidad, valor);
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                verificarCasoEditable(con, hogar);
                int idDia = obtenerDia(con, hogar, dia, idGasto == null);
                if (idDia == 0) {
                    throw new SQLException("No se encontró el día de este hogar.");
                }
                comprobarAbierto(con, idDia);
                int idArticulo = buscarArticulo(con, articulo);
                if (idArticulo == 0) {
                    throw new SQLException("Selecciona un artículo del catálogo.");
                }
                DatosEditables datos = new DatosEditables(idDia, idArticulo, importes.cantidad(), unidad, adquisicion,
                        importes.valor(), frecuencia, tipo, lugar);
                if (idGasto == null) {
                    int nuevo = siguienteId(con, "GASTO", "IDGASTO");
                    String insertar = """
                            INSERT INTO GASTO (IDGASTO, IDDIA, IDARTICULO, CANTIDAD, UNIDADMEDIDA, FORMAADQUISICION,
                                VALOR, FRECUENCIACOMPRA, TIPOGASTO, LUGARCOMPRA, ESTADO, SINCRONIZADO)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'COMPLETO', 'S')
                            """;
                    try (PreparedStatement ps = con.prepareStatement(insertar)) {
                        ps.setInt(1, nuevo);
                        ps.setInt(2, idDia);
                        fijarDatos(ps, 3, datos);
                        ps.executeUpdate();
                    }
                } else {
                    verificarEdicion(con, idGasto, anterior);
                    actualizarGasto(con, idGasto, datos);
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // ==========================================
    // ELIMINAR GASTO
    // ==========================================

    public static void eliminar(int hogar, int dia, int idGasto) throws SQLException {
        eliminar(hogar, dia, idGasto, null);
    }

    public static void eliminar(int hogar, int dia, int idGasto, DatosEditables anterior) throws SQLException {
        SesionActual.exigirGastos(hogar);

        try (Connection con = conectar()) {

            con.setAutoCommit(false);

            try {

                verificarCasoEditable(con, hogar);

                int idDia = obtenerDia(
                    con,
                    hogar,
                    dia,
                    false
                );

                if (idDia == 0) {
                    throw new SQLException(
                        "No se encontró el día de este hogar."
                    );
                }

                comprobarAbierto(con, idDia);
                verificarEdicion(con, idGasto, anterior);

                String sql =
                    "DELETE FROM GASTO "
                    + "WHERE IDGASTO = ? AND IDDIA = ?";

                try (PreparedStatement ps =
                         con.prepareStatement(sql)) {

                    ps.setInt(1, idGasto);
                    ps.setInt(2, idDia);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "El gasto no existe en este día."
                        );
                    }
                }

                con.commit();

            } catch (SQLException ex) {

                con.rollback();
                throw ex;
            }
        }
    }

    // ==========================================
    // VERIFICAR QUE LA ENCUESTA SEA EDITABLE
    // ==========================================

    private static void verificarCasoEditable(
            Connection con,
            int hogar
    ) throws SQLException {

        // Serializa la primera escritura cuando todavía no existe una encuesta del hogar.
        try (PreparedStatement ps = con.prepareStatement("SELECT IDHOGAR FROM HOGAR WHERE IDHOGAR = ? FOR UPDATE")) {
            ps.setInt(1, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encuentra el hogar.");
                }
            }
        }

        int id = 0;

        String sql =
            "SELECT IDENCUESTA "
            + "FROM ENCUESTA "
            + "WHERE IDHOGAR = ? "
            + "ORDER BY IDENCUESTA DESC";

        try (PreparedStatement ps =
                 con.prepareStatement(sql)) {

            ps.setInt(1, hogar);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    id = rs.getInt(1);
                }
            }
        }

        if (id == 0) {
            return;
        }

        String verificar =
            "SELECT ESTADO "
            + "FROM ENCUESTA "
            + "WHERE IDENCUESTA = ? "
            + "FOR UPDATE";

        try (PreparedStatement ps =
                 con.prepareStatement(verificar)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                        "No se encuentra la encuesta."
                    );
                }

                String estado = rs.getString(1);

                if (estado != null
                        && (estado.strip().equalsIgnoreCase("TRANSMITIDO")
                        || estado.strip().equalsIgnoreCase("CERRADO"))) {

                    throw new SQLException(
                        "El caso está transmitido o cerrado; "
                        + "no se pueden modificar gastos."
                    );
                }
            }
        }
    }

    // ==========================================
    // BUSCAR ARTÍCULO EN EL CATÁLOGO
    // ==========================================

    static int buscarArticulo(
            Connection con,
            String nombre
    ) throws SQLException {

        String sql =
            "SELECT IDARTICULO "
            + "FROM ARTICULO "
            + "WHERE UPPER(TRIM(DESCRIPCION)) = "
            + "UPPER(TRIM(?))";

        try (PreparedStatement ps =
                 con.prepareStatement(sql)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    // ==========================================
    // OBTENER O CREAR DÍA
    // ==========================================

    private static int obtenerDia(
            Connection con,
            int hogar,
            int dia,
            boolean crear
    ) throws SQLException {

        String sql =
            "SELECT d.IDDIA "
            + "FROM DIACICLO d "
            + "JOIN ENCUESTA e "
            + "ON e.IDENCUESTA = d.IDENCUESTA "
            + "WHERE e.IDHOGAR = ? "
             + "AND d.NUMERODIA = ? "
            + "AND e.IDENCUESTA = (SELECT MAX(actual.IDENCUESTA) FROM ENCUESTA actual WHERE actual.IDHOGAR = e.IDHOGAR) "
            + "ORDER BY d.IDDIA DESC";

        try (PreparedStatement ps =
                 con.prepareStatement(sql)) {

            ps.setInt(1, hogar);
            ps.setInt(2, dia);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        if (!crear) {
            return 0;
        }

        int encuesta;

        String buscarEncuesta =
            "SELECT IDENCUESTA "
            + "FROM ENCUESTA "
            + "WHERE IDHOGAR = ? "
            + "ORDER BY IDENCUESTA DESC";

        try (PreparedStatement ps =
                 con.prepareStatement(buscarEncuesta)) {

            ps.setInt(1, hogar);

            try (ResultSet rs = ps.executeQuery()) {

                encuesta = rs.next()
                    ? rs.getInt(1)
                    : 0;
            }
        }

        if (encuesta == 0) {

            encuesta = siguienteId(
                con,
                "ENCUESTA",
                "IDENCUESTA"
            );

            String insertarEncuesta =
                "INSERT INTO ENCUESTA "
                + "(IDENCUESTA, IDHOGAR, ESTADO) "
                + "VALUES (?, ?, 'EN_PROGRESO')";

            try (PreparedStatement ps =
                     con.prepareStatement(insertarEncuesta)) {

                ps.setInt(1, encuesta);
                ps.setInt(2, hogar);
                ps.executeUpdate();
            }
        }

        int idDia = siguienteId(
            con,
            "DIACICLO",
            "IDDIA"
        );

        String insertarDia =
            "INSERT INTO DIACICLO "
            + "(IDDIA, NUMERODIA, IDENCUESTA, "
            + "ESTADO, FECHA) "
            + "VALUES (?, ?, ?, 'EN_PROGRESO', SYSDATE)";

        try (PreparedStatement ps =
                 con.prepareStatement(insertarDia)) {

            ps.setInt(1, idDia);
            ps.setInt(2, dia);
            ps.setInt(3, encuesta);

            ps.executeUpdate();
        }

        return idDia;
    }

    // ==========================================
    // GENERAR SIGUIENTE ID
    // ==========================================

    private static int siguienteId(
            Connection con,
            String tabla,
            String columna
    ) throws SQLException {

        // Los nombres de tabla y columna son
        // constantes internas del programa.

        try (Statement st = con.createStatement()) {

            st.execute(
                "LOCK TABLE " + tabla
                + " IN EXCLUSIVE MODE"
            );

            String sql =
                "SELECT NVL(MAX(" + columna + "), 0) + 1 "
                + "FROM " + tabla;

            try (ResultSet rs = st.executeQuery(sql)) {

                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
