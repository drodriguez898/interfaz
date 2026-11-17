package persistencia;

import negocio.DatosInformeEncuesta;
import negocio.Rol;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public final class InformeEncuesta {

    private InformeEncuesta() {}

    public static DatosInformeEncuesta cargar(int encuesta, int administrador) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        String sql = """
                SELECT NVL(TRIM(h.NOMBREFAMILIA), 'Hogar #' || h.IDHOGAR), h.MUNICIPIO,
                    h.DEPARTAMENTO, u.NOMBRE, a.NOMBRE, e.FECHATRANSMISION, e.FECHACIERRE,
                    (SELECT COUNT(DISTINCT d.NUMERODIA) FROM DIACICLO d
                     WHERE d.IDENCUESTA = e.IDENCUESTA AND d.NUMERODIA BETWEEN 1 AND 14
                       AND UPPER(TRIM(d.ESTADO)) = 'CERRADO')
                FROM ENCUESTA e JOIN HOGAR h ON h.IDHOGAR = e.IDHOGAR
                LEFT JOIN ENCUESTADOR u ON u.IDENCUESTADOR = h.IDENCUESTADOR
                LEFT JOIN COORDINADOR a ON a.IDCOORDINADOR = e.IDADMINISTRADOR
                WHERE e.IDENCUESTA = ? AND UPPER(TRIM(e.ESTADO)) = 'CERRADO'
                """;
        try (Connection con = RolesDAO.conectar()) {
            RolesDAO.verificarAdministrador(con, administrador);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, encuesta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("El informe está disponible cuando la encuesta está cerrada.");
                    }
                    String municipio = rs.getString(2);
                    String departamento = rs.getString(3);
                    String ubicacion = municipio == null ? departamento : departamento == null
                            ? municipio : municipio + ", " + departamento;
                    return new DatosInformeEncuesta(encuesta, rs.getString(1), ubicacion, rs.getString(4), rs.getString(5),
                            fecha(rs.getTimestamp(6)), fecha(rs.getTimestamp(7)), rs.getInt(8),
                            CasosDAO.listarGastosRevision(con, encuesta));
                }
            }
        }
    }

    private static LocalDateTime fecha(Timestamp valor) {
        return valor == null ? null : valor.toLocalDateTime();
    }

}
