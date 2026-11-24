package persistencia;

import negocio.Rol;

import java.sql.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class EncuestadorDAO {

    public static String protegerContrasena(String contrasena)
            throws Exception {

        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);

        byte[] hash = calcularHash(contrasena, sal);

        return Base64.getEncoder().encodeToString(sal)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private static byte[] calcularHash(String contrasena, byte[] sal)
            throws Exception {

        KeySpec spec = new PBEKeySpec(
                contrasena.toCharArray(), sal, 150000, 256);

        return SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();
    }

    public static boolean verificarClave(String clave, String guardada) throws Exception {
        if (guardada == null || !guardada.contains(":")) return false;
        String[] partes = guardada.split(":", 2);
        try { return MessageDigest.isEqual(Base64.getDecoder().decode(partes[1]), calcularHash(clave, Base64.getDecoder().decode(partes[0]))); }
        catch (IllegalArgumentException ex) { return false; }
    }

    static void registrar(String documento, String nombre,
            int administrador, String contrasena) throws Exception {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);

        String sql = "INSERT INTO ENCUESTADOR "
                + "(IDENCUESTADOR, DOCUMENTO, NOMBRE, "
                + "ESTADO, IDCOORDINADOR, CONTRASENA) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);

            try {
                RolesDAO.verificarAdministrador(con, administrador);
                try (Statement bloqueo = con.createStatement()) {
                    bloqueo.execute("LOCK TABLE ENCUESTADOR IN EXCLUSIVE MODE");
                }
                try (PreparedStatement verificar = con.prepareStatement("SELECT IDENCUESTADOR, ESTADO FROM ENCUESTADOR WHERE DOCUMENTO=? FOR UPDATE")) {
                    verificar.setString(1, documento);
                    try (ResultSet filas = verificar.executeQuery()) {
                        if (filas.next()) {
                            int existente = filas.getInt(1);
                            if (!"BAJA_CUENTA".equalsIgnoreCase(filas.getString(2))) {
                                throw new SQLException("Este documento ya está registrado como encuestador.");
                            }
                            try (PreparedStatement reactivar = con.prepareStatement(
                                    "UPDATE ENCUESTADOR SET NOMBRE = ?, ESTADO = 'ACTIVO', IDCOORDINADOR = ?, CONTRASENA = ? WHERE IDENCUESTADOR = ?")) {
                                reactivar.setString(1, nombre);
                                reactivar.setInt(2, administrador);
                                reactivar.setString(3, protegerContrasena(contrasena));
                                reactivar.setInt(4, existente);
                                reactivar.executeUpdate();
                            }
                            con.commit();
                            return;
                        }
                    }
                }
                int id;

                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery(
                        "SELECT NVL(MAX(IDENCUESTADOR),0)+1 FROM ENCUESTADOR")) {
                    rs.next();
                    id = rs.getInt(1);
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.setString(2, documento);
                    ps.setString(3, nombre);
                    ps.setString(4, "ACTIVO");
                    ps.setInt(5, administrador);
                    ps.setString(6, protegerContrasena(contrasena));
                    ps.executeUpdate();
                }

                con.commit();

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

}
