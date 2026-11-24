package negocio;

import java.time.LocalDate;

public record DatosHogar(String familia, String direccion, String departamento, String municipio,
        LocalDate inicio, int personas) {}
