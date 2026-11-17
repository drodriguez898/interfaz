package negocio;

import java.time.LocalDateTime;
import java.util.List;

public record DatosInformeEncuesta(int encuesta, String familia, String ubicacion, String encuestador, String responsable,
        LocalDateTime transmision, LocalDateTime cierre, int diasCerrados, List<GastoRevision> gastos) {}
