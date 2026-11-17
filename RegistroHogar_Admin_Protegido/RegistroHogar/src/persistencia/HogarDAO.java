package persistencia;

import negocio.DatosHogar;
import negocio.DatosJefe;
import negocio.EncuestadoResumen;
import negocio.EncuestadorAsignable;
import negocio.HogarAsignable;
import negocio.Rol;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia de hogares e integrantes en Oracle.
 */
public final class HogarDAO {

    private HogarDAO() {
    }

    public static int guardarHogar(int administrador, DatosHogar hogar, DatosJefe jefe, String clave) throws Exception {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        if (hogar.familia().isBlank() || hogar.familia().length() > 100
                || hogar.direccion().isBlank() || hogar.direccion().length() > 150
                || hogar.departamento().isBlank() || hogar.departamento().length() > 100
                || hogar.municipio().isBlank() || hogar.municipio().length() > 100
                || hogar.inicio() == null || hogar.personas() < 1) {
            throw new SQLException("Revise los datos obligatorios del hogar y la longitud de los campos.");
        }
        if (jefe.nombre().isBlank() || jefe.nombre().length() > 100 || jefe.edad() < 0 || jefe.edad() > 120) {
            throw new SQLException("Ingrese el nombre del jefe de hogar y una edad entre 0 y 120 años.");
        }
        RolesDAO.validarCredenciales(jefe.documento(), clave);
        String hash = EncuestadorDAO.protegerContrasena(clave);
        String insertarHogar = """
                INSERT INTO HOGAR (IDHOGAR, NOMBREFAMILIA, DIRECCION, DEPARTAMENTO,
                    MUNICIPIO, FECHAINICIO, CANTIDADPERSONAS)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String insertarJefe = """
                INSERT INTO ENCUESTADO (IDENCUESTADO, NOMBRE, EDAD, ESENCARGADOGASTOS, IDHOGAR,
                    DOCUMENTO, CONTRASENA)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);
            try {
                RolesDAO.verificarAdministrador(con, administrador);
                int idHogar = siguienteId(con, "HOGAR", "IDHOGAR");
                int idJefe = siguienteId(con, "ENCUESTADO", "IDENCUESTADO");
                RolesDAO.verificarDocumentoJefe(con, idJefe, jefe.documento());

                try (PreparedStatement ps = con.prepareStatement(insertarHogar)) {
                    ps.setInt(1, idHogar);
                    ps.setString(2, hogar.familia());
                    ps.setString(3, hogar.direccion());
                    ps.setString(4, hogar.departamento());
                    ps.setString(5, hogar.municipio());
                    ps.setDate(6, Date.valueOf(hogar.inicio()));
                    ps.setInt(7, hogar.personas());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(insertarJefe)) {
                    ps.setInt(1, idJefe);
                    ps.setString(2, jefe.nombre());
                    ps.setInt(3, jefe.edad());
                    fijarIndicadorJefe(con, ps, 4);
                    ps.setInt(5, idHogar);
                    ps.setString(6, jefe.documento());
                    ps.setString(7, hash);
                    ps.executeUpdate();
                }
                con.commit();
                return idHogar;
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    static void fijarIndicadorJefe(Connection con, PreparedStatement ps, int parametro) throws SQLException {
        String sql = """
                SELECT DATA_TYPE, CHAR_LENGTH
                FROM ALL_TAB_COLUMNS WHERE OWNER = SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')
                  AND TABLE_NAME = 'ENCUESTADO' AND COLUMN_NAME = 'ESENCARGADOGASTOS'
                """;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("No existe ENCUESTADO.ESENCARGADOGASTOS.");
            }
            if ("NUMBER".equalsIgnoreCase(rs.getString(1))) {
                ps.setInt(parametro, 1);
            } else {
                ps.setString(parametro, rs.getInt(2) == 1 ? "S" : "SI");
            }
        }
    }

    /**
     * Calcula el siguiente ID.
     */
    private static int siguienteId(
            Connection con,
            String tabla,
            String columna) throws SQLException {

        String sql = "SELECT NVL(MAX(" + columna + "), 0) + 1 FROM " + tabla;
        try (Statement st = con.createStatement()) {
            st.execute("LOCK TABLE " + tabla + " IN SHARE ROW EXCLUSIVE MODE");
            try (ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /**
     * Devuelve filas con este orden:
     * [idHogar, nombreVisible, direccion, municipio, fechaInicio]
     */
    public static List<String[]> listarHogares() throws SQLException {
        SesionActual.exigir(Rol.ADMINISTRADOR);

        List<String[]> resultado = new ArrayList<>();

        String sql =
            "SELECT h.idHogar, h.nombreFamilia, h.direccion, "
            + "h.municipio, h.fechaInicio "
            + "FROM hogar h "
            + "ORDER BY h.idHogar DESC";

        try (Connection con = ConexionOracle.obtenerConexion()) {


            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {

                while (rs.next()) {

                    String id = rs.getString("idHogar");
                    String nombre = rs.getString("nombreFamilia");

                    if (nombre == null || nombre.trim().isEmpty()) {
                        nombre = "Hogar #" + id;
                    }

                    String direccion = rs.getString("direccion");
                    String municipio = rs.getString("municipio");
                    String fecha = rs.getString("fechaInicio");

                    resultado.add(new String[]{
                        id,
                        nombre,
                        direccion,
                        municipio,
                        fecha
                    });
                }
            }
        }

        return resultado;
    }
    public static List<String[]> listarHogaresPorEncuestador(
        int idEncuestador) throws SQLException {
    SesionActual.exigirUsuario(Rol.ENCUESTADOR, idEncuestador);

    List<String[]> hogares = new ArrayList<>();

    String sql = "SELECT IDHOGAR, NOMBREFAMILIA, DIRECCION, "
            + "MUNICIPIO, FECHAINICIO "
            + "FROM HOGAR "
            + "WHERE IDENCUESTADOR = ? "
            + "ORDER BY IDHOGAR DESC";

    try (Connection con = RolesDAO.conectar();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, idEncuestador);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("IDHOGAR");
                String nombre = rs.getString("NOMBREFAMILIA");

                if (nombre == null || nombre.trim().isEmpty()) {
                    nombre = "Hogar #" + id;
                }

                hogares.add(new String[]{
                    id,
                    nombre,
                    rs.getString("DIRECCION"),
                    rs.getString("MUNICIPIO"),
                    rs.getString("FECHAINICIO")
                });
            }
        }
    }

    return hogares;
}

    public static List<EncuestadorAsignable> listarEncuestadoresParaAsignar() throws SQLException {
        SesionActual.exigir(Rol.ADMINISTRADOR);
        String sql = """
                SELECT IDENCUESTADOR, NOMBRE
                FROM ENCUESTADOR WHERE UPPER(TRIM(ESTADO)) = 'ACTIVO'
                ORDER BY NOMBRE, IDENCUESTADOR
                """;
        List<EncuestadorAsignable> encuestadores = new ArrayList<>();
        try (Connection con = RolesDAO.conectar(); Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                encuestadores.add(new EncuestadorAsignable(rs.getInt(1), rs.getString(2)));
            }
        }
        return encuestadores;
    }

    public static List<HogarAsignable> listarHogaresParaAsignar() throws SQLException {
        SesionActual.exigir(Rol.ADMINISTRADOR);
        String sql = """
                SELECT h.IDHOGAR, NVL(TRIM(h.NOMBREFAMILIA), 'Hogar #' || h.IDHOGAR),
                    h.MUNICIPIO, h.IDENCUESTADOR, e.NOMBRE,
                    CASE WHEN EXISTS (
                        SELECT 1 FROM ENCUESTADO
                        WHERE IDHOGAR = h.IDHOGAR AND DOCUMENTO IS NOT NULL
                          AND CONTRASENA IS NOT NULL AND %s
                    ) THEN 1 ELSE 0 END
                FROM HOGAR h LEFT JOIN ENCUESTADOR e ON e.IDENCUESTADOR = h.IDENCUESTADOR
                ORDER BY h.IDHOGAR DESC
                """.formatted(RolesDAO.ES_JEFE);
        List<HogarAsignable> hogares = new ArrayList<>();
        try (Connection con = RolesDAO.conectar(); Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idEncuestador = rs.getInt(4);
                Integer encuestador = rs.wasNull() ? null : idEncuestador;
                hogares.add(new HogarAsignable(rs.getInt(1), rs.getString(2), rs.getString(3), encuestador,
                        rs.getString(5), rs.getInt(6) == 1));
            }
        }
        return hogares;
    }

    static void verificarConsulta(Connection con, int hogar) throws SQLException {
        if (SesionActual.es(Rol.ENCUESTADOR)) {
            verificarAsignacion(con, hogar, SesionActual.id(), false);
        } else {
            SesionActual.exigirGastos(hogar);
        }
    }

    static void verificarAsignacion(Connection con, int hogar, int encuestador, boolean bloquear)
            throws SQLException {
        String sql = """
                SELECT 1 FROM HOGAR h
                WHERE h.IDHOGAR = ? AND h.IDENCUESTADOR = ?
                  AND EXISTS (SELECT 1 FROM RH_ESTADO_ENCUESTADOR e
                    WHERE e.IDENCUESTADOR = h.IDENCUESTADOR AND UPPER(TRIM(e.ESTADO)) = 'ACTIVO')
                """;
        if (bloquear) {
            sql += " FOR UPDATE";
        }
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hogar);
            ps.setInt(2, encuestador);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El hogar ya no está asignado a un encuestador activo de esta sesión.");
                }
            }
        }
    }

    public static void asignarHogar(int idAdministrador, int idHogar, int idEncuestador, Integer anterior)
            throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, idAdministrador);
        String encuestadorActivo = """
                SELECT IDENCUESTADOR FROM ENCUESTADOR
                WHERE IDENCUESTADOR = ? AND UPPER(TRIM(ESTADO)) = 'ACTIVO' FOR UPDATE
                """;
        String asignarHogar = """
                UPDATE HOGAR SET IDENCUESTADOR = ?
                WHERE IDHOGAR = ? AND (IDENCUESTADOR = ? OR (IDENCUESTADOR IS NULL AND ? IS NULL))
                """;
        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);
            try {
                RolesDAO.verificarAdministrador(con, idAdministrador);
                try (PreparedStatement ps = con.prepareStatement(encuestadorActivo)) {
                    ps.setInt(1, idEncuestador);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El encuestador seleccionado debe estar activo.");
                        }
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(asignarHogar)) {
                    ps.setInt(1, idEncuestador);
                    ps.setInt(2, idHogar);
                    ps.setObject(3, anterior, Types.NUMERIC);
                    ps.setObject(4, anterior, Types.NUMERIC);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("La asignación cambió. Seleccione el hogar nuevamente.");
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }
    /**
     * Elimina el hogar y los registros asociados en una sola transacción.
     */
    public static void eliminarHogar(int administrador, int hogar, String familia) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        if (hogar <= 0 || familia == null) {
            throw new SQLException("Selecciona un hogar válido.");
        }
        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);
            try {
                RolesDAO.verificarAdministrador(con, administrador);
                String consulta = """
                        SELECT NVL(TRIM(NOMBREFAMILIA), 'Hogar #' || IDHOGAR)
                        FROM HOGAR WHERE IDHOGAR = ? FOR UPDATE
                        """;
                try (PreparedStatement ps = con.prepareStatement(consulta)) {
                    ps.setInt(1, hogar);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !familia.equals(rs.getString(1))) {
                            throw new SQLException("El hogar cambió. Actualiza la lista y vuelve a seleccionarlo.");
                        }
                    }
                }

                String[] sentencias = {
                    """
                    DELETE FROM OBSERVACION WHERE IDGASTO IN (
                        SELECT g.IDGASTO FROM GASTO g JOIN DIACICLO d ON d.IDDIA = g.IDDIA
                        JOIN ENCUESTA e ON e.IDENCUESTA = d.IDENCUESTA WHERE e.IDHOGAR = ?
                    )
                    """,
                    """
                    DELETE FROM GASTO WHERE IDDIA IN (
                        SELECT d.IDDIA FROM DIACICLO d JOIN ENCUESTA e ON e.IDENCUESTA = d.IDENCUESTA
                        WHERE e.IDHOGAR = ?
                    )
                    """,
                    """
                    DELETE FROM DIACICLO WHERE IDENCUESTA IN (
                        SELECT IDENCUESTA FROM ENCUESTA WHERE IDHOGAR = ?
                    )
                    """,
                    "DELETE FROM ENCUESTA WHERE IDHOGAR = ?",
                    "DELETE FROM VISITA WHERE IDHOGAR = ?",
                    "DELETE FROM ENCUESTADO WHERE IDHOGAR = ?",
                    "DELETE FROM HOGAR WHERE IDHOGAR = ?"
                };
                for (int i = 0; i < sentencias.length; i++) {
                    try (PreparedStatement ps = con.prepareStatement(sentencias[i])) {
                        ps.setInt(1, hogar);
                        int eliminados = ps.executeUpdate();
                        if (i == sentencias.length - 1 && eliminados != 1) {
                            throw new SQLException("El hogar ya no está disponible.");
                        }
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                try {
                    con.rollback();
                } catch (SQLException errorRollback) {
                    ex.addSuppressed(errorRollback);
                }
                if (ex.getErrorCode() == 2292) {
                    throw new SQLException("No se pudo eliminar el hogar porque otro registro de la base "
                            + "hace referencia a sus datos. No se eliminó ningún dato.", ex);
                }
                throw ex;
            }
        }
    }

    public static List<EncuestadoResumen> listarEncuestados(int hogar) throws SQLException {
        String sql = "SELECT IDENCUESTADO, NOMBRE FROM RH_PERSONAS_HOGAR WHERE IDHOGAR = ? ORDER BY IDENCUESTADO";
        List<EncuestadoResumen> encuestados = new ArrayList<>();
        try (Connection con = RolesDAO.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            verificarConsulta(con, hogar);
            ps.setInt(1, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    encuestados.add(new EncuestadoResumen(rs.getInt(1), rs.getString(2)));
                }
            }
        }
        return encuestados;
    }

    public static String consultarNombreFamilia(int hogar) throws SQLException {
        SesionActual.exigirGastos(hogar);
        try (Connection con = RolesDAO.conectar(); PreparedStatement ps = con.prepareStatement(
                "SELECT NOMBREFAMILIA FROM HOGAR WHERE IDHOGAR = ?")) {
            ps.setInt(1, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

}
