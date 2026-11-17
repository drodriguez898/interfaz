package persistencia;

import negocio.CorreccionGasto;
import negocio.DatosEditables;
import negocio.EncuestaResumen;
import negocio.GastoRevision;
import negocio.Importes;
import negocio.ObservacionPendiente;
import negocio.Rol;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Operaciones sobre las tablas existentes de hogares, visitas y encuestas. */
public final class CasosDAO {
    private CasosDAO() {}

    static List<GastoRevision> listarGastosRevision(Connection con, int encuesta) throws SQLException {
        SesionActual.exigir(Rol.ADMINISTRADOR);
        String sql = """
                SELECT g.IDGASTO, d.NUMERODIA, a.DESCRIPCION, g.CANTIDAD, g.UNIDADMEDIDA, g.VALOR, g.IDARTICULO
                FROM GASTO g JOIN DIACICLO d ON d.IDDIA = g.IDDIA
                LEFT JOIN ARTICULO a ON a.IDARTICULO = g.IDARTICULO
                WHERE d.IDENCUESTA = ? ORDER BY d.NUMERODIA, g.IDGASTO
                """;
        List<GastoRevision> gastos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, encuesta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int articulo = rs.getInt(7);
                    Integer articuloId = rs.wasNull() ? null : articulo;
                    gastos.add(new GastoRevision(rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getBigDecimal(4), rs.getString(5), rs.getBigDecimal(6), articuloId));
                }
            }
        }
        return gastos;
    }

    public static void visita(int hogar,int encuestador) throws SQLException {
        SesionActual.exigirUsuario(Rol.ENCUESTADOR, encuestador);
        try(Connection c=RolesDAO.conectar()) {c.setAutoCommit(false);try{
            HogarDAO.verificarAsignacion(c, hogar, encuestador, true);
            try(Statement s=c.createStatement()){s.execute("LOCK TABLE VISITA IN EXCLUSIVE MODE");}
            int id,numero;
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT NVL(MAX(IDVISITA),0)+1 FROM VISITA")){r.next();id=r.getInt(1);}
            try(PreparedStatement p=c.prepareStatement("SELECT NVL(MAX(NUMEROVISITA),0)+1 FROM VISITA WHERE IDHOGAR=?")){p.setInt(1,hogar);try(ResultSet r=p.executeQuery()){r.next();numero=r.getInt(1);}}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO VISITA (IDVISITA,NUMEROVISITA,FECHA,IDHOGAR,IDENCUESTADOR) VALUES (?,?,SYSDATE,?,?)")){p.setInt(1,id);p.setInt(2,numero);p.setInt(3,hogar);p.setInt(4,encuestador);p.executeUpdate();}
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}}
    }
    public static String validar(Connection con, int encuesta) throws SQLException {
        String diasCerrados = """
                SELECT COUNT(DISTINCT d.NUMERODIA), COUNT(g.IDGASTO)
                FROM DIACICLO d LEFT JOIN GASTO g ON g.IDDIA = d.IDDIA
                WHERE d.IDENCUESTA = ? AND d.NUMERODIA BETWEEN 1 AND 14
                  AND UPPER(TRIM(d.ESTADO)) = 'CERRADO'
                """;
        try (PreparedStatement ps = con.prepareStatement(diasCerrados)) {
            ps.setInt(1, encuesta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int faltan = 14 - rs.getInt(1);
                if (faltan > 0) {
                    return "Faltan " + faltan + " días por cerrar.";
                }
                if (rs.getInt(2) == 0) {
                    return "El ciclo debe tener al menos un gasto registrado.";
                }
            }
        }

        String pendientes = """
                SELECT COUNT(*) FROM OBSERVACION o
                JOIN GASTO g ON g.IDGASTO = o.IDGASTO
                JOIN DIACICLO d ON d.IDDIA = g.IDDIA
                WHERE d.IDENCUESTA = ? AND UPPER(TRIM(o.ESTADO)) = 'PENDIENTE'
                """;
        try (PreparedStatement ps = con.prepareStatement(pendientes)) {
            ps.setInt(1, encuesta);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int cantidad = rs.getInt(1);
                return cantidad == 0 ? null : "Hay " + cantidad + " observaciones pendientes de resolver.";
            }
        }
    }

    public static void transmitir(int hogar,int encuestador) throws SQLException {
        SesionActual.exigirUsuario(Rol.ENCUESTADOR, encuestador);
        try(Connection c=RolesDAO.conectar()) {c.setAutoCommit(false);try{
            HogarDAO.verificarAsignacion(c, hogar, encuestador, true);
            int id;String estado;
            try(PreparedStatement p=c.prepareStatement("SELECT IDENCUESTA,ESTADO FROM ENCUESTA WHERE IDHOGAR=? ORDER BY IDENCUESTA DESC")){p.setInt(1,hogar);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Aún no hay encuesta ni gastos para este hogar.");id=r.getInt(1);estado=r.getString(2);}}
            try(PreparedStatement p=c.prepareStatement("SELECT ESTADO FROM ENCUESTA WHERE IDENCUESTA=? FOR UPDATE")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("La encuesta ya no existe.");estado=r.getString(1);}}
            if(estado!=null && (estado.equalsIgnoreCase("TRANSMITIDO")||estado.equalsIgnoreCase("CERRADO")))throw new SQLException("La encuesta ya fue transmitida o cerrada.");
            String problema=validar(c,id);if(problema!=null)throw new SQLException(problema);
            try(PreparedStatement p=c.prepareStatement("UPDATE ENCUESTA SET ESTADO='TRANSMITIDO',FECHATRANSMISION=SYSDATE WHERE IDENCUESTA=? AND UPPER(NVL(ESTADO,'EN_PROGRESO')) NOT IN ('CERRADO','TRANSMITIDO')")){p.setInt(1,id);if(p.executeUpdate()!=1)throw new SQLException("El estado de la encuesta cambió; actualice la pantalla.");}
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}}
    }

    public static boolean puedeCorregir(String estado) {
        String normalizado = estado == null ? "" : estado.strip().toUpperCase(Locale.ROOT);
        return normalizado.isEmpty() || "EN_PROGRESO".equals(normalizado)
                || "EN_CORRECCION".equals(normalizado) || "TRANSMITIDO".equals(normalizado);
    }

    private static void verificarRevision(Connection con, int encuesta, int administrador, boolean cierre)
            throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        RolesDAO.verificarAdministrador(con, administrador);
        String sql = "SELECT ESTADO, IDADMINISTRADOR FROM ENCUESTA WHERE IDENCUESTA = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, encuesta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró la encuesta.");
                }
                String estado = rs.getString(1);
                if (cierre && (estado == null || !"TRANSMITIDO".equalsIgnoreCase(estado.strip()))) {
                    throw new SQLException("La encuesta debe estar transmitida.");
                }
                if (!cierre && !puedeCorregir(estado)) {
                    throw new SQLException("Esta encuesta ya no admite correcciones.");
                }
                int asignado = rs.getInt(2);
                if (!rs.wasNull() && asignado != administrador) {
                    throw new SQLException("La encuesta está a cargo de otro administrador.");
                }
            }
        }
    }

    public static void cerrar(int encuesta, int administrador) throws SQLException {
        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);
            try {
                verificarRevision(con, encuesta, administrador, true);
                String problema = validar(con, encuesta);
                if (problema != null) {
                    throw new SQLException(problema);
                }
                String sql = """
                        UPDATE ENCUESTA SET ESTADO = 'CERRADO', FECHACIERRE = SYSDATE, IDADMINISTRADOR = ?
                        WHERE IDENCUESTA = ?
                        """;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, administrador);
                    ps.setInt(2, encuesta);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    public static DatosEditables cargarCorreccion(int encuesta, int administrador, int gasto) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        try (Connection con = RolesDAO.conectar()) {
            return leerGastoEncuesta(con, encuesta, gasto);
        }
    }

    public static String notasPendientesDelGasto(int encuesta, int administrador, int gasto) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        String sql = """
                SELECT o.TEXTO FROM OBSERVACION o JOIN GASTO g ON g.IDGASTO = o.IDGASTO
                JOIN DIACICLO d ON d.IDDIA = g.IDDIA
                WHERE d.IDENCUESTA = ? AND g.IDGASTO = ? AND UPPER(TRIM(o.ESTADO)) = 'PENDIENTE'
                ORDER BY o.IDOBSERVACION
                """;
        StringBuilder notas = new StringBuilder();
        try (Connection con = RolesDAO.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, encuesta);
            ps.setInt(2, gasto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (notas.length() > 0) {
                        notas.append("\n");
                    }
                    notas.append(rs.getString(1));
                }
            }
        }
        return notas.toString();
    }

    private static DatosEditables leerGastoEncuesta(Connection con, int encuesta, int gasto) throws SQLException {
        DatosEditables datos = GastoDAO.leerEdicion(con, gasto);
        String sql = "SELECT 1 FROM DIACICLO WHERE IDDIA = ? AND IDENCUESTA = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, datos.diaId());
            ps.setInt(2, encuesta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El gasto no pertenece a esta encuesta.");
                }
            }
        }
        return datos;
    }

    public static void corregir(int encuesta, int administrador, GastoRevision gasto, DatosEditables anterior,
            CorreccionGasto cambios) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        Importes importes = GastoDAO.leerImportes(cambios.cantidad(), cambios.valor());
        String nota = cambios.nota().strip();
        if (nota.length() > 450) {
            throw new SQLException("La nota admite hasta 450 caracteres.");
        }
        String unidad = campoOpcional(cambios.unidad(), 30, "unidad");
        String adquisicion = campoOpcional(cambios.adquisicion(), 100, "forma de adquisición");
        String frecuencia = campoOpcional(cambios.frecuencia(), 50, "frecuencia");
        String tipo = campoOpcional(cambios.tipo(), 50, "tipo de gasto");
        String lugar = campoOpcional(cambios.lugar(), 100, "lugar de compra");

        try (Connection con = RolesDAO.conectar()) {
            con.setAutoCommit(false);
            try {
                verificarRevision(con, encuesta, administrador, false);
                DatosEditables actual = leerGastoEncuesta(con, encuesta, gasto.id());
                if (!actual.equals(anterior)) {
                    throw new SQLException("Este gasto cambió. Vuelve a abrirlo para corregirlo.");
                }
                Integer articulo = actual.articuloId();
                if (!Objects.equals(cambios.articulo(), gasto.articulo())) {
                    int encontrado = cambios.articulo() == null ? 0 : GastoDAO.buscarArticulo(con, cambios.articulo());
                    if (encontrado == 0) {
                        throw new SQLException("Selecciona un artículo del catálogo.");
                    }
                    articulo = encontrado;
                }
                DatosEditables nuevos = new DatosEditables(actual.diaId(), articulo, importes.cantidad(),
                        unidad, adquisicion, importes.valor(), frecuencia, tipo, lugar);
                GastoDAO.actualizarGasto(con, gasto.id(), nuevos);
                registrarCorreccion(con, encuesta, administrador, gasto.id(), nota);
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    private static String campoOpcional(String valor, int maximo, String etiqueta) throws SQLException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String limpio = valor.strip();
        if (limpio.length() > maximo) {
            throw new SQLException("El campo " + etiqueta + " admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    private static void registrarCorreccion(Connection con, int encuesta, int administrador, int gasto,
            String nota) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("LOCK TABLE OBSERVACION IN EXCLUSIVE MODE");
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE OBSERVACION SET ESTADO = 'RESUELTA' WHERE IDGASTO = ? AND UPPER(TRIM(ESTADO)) = 'PENDIENTE'")) {
            ps.setInt(1, gasto);
            ps.executeUpdate();
        }
        String insertar = """
                INSERT INTO OBSERVACION (IDOBSERVACION, TEXTO, FECHASOLICITUD, ESTADO, IDGASTO)
                SELECT NVL(MAX(IDOBSERVACION), 0) + 1, ?, SYSDATE, 'RESUELTA', ? FROM OBSERVACION
                """;
        try (PreparedStatement ps = con.prepareStatement(insertar)) {
            ps.setString(1, nota.isEmpty() ? "Gasto corregido por el administrador." : "Corrección administrativa. " + nota);
            ps.setInt(2, gasto);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE ENCUESTA SET IDADMINISTRADOR = ? WHERE IDENCUESTA = ?")) {
            ps.setInt(1, administrador);
            ps.setInt(2, encuesta);
            ps.executeUpdate();
        }
    }

    public static List<ObservacionPendiente> listarObservacionesPendientes(int hogar) throws SQLException {
        String sql = """
                SELECT o.IDOBSERVACION, d.NUMERODIA, g.IDGASTO, a.DESCRIPCION, o.TEXTO, d.ESTADO
                FROM OBSERVACION o JOIN GASTO g ON g.IDGASTO = o.IDGASTO
                JOIN DIACICLO d ON d.IDDIA = g.IDDIA
                JOIN ENCUESTA e ON e.IDENCUESTA = d.IDENCUESTA
                LEFT JOIN ARTICULO a ON a.IDARTICULO = g.IDARTICULO
                WHERE e.IDHOGAR = ? AND UPPER(TRIM(e.ESTADO)) = 'EN_CORRECCION'
                  AND e.IDENCUESTA = (SELECT MAX(actual.IDENCUESTA) FROM ENCUESTA actual WHERE actual.IDHOGAR = e.IDHOGAR)
                  AND UPPER(TRIM(o.ESTADO)) = 'PENDIENTE' ORDER BY d.NUMERODIA, o.IDOBSERVACION
                """;
        List<ObservacionPendiente> observaciones = new ArrayList<>();
        try (Connection con = RolesDAO.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            HogarDAO.verificarConsulta(con, hogar);
            ps.setInt(1, hogar);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString(6);
                    observaciones.add(new ObservacionPendiente(rs.getInt(1), rs.getInt(2), rs.getInt(3),
                            rs.getString(4), rs.getString(5), estado != null && "CERRADO".equalsIgnoreCase(estado.strip())));
                }
            }
        }
        return observaciones;
    }

    public static String observacionesPendientes(int hogar) throws SQLException {
        StringBuilder lista = new StringBuilder();
        for (ObservacionPendiente observacion : listarObservacionesPendientes(hogar)) {
            lista.append("Observación ").append(observacion.id()).append(" · ").append(observacion)
                    .append(" · Gasto ").append(observacion.gasto()).append("\n")
                    .append(observacion.texto()).append("\n\n");
        }
        return lista.toString();
    }

    public static void resolverObservacion(int hogar,int encuestador,int observacion) throws SQLException {
        SesionActual.exigirUsuario(Rol.ENCUESTADOR, encuestador);
        try(Connection c=RolesDAO.conectar()){c.setAutoCommit(false);try{
            HogarDAO.verificarAsignacion(c, hogar, encuestador, true);
            String sql="UPDATE OBSERVACION SET ESTADO='RESUELTA' WHERE IDOBSERVACION=? AND UPPER(TRIM(ESTADO))='PENDIENTE' AND IDGASTO IN (SELECT g.IDGASTO FROM GASTO g JOIN DIACICLO d ON d.IDDIA=g.IDDIA JOIN ENCUESTA e ON e.IDENCUESTA=d.IDENCUESTA WHERE e.IDHOGAR=? AND UPPER(TRIM(e.ESTADO))='EN_CORRECCION')";
            try(PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,observacion);p.setInt(2,hogar);if(p.executeUpdate()!=1)throw new SQLException("No se encontró una observación pendiente de este hogar en corrección.");}
            c.commit();
        }catch(SQLException ex){c.rollback();throw ex;}}
    }

    public static List<EncuestaResumen> listarEncuestas(int administrador) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        String sql = """
                SELECT e.IDENCUESTA, e.IDHOGAR, NVL(TRIM(h.NOMBREFAMILIA), 'Hogar #' || h.IDHOGAR),
                    e.ESTADO, c.NOMBRE
                FROM ENCUESTA e JOIN HOGAR h ON h.IDHOGAR = e.IDHOGAR
                LEFT JOIN COORDINADOR c ON c.IDCOORDINADOR = e.IDADMINISTRADOR
                ORDER BY e.IDENCUESTA DESC
                """;
        List<EncuestaResumen> encuestas = new ArrayList<>();
        try (Connection con = RolesDAO.conectar(); Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                encuestas.add(new EncuestaResumen(rs.getInt(1), rs.getInt(2), rs.getString(3),
                        rs.getString(4), rs.getString(5)));
            }
        }
        return encuestas;
    }

    public static List<GastoRevision> listarGastosRevision(int administrador, int encuesta) throws SQLException {
        SesionActual.exigirUsuario(Rol.ADMINISTRADOR, administrador);
        try (Connection con = RolesDAO.conectar()) {
            return listarGastosRevision(con, encuesta);
        }
    }

}
