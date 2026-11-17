package persistencia;

import negocio.JefeDisponible;
import negocio.Rol;
import negocio.Usuario;
import negocio.UsuarioResumen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RolesDAO {
    private RolesDAO() {}

    static final String ES_JEFE = condicionJefe("ESENCARGADOGASTOS");
    private static final String JEFE_DE_REGISTRO = condicionJefe("e.ESENCARGADOGASTOS");
    private static final String JEFE_DISPONIBLE = """
            (%s OR NOT EXISTS (
                SELECT 1 FROM ENCUESTADO jefe
                WHERE jefe.IDHOGAR = e.IDHOGAR AND %s
            ))
            AND NOT EXISTS (
                SELECT 1 FROM ENCUESTADO cuenta
                WHERE cuenta.IDHOGAR = e.IDHOGAR
                  AND cuenta.DOCUMENTO IS NOT NULL AND cuenta.CONTRASENA IS NOT NULL AND %s
            )
            """.formatted(JEFE_DE_REGISTRO, condicionJefe("jefe.ESENCARGADOGASTOS"),
                    condicionJefe("cuenta.ESENCARGADOGASTOS"));

    private static String condicionJefe(String columna) {
        return "UPPER(TRIM(CAST(" + columna + " AS VARCHAR2(10)))) IN ('S','SI','SÍ','1')";
    }

    static Connection conectar() throws SQLException {
        return ConexionOracle.obtenerConexion();
    }

    /** Busca el documento en las cuentas habilitadas de los tres roles y verifica la contraseña. */
    public static Usuario entrar(String documento, String clave) throws Exception {
        SesionActual.salir();
        String sql = """
                SELECT ID, NOMBRE, CONTRASENA, HOGAR, ROL
                FROM RH_ACCESO WHERE DOCUMENTO = ?
                """;
        try (Connection con = ConexionOracle.autenticar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            Usuario usuario = null;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!EncuestadorDAO.verificarClave(clave, rs.getString("CONTRASENA"))) {
                        continue;
                    }
                    if (usuario != null) {
                        throw new SQLException("El documento y la contraseña coinciden con más de una cuenta. "
                                + "Solicite al administrador que revise esas cuentas.");
                    }
                    usuario = new Usuario(rs.getInt("ID"), rs.getInt("HOGAR"), documento,
                            rs.getString("NOMBRE"), Rol.valueOf(rs.getString("ROL")));
                }
            }
            if (usuario != null) {
                SesionActual.iniciar(usuario.getRol(), usuario.getId(), usuario.getHogar());
                try (Connection prueba = ConexionOracle.obtenerConexion()) {
                    if (prueba.isClosed()) {
                        throw new SQLException("No se pudo abrir la conexión del rol.");
                    }
                } catch (SQLException ex) {
                    SesionActual.salir();
                    throw ex;
                }
            }
            return usuario;
        }
    }

    public static List<JefeDisponible> listarJefesSinCuenta(int administrador, int hogar) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        String sql = """
                SELECT e.IDENCUESTADO, e.NOMBRE, e.DOCUMENTO
                FROM ENCUESTADO e
                WHERE e.IDHOGAR = ? AND e.CONTRASENA IS NULL AND %s
                ORDER BY e.NOMBRE, e.IDENCUESTADO
                """.formatted(JEFE_DISPONIBLE);
        List<JefeDisponible> jefes = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jefes.add(new JefeDisponible(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
            }
        }
        return jefes;
    }

    public static void registrarEncuestador(int idAdministrador, String documento, String nombre,
            String clave) throws Exception {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, idAdministrador);
        validarCredenciales(documento, clave);
        validarNombre(nombre);
        EncuestadorDAO.registrar(documento, nombre, idAdministrador, clave);
    }

    public static void configurarAdministrador(String claveOracle, String documento, String nombre,
            String correo, String clave) throws Exception {
        try (Connection con = ConexionOracle.conectarConClave(claveOracle)) {
            configurarAdministradorInicial(con, documento, nombre, correo, clave);
        }
    }

    static void configurarAdministradorInicial(Connection con, String documento,
            String nombre, String correo, String clave) throws Exception {
        validarCredenciales(documento, clave);
        guardarAdministrador(con, documento, nombre, correo, clave);
    }

    static void validarCredenciales(String documento, String clave) {
        if (!documento.matches("[0-9]{5,20}")) {
            throw new IllegalArgumentException("El documento debe tener entre 5 y 20 dígitos.");
        }
        if (clave.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres.");
        }
    }

    private static void validarNombre(String nombre) {
        if (nombre.isBlank() || nombre.length() > 100) {
            throw new IllegalArgumentException("Ingrese un nombre de hasta 100 caracteres.");
        }
    }

    static void verificarAdministrador(Connection con, int id) throws SQLException {
        String sql = "SELECT 1 FROM COORDINADOR WHERE IDCOORDINADOR = ? AND ROL_ACCESO = 'ADMINISTRADOR'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Se requiere una cuenta de administrador.");
                }
            }
        }
    }

    private static void guardarAdministrador(Connection con, String documento, String nombre,
            String correo, String clave) throws Exception {
        if (correo.length() > 100) {
            throw new IllegalArgumentException("El correo debe tener hasta 100 caracteres.");
        }
        String hash = EncuestadorDAO.protegerContrasena(clave);
        con.setAutoCommit(false);
        try {
            try (Statement st = con.createStatement()) {
                st.execute("LOCK TABLE COORDINADOR IN EXCLUSIVE MODE");
            }
            if (actualizarAdministradorExistente(con, documento, hash)) {
                con.commit();
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM COORDINADOR WHERE DOCUMENTO = ?")) {
                ps.setString(1, documento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("Ese documento ya existe en el registro de responsables de campo.");
                    }
                }
            }

            validarNombre(nombre);
            String sql = """
                    INSERT INTO COORDINADOR
                        (IDCOORDINADOR, NOMBRE, CORREOINST, DOCUMENTO, CONTRASENA, ROL_ACCESO)
                    SELECT NVL(MAX(IDCOORDINADOR),0) + 1, ?, ?, ?, ?, 'ADMINISTRADOR'
                    FROM COORDINADOR
                    """;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, nombre);
                ps.setString(2, correo.isBlank() ? null : correo);
                ps.setString(3, documento);
                ps.setString(4, hash);
                ps.executeUpdate();
            }
            con.commit();
        } catch (Exception ex) {
            con.rollback();
            throw ex;
        }
    }

    public static void registrarJefe(int idAdministrador, int hogar, int idJefe, String documento, String clave)
            throws Exception {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, idAdministrador);
        validarCredenciales(documento, clave);
        String hash = EncuestadorDAO.protegerContrasena(clave);
        String sql = """
                UPDATE ENCUESTADO e SET DOCUMENTO = ?, CONTRASENA = ?
                WHERE e.IDENCUESTADO = ? AND e.IDHOGAR = ?
                  AND e.CONTRASENA IS NULL AND (e.DOCUMENTO IS NULL OR e.DOCUMENTO = ?)
                  AND %s
                """.formatted(JEFE_DISPONIBLE);
        String asignarJefe = """
                UPDATE ENCUESTADO e SET ESENCARGADOGASTOS = ?
                WHERE e.IDENCUESTADO = ? AND CASE WHEN %s THEN 1 ELSE 0 END = 0
                """.formatted(JEFE_DE_REGISTRO);

        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                verificarAdministrador(con, idAdministrador);
                try (Statement st = con.createStatement()) {
                    st.execute("LOCK TABLE ENCUESTADO IN EXCLUSIVE MODE");
                }
                verificarDocumentoJefe(con, idJefe, documento);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, documento);
                    ps.setString(2, hash);
                    ps.setInt(3, idJefe);
                    ps.setInt(4, hogar);
                    ps.setString(5, documento);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Revise el documento y vuelva a elegir el jefe. "
                                + "El registro pudo cambiar o ya tiene cuenta.");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(asignarJefe)) {
                    HogarDAO.fijarIndicadorJefe(con, ps, 1);
                    ps.setInt(2, idJefe);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    static void verificarDocumentoJefe(Connection con, int idJefe, String documento) throws SQLException {
        String sql = "SELECT 1 FROM ENCUESTADO WHERE DOCUMENTO = ? AND IDENCUESTADO <> ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, documento);
            ps.setInt(2, idJefe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("Ese documento ya está registrado en otro encuestado.");
                }
            }
        }
    }

    private static boolean actualizarAdministradorExistente(
            Connection con, String documento, String hash) throws SQLException {
        String buscar = "SELECT DOCUMENTO FROM COORDINADOR WHERE ROL_ACCESO = 'ADMINISTRADOR'";
        boolean hayAdministrador = false;
        boolean documentoAdministrador = false;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(buscar)) {
            while (rs.next()) {
                hayAdministrador = true;
                documentoAdministrador |= documento.equals(rs.getString(1));
            }
        }
        if (hayAdministrador && !documentoAdministrador) {
            throw new SQLException("Ya hay una cuenta de administrador. Indique su documento para "
                    + "recuperarla o use esa cuenta para crear otra.");
        }

        String actualizar = """
                UPDATE COORDINADOR SET CONTRASENA = ?, ROL_ACCESO = 'ADMINISTRADOR'
                WHERE DOCUMENTO = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(actualizar)) {
            ps.setString(1, hash);
            ps.setString(2, documento);
            int filas = ps.executeUpdate();
            if (filas > 1) {
                throw new SQLException("Hay registros duplicados con ese documento. Revise la tabla COORDINADOR.");
            }
            return filas == 1;
        }
    }
    public static List<UsuarioResumen> listarUsuarios(int administrador) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        String sql = """
                SELECT 'Encuestador' AS ROL, IDENCUESTADOR AS ID, NOMBRE, ESTADO FROM ENCUESTADOR
                WHERE NVL(UPPER(TRIM(ESTADO)), '-') <> 'BAJA_CUENTA'
                UNION ALL
                SELECT 'Administrador', IDCOORDINADOR, NOMBRE,
                    CASE WHEN CONTRASENA IS NULL THEN 'SIN ACCESO' ELSE 'ACTIVO' END
                FROM COORDINADOR WHERE ROL_ACCESO = 'ADMINISTRADOR'
                UNION ALL
                SELECT 'Jefe de hogar', IDENCUESTADO, NOMBRE, 'ACTIVO'
                FROM ENCUESTADO
                WHERE IDHOGAR IS NOT NULL AND DOCUMENTO IS NOT NULL AND CONTRASENA IS NOT NULL
                  AND %s
                ORDER BY 1, 3
                """.formatted(ES_JEFE);
        List<UsuarioResumen> usuarios = new ArrayList<>();
        try (Connection con = conectar(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                usuarios.add(new UsuarioResumen(rs.getInt(2), rs.getString(3), rs.getString(1), rs.getString(4)));
            }
        }
        return usuarios;
    }

    /** Elimina el acceso y conserva las personas que figuran en registros históricos. */
    public static void eliminarUsuario(int administrador, UsuarioResumen usuario) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        if (usuario == null || usuario.id() <= 0) {
            throw new SQLException("Selecciona un usuario.");
        }
        boolean encuestador = "Encuestador".equals(usuario.rol());
        boolean jefe = "Jefe de hogar".equals(usuario.rol());
        if (!encuestador && !jefe) {
            throw new SQLException("La cuenta de administrador no se puede eliminar.");
        }

        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                verificarAdministrador(con, administrador);
                String tabla = encuestador ? "ENCUESTADOR" : "ENCUESTADO";
                String id = encuestador ? "IDENCUESTADOR" : "IDENCUESTADO";
                String sql = "SELECT NOMBRE FROM " + tabla + " WHERE " + id + " = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, usuario.id());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !java.util.Objects.equals(usuario.nombre(), rs.getString(1))) {
                            throw new SQLException("El usuario cambió. Actualiza la lista y vuelve a intentarlo.");
                        }
                    }
                }
                if (encuestador) {
                    eliminarEncuestador(con, usuario.id());
                } else {
                    String baja = "UPDATE ENCUESTADO SET CONTRASENA = NULL WHERE IDENCUESTADO = ? AND "
                            + ES_JEFE + " AND IDHOGAR IS NOT NULL AND CONTRASENA IS NOT NULL";
                    try (PreparedStatement ps = con.prepareStatement(baja)) {
                        ps.setInt(1, usuario.id());
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("La cuenta del jefe ya no está disponible.");
                        }
                    }
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private static void eliminarEncuestador(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM ENCUESTADOR WHERE IDENCUESTADOR = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("El encuestador ya no está disponible.");
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() != 2292) {
                throw ex;
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ENCUESTADOR SET ESTADO = 'BAJA_CUENTA', CONTRASENA = NULL WHERE IDENCUESTADOR = ?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("El encuestador ya no está disponible.");
                }
            }
        }
    }

}
